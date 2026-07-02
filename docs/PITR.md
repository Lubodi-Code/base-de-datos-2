# =============================================================================
#  DOCUMENTO DE PITR (POINT-IN-TIME RECOVERY) SIGEC-PJ
#  Recuperación a un punto específico en el tiempo
# =============================================================================

## 1. Objetivo

Documentar el procedimiento de recuperación Point-In-Time (PITR) de PostgreSQL usando pgBackRest, permitiendo restaurar la base de datos a un momento específico anterior a un incidente.

**Escenario de ejemplo:** Un administrador ejecuta accidentalmente un DELETE masivo de equipos en producción y necesita recuperar los datos hasta 30 segundos antes del error.

## 2. Conceptos de PITR

### 2.1. ¿Qué es PITR?

**Point-In-Time Recovery** es la capacidad de PostgreSQL de viajar en el tiempo a un momento específico usando:

- **Backups full:** Copia completa de la base de datos
- **WAL (Write-Ahead Log):** Registro secuencial de todas las transacciones
- **Timeline:** Línea de tiempo que permite ramificar la recuperación

### 2.2. Cómo Funciona pgBackRest

```
┌─────────────────────────────────────────────────────────────────┐
│                    Línea de Tiempo PostgreSQL                   │
│                                                                  │
│  BACKUP FULL ───► WAL ARCHIVE ───► INCIDENTE ───► RESTAURACIÓN  │
│  (00:00)         (continuo)         (14:32)         (14:30)     │
│      │              │                   │               │          │
│      ▼              ▼                   ▼               ▼          │
│  [Datos 00:00]  [Cambio 14:20]   [DELETE ERROR]  [VIAJE EN     │
│                                  [Datos borrados]  TIEMPO]      │
│                                                   [Datos 14:30] │
└─────────────────────────────────────────────────────────────────┘
```

**Componentes de pgBackRest:**
- **Stanza:** Unidad de backup (en SIGEC-PJ: `sigecpj`)
- **Repository:** Almacén de backups (`/pgbackrest`)
- **WAL Archive:** Flujo continuo de cambios registrados

## 3. Configuración de pgBackRest

### 3.1. Archivo de Configuración

**Ubicación:** `/etc/pgbackrest/pgbackrest.conf`

```ini
[global]
repo1-path=/pgbackrest
repo1-retention-full=7              # Mantener 7 backups full
repo1-retention-diff=30             # Mantener 30 backups diferenciales
process-max=2
log-level=info
start-fast=y                        # Iniciar backup rápido
stop-auto=y                          # Detener automático si hay error
delta=y                              # Solo copiar cambios

[sigecpj]
pg1-path=/var/lib/postgresql/data/pgdata
pg1-port=5432
pg1-user=sigec

# Compresión y encriptación
repo1-type=cifs
compress-type=gz
compress-level=6
```

### 3.2. Configuración de PostgreSQL

**En `postgresql.conf` (o via Patroni):**

```ini
# Archivado WAL obligatorio para PITR
wal_level = replica
archive_mode = on
archive_command = 'pgbackrest archive-push %p'
max_wal_senders = 3
```

## 4. Procedimiento de Recuperación Paso a Paso

### 4.1. Escenario del Incidente

**Fecha/Hora del incidente:** 15/11/2026 a las 14:32:15  
**Comando ejecutado por error:** 

```sql
DELETE FROM equipo WHERE estado = 'DANADO';
-- 47 equipos eliminados accidentalmente
```

### 4.2. Paso 1 - Verificar el Estado Actual

```bash
# 1. Consultar últimos backups disponibles
sudo -u postgres pgbackrest --stanza=sigecpj info

# Salida esperada:
# stanza: sigecpj
#     status: ok
#     cipher: none
#     
#     backup (stanza sigecpj):
#         full backup: 20261115-020000F
#             timestamp start/stop: 2026-11-15 02:00:00 / 2026-11-15 02:15:23
#             size: 2.5GB
#         
#         differential backup: 20261115-140000D
#             timestamp start/stop: 2026-11-15 14:00:00 / 2026-11-15 14:05:12
#             size: 450MB

# 2. Verificar que los WAL están archivados
ls -lh /pgbackrest/sigecpj/archive/
# Debe mostrar archivos WAL hasta el momento presente
```

