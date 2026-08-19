param(
    [int]$TimeoutSeconds = 60,
    [int]$ProbeIntervalMilliseconds = 250
)

$ErrorActionPreference = 'Stop'
$composeRoot = Split-Path -Parent $PSScriptRoot
Set-Location $composeRoot

function Invoke-HaSql {
    param([Parameter(Mandatory)][string]$Sql)

    $encodedSql = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($Sql))
    $result = docker compose exec -T patroni-1 sh -c `
        'echo "$1" | base64 -d | PGPASSWORD="$POSTGRES_PASSWORD" psql -h haproxy -p 5000 -U postgres -d sigecpj -Atq -v ON_ERROR_STOP=1' `
        _ $encodedSql 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw 'HAProxy/PostgreSQL no acepto la consulta.'
    }
    return ($result | Out-String).Trim()
}

$cluster = docker compose exec -T patroni-1 `
    patronictl -c /etc/patroni/patroni.yml list -f json | ConvertFrom-Json
$oldLeader = ($cluster | Where-Object Role -eq 'Leader').Member
if (-not $oldLeader) {
    throw 'No se encontro un lider Patroni antes de la prueba.'
}

$marker = "pre-fallo-$([guid]::NewGuid().ToString('N'))"
$probe = "post-fallo-$([guid]::NewGuid().ToString('N'))"
$attempts = 0
$recovered = $false
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

try {
    Invoke-HaSql @'
CREATE TABLE IF NOT EXISTS public.ha_failover_probe (
    marker text PRIMARY KEY,
    confirmado_en timestamptz NOT NULL DEFAULT clock_timestamp()
)
'@ | Out-Null
    Invoke-HaSql "INSERT INTO public.ha_failover_probe(marker) VALUES ('$marker')" | Out-Null

    $stopwatch.Restart()
    docker compose kill -s SIGKILL $oldLeader | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo detener abruptamente $oldLeader."
    }

    while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
        $attempts++
        try {
            Invoke-HaSql "INSERT INTO public.ha_failover_probe(marker) VALUES ('$probe')" | Out-Null
            $recovered = $true
            break
        }
        catch {
            Start-Sleep -Milliseconds $ProbeIntervalMilliseconds
        }
    }
    $stopwatch.Stop()

    if (-not $recovered) {
        throw "No se recuperaron las escrituras en $TimeoutSeconds segundos."
    }

    $rpoCheck = Invoke-HaSql "SELECT count(*) FROM public.ha_failover_probe WHERE marker IN ('$marker', '$probe')"
    $newCluster = docker compose exec -T patroni-1 `
        patronictl -c /etc/patroni/patroni.yml list -f json | ConvertFrom-Json
    $newLeader = ($newCluster | Where-Object Role -eq 'Leader').Member

    [pscustomobject]@{
        LiderAnterior = $oldLeader
        LiderNuevo = $newLeader
        RtoSegundos = [math]::Round($stopwatch.Elapsed.TotalSeconds, 3)
        Intentos = $attempts
        MarcadoresConfirmados = [int]$rpoCheck
        Rpo = if ([int]$rpoCheck -eq 2) { 0 } else { 'FALLO' }
    } | Format-List
}
finally {
    docker compose up -d $oldLeader | Out-Null

    $deadline = (Get-Date).AddSeconds(120)
    do {
        Start-Sleep -Seconds 2
        $state = docker compose exec -T patroni-1 `
            patronictl -c /etc/patroni/patroni.yml list -f json 2>$null | ConvertFrom-Json
        $oldNode = $state | Where-Object Member -eq $oldLeader
    } while ((Get-Date) -lt $deadline -and $oldNode.State -notin @('running', 'streaming'))

    try {
        Invoke-HaSql 'DROP TABLE IF EXISTS public.ha_failover_probe' | Out-Null
    }
    catch {
        Write-Warning 'No se pudo limpiar la tabla temporal ha_failover_probe.'
    }
}
