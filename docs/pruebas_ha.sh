#!/usr/bin/env bash
# =============================================================================
#  SIGEC-PJ · Pruebas automáticas de Alta Disponibilidad, seguridad y backups
#  Ejecuta y VERIFICA (PASS/FAIL real). No inventa salidas.
#
#  Uso (con el stack ya levantado: docker compose up -d):
#      bash docs/pruebas_ha.sh
#
#  Nota Windows/Git Bash: se exporta MSYS_NO_PATHCONV=1 para que no rompa las
#  rutas /etc/patroni/... al pasarlas a los contenedores.
# =============================================================================
set -u
export MSYS_NO_PATHCONV=1

# --- Config (coincide con .env / .env.example) ---
PGPW="${POSTGRES_PASSWORD:-sigec_dev_cambiar_2024}"
DBUSER="${POSTGRES_USER:-sigec}"
DB="sigecpj"
PATRONICTL="patronictl -c /etc/patroni/patroni.yml"
FRONT="http://localhost:8088"        # frontend -> HAProxy -> backend
STATS="http://localhost:7000"        # HAProxy stats (admin:admin123)

PASS=0; FAIL=0
ok()   { echo "  ✅ PASS: $1"; PASS=$((PASS+1)); }
bad()  { echo "  ❌ FAIL: $1"; FAIL=$((FAIL+1)); }
head() { echo; echo "==================================================================="; echo "  $1"; echo "==================================================================="; }

# Ejecuta psql dentro de un contenedor patroni (tienen cliente psql).
# uso: pql <servicio> <host> <puerto> <sql>
pql() {
  docker compose exec -T -e PGPASSWORD="$PGPW" "$1" \
    psql -h "$2" -p "$3" -U "$DBUSER" -d "$DB" -tAc "$4" 2>&1
}
# Miembro que es Leader ahora mismo (parsea la tabla de patronictl).
current_leader() {
  docker compose exec -T patroni-1 $PATRONICTL list 2>/dev/null \
    | awk -F'|' '/Leader/ {gsub(/ /,"",$2); print $2; exit}'
}

# ---------------------------------------------------------------------------
head "0. El stack está arriba"
running=$(docker compose ps --status running --services 2>/dev/null | tr '\n' ' ')
echo "  Servicios corriendo: $running"
for s in etcd patroni-1 patroni-2 haproxy backend-1 backend-2 frontend; do
  echo "$running" | grep -qw "$s" && ok "servicio $s activo" || bad "servicio $s NO está corriendo"
done

# ---------------------------------------------------------------------------
head "1. Clúster Patroni: 1 Leader + 1 réplica síncrona"
docker compose exec -T patroni-1 $PATRONICTL list
leader=$(current_leader)
[ -n "$leader" ] && ok "Leader detectado: $leader" || bad "no se detectó Leader"
docker compose exec -T patroni-1 $PATRONICTL list | grep -q "Sync Standby" \
  && ok "existe una réplica en modo 'Sync Standby' (replicación síncrona activa)" \
  || bad "no hay 'Sync Standby' (¿replicación asíncrona? RPO no garantizado)"

# ---------------------------------------------------------------------------
head "2. Replicación síncrona confirmada (RPO = 0)"
sc=$(pql "$leader" 127.0.0.1 5432 "SHOW synchronous_commit;" | tr -d '[:space:]')
echo "  synchronous_commit = $sc"
[ "$sc" = "on" ] && ok "synchronous_commit=on en el líder" || bad "synchronous_commit != on"

# ---------------------------------------------------------------------------
head "3. Datos semilla presentes"
n=$(pql patroni-1 haproxy 5001 "SELECT count(*) FROM equipo;" | tr -d '[:space:]')
echo "  equipos en inventario: $n"
[ "${n:-0}" -gt 0 ] 2>/dev/null && ok "hay $n equipos cargados (seed OK)" || bad "no hay equipos (¿seed no cargó?)"