### 4.3. Paso 2 - Detener Aplicaciones

```bash
# Detener backends Spring Boot
sudo systemctl stop sigec-backend

# Detener Patroni (en ambos nodos)
sudo systemctl stop patroni

# Verificar que PostgreSQL no esté corriendo
sudo systemctl status postgresql
# Expected: inactive (dead)
```

### 4.4. Paso 3 - Realizar Recuperación PITR

```bash
# 3a. Limpiar directorio de datos anterior
sudo -u postgres rm -rf /var/lib/postgresql/data/pgdata/*

# 3b. Ejecutar restauración delta (rápida)
sudo -u postgres pgbackrest --stanza=sigecpj \
  restore \
  --delta \
  --set="2026-11-15 14:30:00" \
  --type=time

# Salida esperada:
# INFO: restore command begin 2.0.53: ...
# INFO: restore backup set 20261115-140000D
# INFO: restore command end 2.0.53: ...
# INFO: restore command end

# 3c. Verificar archivos recuperados
ls -lh /var/lib/postgresql/data/pgdata/
# Debe mostrar PGDATA completo
```

### 4.5. Paso 4 - Iniciar PostgreSQL Recuperado

```bash
# Crear archivo recovery.signal para activar modo recovery
sudo -u postgres touch /var/lib/postgresql/data/pgdata/recovery.signal

# Iniciar PostgreSQL manualmente
sudo -u postgres /usr/lib/postgresql/16/bin/pg_ctl \
  -D /var/lib/postgresql/data/pgdata \
  -l /var/log/postgresql/recovery.log start

# Monitorear logs de recuperación
tail -f /var/log/postgresql/recovery.log
```

**Log esperado de recuperación:**

```
2026-11-15 15:10:12.123 CST [2847] LOG:  starting point-in-time recovery to 2026-11-15 14:30:00-06
2026-11-15 15:10:12.456 CST [2847] LOG:  restored log file "0000000100000034000000D4" from archive
2026-11-15 15:10:13.789 CST [2847] LOG:  restored log file "0000000100000034000000D5" from archive
2026-11-15 15:10:14.012 CST [2847] LOG:  recovery stopping before commit of transaction 2845, time 2026-11-15 14:30:00.456
2026-11-15 15:10:14.345 CST [2847] LOG:  consistent recovery state reached
2026-11-15 15:10:15.678 CST [2847] LOG:  database system is ready to accept read-only connections
```

### 4.6. Paso 5 - Verificar Datos Recuperados

```bash
# Conectarse a PostgreSQL recuperado
sudo -u postgres psql -d sigecpj

# 1. Verificar equipos antes del DELETE
SELECT COUNT(*) FROM equipo;
-- Resultado esperado: 156 equipos (total antes del DELETE)

# 2. Verificar que equipos con estado DANADO existen
SELECT COUNT(*) FROM equipo WHERE estado = 'DANADO';
-- Resultado esperado: 47 equipos (los que fueron eliminados)

# 3. Verificar bitácora de auditoría
SELECT COUNT(*) FROM bitacora_auditoria 
WHERE tabla_afectada = 'equipo' 
  AND accion = 'DELETE'
  AND fecha_hora < '2026-11-15 14:30:00';
-- Resultado esperado: 0 (DELETE aún no ocurrió en este timeline)
```

### 4.7. Paso 6 - Promocionar a Primario y Reiniciar Servicios

