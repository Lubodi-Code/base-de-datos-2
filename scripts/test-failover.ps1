param(
    [int]$TimeoutSeconds = 60,
    [int]$ProbeIntervalMilliseconds = 250,
    [int]$RestoreWaitSeconds = 120,
    [int]$PatroniTtlSeconds = 20,
    [int]$PatroniLoopWaitSeconds = 2,
    [int]$PatroniRetryTimeoutSeconds = 3
)

$ErrorActionPreference = 'Stop'
$composeRoot = Split-Path -Parent $PSScriptRoot
$patroniServices = @('patroni-1', 'patroni-2', 'patroni-3')
$script:SqlClientService = $null
$oldLeader = $null
$leaderStopped = $false
$tableCreated = $false

Set-Location $composeRoot

function Get-RunningComposeServices {
    $services = @(docker compose ps --status running --services 2>$null)
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker Compose no esta disponible o no se pudo leer el proyecto.'
    }
    return $services
}

function Get-PatroniCluster {
    param([Parameter(Mandatory)][string]$ControlService)

    $json = docker compose exec -T $ControlService `
        patronictl -c /etc/patroni/patroni.yml list -f json 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $json) {
        throw "No se pudo consultar Patroni mediante $ControlService."
    }
    return @($json | ConvertFrom-Json)
}

function Invoke-HaSql {
    param([Parameter(Mandatory)][string]$Sql)

    $encodedSql = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($Sql))
    $result = docker compose exec -T $script:SqlClientService sh -c `
        'echo "$1" | base64 -d | PGPASSWORD="$POSTGRES_PASSWORD" psql -h haproxy -p 5000 -U postgres -d sigecpj -Atq -v ON_ERROR_STOP=1' `
        _ $encodedSql 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw 'HAProxy/PostgreSQL no acepto la consulta.'
    }
    return ($result | Out-String).Trim()
}

function Get-ApiHealthStatus {
    $status = curl.exe -k -s -o NUL -w '%{http_code}' `
        --connect-timeout 1 --max-time 2 https://localhost:8443/actuator/health 2>$null
    if ($LASTEXITCODE -ne 0) {
        return 0
    }
    return [int]$status
}

if ($PatroniTtlSeconds -lt 20) {
    throw 'Patroni 4.1.5 exige ttl >= 20; un valor menor se ajustaria silenciosamente a 20.'
}
if ($PatroniTtlSeconds -lt ($PatroniLoopWaitSeconds + (2 * $PatroniRetryTimeoutSeconds))) {
    throw 'La configuracion no cumple loop_wait + 2 * retry_timeout <= ttl.'
}

$runningServices = Get-RunningComposeServices
$missingServices = @($patroniServices | Where-Object { $_ -notin $runningServices })
if ($missingServices.Count -gt 0) {
    $missing = $missingServices -join ', '
    throw "La prueba requiere los 3 nodos Patroni activos. Faltan: $missing. Ejecuta: docker compose up -d patroni-1 patroni-2 patroni-3"
}

$controlService = $patroniServices | Where-Object { $_ -in $runningServices } | Select-Object -First 1
$cluster = Get-PatroniCluster -ControlService $controlService
$leaders = @($cluster | Where-Object Role -eq 'Leader')
$readyNodes = @($cluster | Where-Object State -in @('running', 'streaming'))

if ($cluster.Count -ne 3 -or $readyNodes.Count -ne 3) {
    throw 'Los 3 contenedores existen, pero el cluster aun no esta listo. Espera unos segundos y vuelve a ejecutar la prueba.'
}
if ($leaders.Count -ne 1) {
    throw "Se esperaba exactamente 1 lider y Patroni reporto $($leaders.Count). No es seguro iniciar la prueba."
}

# bootstrap.dcs solo se aplica al crear el cluster. Se actualiza tambien el DCS
# para que instalaciones con volumenes existentes usen estos tiempos.
Write-Host 'Aplicando tiempos dinamicos de Patroni...'
docker compose exec -T $controlService patronictl -c /etc/patroni/patroni.yml `
    edit-config --quiet --force `
    --set "ttl=$PatroniTtlSeconds" `
    --set "loop_wait=$PatroniLoopWaitSeconds" `
    --set "retry_timeout=$PatroniRetryTimeoutSeconds" | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo actualizar la configuracion dinamica de Patroni.'
}
Start-Sleep -Seconds ($PatroniLoopWaitSeconds + 1)

$cluster = Get-PatroniCluster -ControlService $controlService
$oldLeader = ($cluster | Where-Object Role -eq 'Leader').Member
$survivors = @($cluster | Where-Object Member -ne $oldLeader)
$script:SqlClientService = ($survivors | Select-Object -First 1).Member
$controlService = $script:SqlClientService