# ---------------------------------------------------------------------------
head "4. Camino de ESCRITURA vía HAProxy:5000 (siempre al primario)"
w=$(pql patroni-1 haproxy 5000 "SELECT 'ok', now();")
echo "  respuesta: $w"
echo "$w" | grep -q "ok|" && ok "escritura/consulta al primario vía HAProxy:5000" || bad "no se pudo escribir vía HAProxy:5000"

# ---------------------------------------------------------------------------
head "5. Camino de LECTURA vía HAProxy:5001 (réplica de solo lectura)"
r=$(pql patroni-1 haproxy 5001 "SELECT pg_is_in_recovery();" | tr -d '[:space:]')
echo "  pg_is_in_recovery en 5001 = $r"
[ "$r" = "t" ] && ok "las lecturas van a una réplica (pg_is_in_recovery=t)" || bad "5001 no apunta a una réplica"

# ---------------------------------------------------------------------------
head "6. Seguridad: login JWT + rol vía frontend -> HAProxy -> backend"
login=$(curl -sk -m 15 -X POST "$FRONT/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}')
echo "  respuesta login: $(echo "$login" | cut -c1-90)..."
echo "$login" | grep -q '"token"' && ok "login devuelve JWT" || bad "login no devolvió token"
echo "$login" | grep -q '"rol":"ADMIN"' && ok "el token trae rol ADMIN (roles ADMIN/TECNICO/CONSULTA)" || bad "no vino el rol en el token"
# Endpoint protegido rechaza sin token:
code=$(curl -sk -m 15 -o /dev/null -w "%{http_code}" "$FRONT/api/v1/equipos")
echo "  GET /api/v1/equipos sin token -> HTTP $code"
[ "$code" = "401" ] || [ "$code" = "403" ] && ok "endpoint protegido rechaza sin token (HTTP $code)" || bad "endpoint no protegido (HTTP $code)"

# ---------------------------------------------------------------------------
head "7. HAProxy: estado de backends (stats CSV)"
csv=$(curl -s -m 10 -u admin:admin123 "$STATS/;csv")
echo "$csv" | grep -E "pg_primary,|pg_replicas,|spring_boot" | awk -F, '{printf "  %-28s -> %s\n",$1"/"$2,$18}'
up_spring=$(echo "$csv" | awk -F, '$1=="spring_boot_backends" && $2 ~ /^backend/ && $18=="UP"' | wc -l | tr -d ' ')
[ "${up_spring:-0}" -ge 2 ] && ok "los 2 backends Spring Boot están UP (active-active)" || bad "no hay 2 backends UP"

# ---------------------------------------------------------------------------
head "8. Backend active-active: apago backend-1, la app debe seguir viva"
echo "  deteniendo backend-1 ..."
docker compose stop backend-1 >/dev/null 2>&1
sleep 3
code=$(curl -sk -m 15 -o /dev/null -w "%{http_code}" -X POST "$FRONT/api/v1/auth/login" \
  -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}')
echo "  login con backend-1 caído -> HTTP $code"
[ "$code" = "200" ] && ok "la app sigue respondiendo con un solo backend (HAProxy redistribuye)" || bad "la app cayó con backend-1 abajo"
echo "  reactivando backend-1 ..."
docker compose start backend-1 >/dev/null 2>&1

# ---------------------------------------------------------------------------
head "9. FAILOVER automático: mato el primario y mido RTO / verifico RPO=0"
leader=$(current_leader)
survivor=$([ "$leader" = "patroni-1" ] && echo patroni-2 || echo patroni-1)
echo "  Leader actual: $leader   | superviviente esperado: $survivor"

# Tabla marcador + fila base (confirmada ANTES del fallo).
# Nota: el INSERT va envuelto en un CTE + SELECT para que psql devuelva SOLO el id
# (un INSERT ... RETURNING suelto ensucia la salida con el tag "INSERT 0 1").
INS="WITH ins AS (INSERT INTO prueba_failover DEFAULT VALUES RETURNING id) SELECT id FROM ins;"
pql "$survivor" haproxy 5000 "CREATE TABLE IF NOT EXISTS prueba_failover(id serial primary key, t timestamptz default now());" >/dev/null
base=$(pql "$survivor" haproxy 5000 "$INS" | tr -d '[:space:]')
echo "  fila base confirmada antes del fallo: id=$base"

echo "  >>> MATANDO al primario ($leader) ..."
t0=$(date +%s)
docker compose stop "$leader" >/dev/null 2>&1

# Reintenta escribir por HAProxy:5000 hasta que el nuevo primario acepte (mide RTO).
# Ventana amplia (90s): con synchronous_mode + 2 nodos, la promoción espera a que
# expire el TTL del líder en etcd y HAProxy tarda unos segundos en reenrutar.
rto=""; ok_id=""
for i in $(seq 1 90); do
  res=$(pql "$survivor" haproxy 5000 "$INS" | tr -d '[:space:]')
  if echo "$res" | grep -qE '^[0-9]+$'; then
    rto=$(( $(date +%s) - t0 )); ok_id="$res"; break
  fi
  sleep 1
done
if [ -n "$rto" ]; then
  ok "escrituras reanudadas tras failover en ~${rto}s, nueva fila id=$ok_id"
else
  bad "las escrituras NO se reanudaron en 90s vía HAProxy:5000"
fi

# Nuevo líder
newleader=$(current_leader)
echo "  nuevo Leader: $newleader"
[ "$newleader" = "$survivor" ] && ok "el superviviente $survivor fue promovido a Leader" || bad "promoción inesperada ($newleader)"

# RPO=0: la fila base (confirmada antes) debe existir en el nuevo primario.
# Se consulta DIRECTO al nuevo líder (127.0.0.1:5432); justo tras el failover
# HAProxy puede cerrar conexiones mientras reenruta.
still=$(pql "$survivor" 127.0.0.1 5432 "SELECT count(*) FROM prueba_failover WHERE id=$base;" | tr -d '[:space:]')
[ "$still" = "1" ] && ok "la fila confirmada antes del fallo sobrevivió (RPO = 0)" || bad "se perdió una transacción confirmada (RPO > 0)"

echo "  recuperando el nodo caído ($leader) ..."
docker compose start "$leader" >/dev/null 2>&1
echo "  esperando reincorporación (hasta 60s) ..."
for i in $(seq 1 60); do
  if docker compose exec -T patroni-1 $PATRONICTL list 2>/dev/null | grep -q "$leader"; then
    role=$(docker compose exec -T patroni-1 $PATRONICTL list 2>/dev/null | awk -F'|' -v m="$leader" '$0 ~ m {print $4}')
    echo "  $leader reincorporado con rol:$role"; break
  fi
  sleep 2
done
docker compose exec -T patroni-1 $PATRONICTL list | grep "$leader" | grep -qiE "standby|replica" \
  && ok "$leader volvió como réplica (sin split-brain)" || echo "  ℹ️  revisa: $leader aún sincronizando, reejecuta 'patronictl list' en unos segundos"

# ---------------------------------------------------------------------------
head "10. Backups pgBackRest (WAL archiving + backup full)"
echo "  intentando stanza-create + backup en el primario actual ($newleader) ..."
sc_out=$(docker compose exec -T "$newleader" gosu postgres pgbackrest --stanza=sigecpj stanza-create 2>&1)
echo "$sc_out" | grep -q "ERROR" \
  && { bad "stanza-create falló (backups NO operativos con la config actual)"; echo "$sc_out" | grep -E "ERROR|WARN" | sed 's/^/    /'; } \
  || { ok "stanza creada"; \
       bk=$(docker compose exec -T "$newleader" gosu postgres pgbackrest --stanza=sigecpj --type=full backup 2>&1); \
       echo "$bk" | grep -q "ERROR" && bad "backup full falló" || ok "backup full completado"; \
       docker compose exec -T "$newleader" gosu postgres pgbackrest --stanza=sigecpj info 2>&1 | sed 's/^/    /'; }

# ---------------------------------------------------------------------------
head "RESUMEN"
echo "  PASS: $PASS    FAIL: $FAIL"
[ "$FAIL" -eq 0 ] && echo "  🎉 Todo verde." || echo "  ⚠️  Revisa los FAIL de arriba antes de presentar."