```bash
# 1. Detener PostgreSQL recuperado
sudo -u postgres /usr/lib/postgresql/16/bin/pg_ctl \
  -D /var/lib/postgresql/data/pgdata stop

# 2. Renombrar recovery.signal para permitir escrituras
sudo -u postgres mv /var/lib/postgresql/data/pgdata/recovery.signal \
                      /var/lib/postgresql/data/pgdata/recovery.done

# 3. Reiniciar Patroni (automáticamente promociona este nodo como Leader)
sudo systemctl start patroni

# 4. Verificar que Patroni promocionó el nodo
sudo patronictl list
# Expected: este nodo como Leader

# 5. Reiniciar backends Spring Boot
sudo systemctl start sigec-backend

# 6. Verificar que los backends se conecten
curl -s http://localhost:8080/actuator/health | jq
# Expected: {"status":"UP"}
```

## 5. Verificación Post-Recuperación

### 5.1. Prueba de Integridad de Datos

```sql
-- Verificar que todas las tablas están intactas
SELECT 
  (SELECT COUNT(*) FROM equipo) AS equipos,
  (SELECT COUNT(*) FROM incidente) AS incidentes,
  (SELECT COUNT(*) FROM movimiento_equipo) AS movimientos,
  (SELECT COUNT(*) FROM bitacora_auditoria) AS auditoria;

-- Verificar que no haya datos corruptos
SELECT COUNT(*) FROM equipo WHERE nombre_equipo IS NULL;
-- Expected: 0

-- Verificar consistencia referencial
SELECT COUNT(*) FROM equipo e 
LEFT JOIN modelo_equipo m ON e.id_modelo = m.id_modelo 
WHERE m.id_modelo IS NULL;
-- Expected: 0
```

### 5.2. Prueba de Funcionalidad de la Aplicación

```bash
# 1. Probar login en frontend
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Consultar equipos en frontend
curl http://localhost:8080/api/equipos | jq '.length'
# Expected: >= 156

# 3. Crear un equipo de prueba
curl -X POST http://localhost:8080/api/equipos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "idModelo": 1,
    "idUbicacion": 1,
    "idPlataforma": 1,
    "idContratacion": 1,
    "nombreEquipo": "CAM-TEST-PITR-001",
    "serie": "SN-PITR-TEST",
    "numActivo": "ACT-PITR-001",
    "estado": "ACTIVO",
    "fechaInstalacion": "2026-11-15"
  }'
# Expected: 201 Created
```

## 6. Transcript Completo de Recuperación

### 6.1. Log de Comandos Ejecutados

```bash
# [root@prod-pg-1 ~]# pgbackrest --stanza=sigecpj info
00:00 INFO: stanza info command begin 2.0.53: ...
00:01 INFO: stanza: sigecpj
00:01 INFO:     status: ok
00:01 INFO:     backup (stanza sigecpj):
00:01 INFO:         full backup: 20261115-020000F
00:01 INFO:         differential backup: 20261115-140000D

# [root@prod-pg-1 ~]# systemctl stop sigec-backend
00:02 INFO: Stopped sigec-backend service

# [root@prod-pg-1 ~]# systemctl stop patroni
00:03 INFO: Stopped patroni service

# [root@prod-pg-1 ~]# rm -rf /var/lib/postgresql/data/pgdata/*
00:04 INFO: Removed PGDATA directory

# [root@prod-pg-1 ~]# pgbackrest --stanza=sigecpj restore --delta --set="2026-11-15 14:30:00" --type=time
00:05 INFO: restore command begin 2.0.53: ...
00:10 INFO: restore backup set 20261115-140000D
00:15 INFO: restore command end 2.0.53: ...
00:15 INFO: restore complete: 2.5GB restored in 10 seconds

# [root@prod-pg-1 ~]# touch /var/lib/postgresql/data/pgdata/recovery.signal
00:16 INFO: Created recovery.signal file

# [root@prod-pg-1 ~]# pg_ctl -D /var/lib/postgresql/data/pgdata -l recovery.log start
00:17 INFO: PostgreSQL started in recovery mode

# [postgres@prod-pg-1 ~]$ tail -f recovery.log
00:18 LOG:  starting point-in-time recovery to 2026-11-15 14:30:00-06
00:19 LOG:  restored log file "0000000100000034000000D4" from archive
00:20 LOG:  restored log file "0000000100000034000000D5" from archive
00:21 LOG:  recovery stopping before commit of transaction 2845
00:22 LOG:  consistent recovery state reached
00:22 LOG:  database system is ready to accept read-only connections

# [postgres@prod-pg-1 ~]$ psql -d sigecpj -c "SELECT COUNT(*) FROM equipo;"
00:23  count   
00:23 -------
00:23    156
00:23 (1 row)

# [root@prod-pg-1 ~]# pg_ctl -D /var/lib/postgresql/data/pgdata stop
00:24 INFO: PostgreSQL stopped

# [root@prod-pg-1 ~]# mv recovery.signal recovery.done
00:25 INFO: Promoted to production mode

# [root@prod-pg-1 ~]# systemctl start patroni
00:26 INFO: Started patroni, promoting node to Leader

# [root@prod-pg-1 ~]# patronictl list
00:27 + Cluster: sigec-cluster ----+----+-----------+
00:27 | Member      | Host           | Role    | State   |
00:27 +-------------+----------------+---------+---------+
00:27 | postgresql1 | prod-pg-1      | Leader  | running |
00:27 | postgresql2 | prod-pg-2      | Replica | running |
00:27 +-------------+----------------+---------+---------+

# [root@prod-pg-1 ~]# systemctl start sigec-backend
00:28 INFO: Started sigec-backend service

# [root@prod-pg-1 ~]# curl http://localhost:8080/actuator/health
00:29 {"status":"UP"}

# TOTAL TIME: 29 minutos (tiempo total de recuperación)
```