if (-not $oldLeader -or $survivors.Count -ne 2) {
    throw 'No se encontro un lider y dos replicas disponibles antes de la prueba.'
}

$marker = "pre-fallo-$([guid]::NewGuid().ToString('N'))"
$probe = "post-fallo-$([guid]::NewGuid().ToString('N'))"
$attempts = 0
$sqlRecovered = $false
$apiRecovered = $false
$sqlRtoSeconds = $null
$apiRtoSeconds = $null
$stopwatch = [System.Diagnostics.Stopwatch]::new()

try {
    Write-Host "Lider detectado: $oldLeader"
    Write-Host "Replicas supervivientes: $(($survivors.Member) -join ', ')"

    Invoke-HaSql @'
CREATE TABLE IF NOT EXISTS public.ha_failover_probe (
    marker text PRIMARY KEY,
    confirmado_en timestamptz NOT NULL DEFAULT clock_timestamp()
)
'@ | Out-Null
    $tableCreated = $true
    Invoke-HaSql "INSERT INTO public.ha_failover_probe(marker) VALUES ('$marker') ON CONFLICT DO NOTHING" | Out-Null

    Write-Host "Deteniendo abruptamente solo el lider $oldLeader..."
    $stopwatch.Start()
    docker compose kill -s SIGKILL $oldLeader | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo detener $oldLeader."
    }
    $leaderStopped = $true

    Write-Host 'Esperando la promocion de una replica y la recuperacion de la API...'
    while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
        $attempts++

        if (-not $sqlRecovered) {
            try {
                Invoke-HaSql "INSERT INTO public.ha_failover_probe(marker) VALUES ('$probe') ON CONFLICT DO NOTHING" | Out-Null
                $sqlRecovered = $true
                $sqlRtoSeconds = $stopwatch.Elapsed.TotalSeconds
            }
            catch {
                # Es normal mientras Patroni elige al nuevo lider.
            }
        }

        if (-not $apiRecovered -and (Get-ApiHealthStatus) -eq 200) {
            $apiRecovered = $true
            $apiRtoSeconds = $stopwatch.Elapsed.TotalSeconds
        }

        if ($sqlRecovered -and $apiRecovered) {
            break
        }
        Start-Sleep -Milliseconds $ProbeIntervalMilliseconds
    }
    $stopwatch.Stop()

    if (-not $sqlRecovered) {
        throw "Las escrituras no se recuperaron en $TimeoutSeconds segundos."
    }
    if (-not $apiRecovered) {
        throw "PostgreSQL se recupero, pero la API no devolvio HTTP 200 en $TimeoutSeconds segundos."
    }

    $rpoCheck = Invoke-HaSql "SELECT count(*) FROM public.ha_failover_probe WHERE marker IN ('$marker', '$probe')"
    $newCluster = Get-PatroniCluster -ControlService $controlService
    $newLeader = ($newCluster | Where-Object Role -eq 'Leader').Member

    Write-Host ''
    Write-Host 'RESULTADO DE LA PRUEBA' -ForegroundColor Cyan
    [pscustomobject]@{
        LiderAnterior = $oldLeader
        LiderNuevo = $newLeader
        RtoEscrituraSegundos = [math]::Round($sqlRtoSeconds, 3)
        RtoApiSegundos = [math]::Round($apiRtoSeconds, 3)
        Intentos = $attempts
        MarcadoresConfirmados = [int]$rpoCheck
        Rpo = if ([int]$rpoCheck -eq 2) { 0 } else { 'FALLO' }
        Resultado = if ($newLeader -and [int]$rpoCheck -eq 2) { 'APROBADO' } else { 'FALLO' }
    } | Format-List
}
finally {
    if ($leaderStopped -and $oldLeader) {
        Write-Host "Restaurando $oldLeader..."
        docker compose up -d $oldLeader | Out-Null

        $deadline = (Get-Date).AddSeconds($RestoreWaitSeconds)
        do {
            Start-Sleep -Seconds 2
            try {
                $state = Get-PatroniCluster -ControlService $controlService
                $oldNode = $state | Where-Object Member -eq $oldLeader
            }
            catch {
                $oldNode = $null
            }
        } while ((Get-Date) -lt $deadline -and $oldNode.State -notin @('running', 'streaming'))

        if (-not $oldNode -or $oldNode.State -notin @('running', 'streaming')) {
            Write-Warning "$oldLeader fue iniciado, pero no se reincorporo antes del limite de tiempo."
        }
    }

    if ($tableCreated) {
        try {
            Invoke-HaSql 'DROP TABLE IF EXISTS public.ha_failover_probe' | Out-Null
        }
        catch {
            Write-Warning 'No se pudo limpiar la tabla temporal ha_failover_probe.'
        }
    }
}