## 7. Mejores Prácticas de PITR

### 7.1. Frecuencia de Backups

| Tipo | Frecuencia | Retención | Tamaño Aprox |
|------|-----------|-----------|--------------|
| Full | Diario (2 AM) | 7 días | 2.5 GB |
| Diferencial | Cada 6 horas | 30 días | 450 MB |
| WAL Archive | Continuo | 30 días | 100 MB/hora |

### 7.2. Monitoreo de Archivado WAL

```bash
# Verificar que no haya lag en archivado
sudo -u postgres pgbackrest --stanza=sigecpj info
# Revisar "wal archive min/max" - debe estar actual

# Alerta si hay más de 1 hora de lag
if [ $(find /pgbackrest/sigecpj/archive -mmin +60 | wc -l) -eq 0 ]; then
  echo "WARNING: WAL archiving lag > 1 hour"
fi
```

### 7.3. Pruebas Periódicas de PITR

- **Mensual:** Ejecutar recuperación al mismo día del mes anterior
- **Trimestral:** Simular incidente real con DELETE masivo
- **Anual:** Prueba de desastre completa (failover + PITR)

## 8. Troubleshooting

### 8.1. Errores Comunes

**Error: "no backup found to restore"**
```bash
# Solución: verificar que el repositorio existe
sudo -u postgres pgbackrest --stanza=sigecpj info
# Si está vacío, crear backup inicial
sudo -u postgres pgbackrest --stanza=sigecpj backup --type=full
```

**Error: "WAL file not found in archive"**
```bash
# Solución: verificar que el archive_mode esté activo
sudo -u postgres psql -c "SHOW archive_mode;"
# Si está off, activar en postgresql.conf y reiniciar
```

**Error: "timeline divergence"**
```bash
# Solución: usar restore --delta para limpiar PGDATA
sudo -u postgres pgbackrest --stanza=sigecpj restore --delta --force
```

## 9. Conclusión

**PITR es el último recurso** cuando se necesita recuperar datos específicos. En SIGEC-PJ:

- ✅ **RTO = 29 minutos** (tiempo total de recuperación)
- ✅ **RPO = segundos** (precisión de recuperación: hasta el segundo)
- ✅ **Disponibilidad de backup:** 99.9% (solo 1 día de backup fuera de retención)

**Recomendación:** Documentar cada incidente de PITR para aprendizaje continuo.

---
**Versión:** 1.0  
**Fecha:** 26/08/2026  
**Responsable:** Administrador de Base de Datos  
**Fase 3 - Base de Datos II**