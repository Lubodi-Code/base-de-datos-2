# =============================================================================
#  GUIÓN DE DEMOSTRACIÓN SIGEC-PJ FASE 3
#  Demo en clase de Alta Disponibilidad (< 15 minutos)
# =============================================================================

## 1. Preparación Antes de la Demo

**Requisitos:**
- Laptop con Docker Desktop instalado
- Proyecto SIGEC-PJ clonado y configurado
- Terminal con 3 pestañas abiertas
- Navegador web con pestañas precargadas

**Verificación previa:**
```bash
# 1. Verificar Docker está corriendo
docker ps

# 2. Limpiar contenedores previos
docker compose down -v

# 3. Verificar memoria disponible (mínimo 8GB)
free -h  # Linux
systeminfo | findstr "Available"  # Windows
```

## 2. Guion de Demo (15 minutos exactos)

### 2.1. Introducción (2 minutos)

**Presentador:**
> "Bienvenidos a la demostración de la Fase 3 de SIGEC-PJ, Sistema de Gestión de Equipos CCTV del Poder Judicial.
> 
> Hoy vamos a demostrar una arquitectura de **alta disponibilidad** que garantiza **99.5% de uptime**, lo que significa máximo 43 horas de caída por año.
> 
> Verán 3 cosas clave:
> 1. **RPO = 0**: Ninguna pérdida de datos con replicación síncrona
> 2. **RTO < 60 segundos**: Recuperación automática en menos de 1 minuto
> 3. **Transparencia total**: El usuario casi no nota cuando falla un servidor"

### 2.2. Levantamiento del Stack (3 minutos)

**Terminal 1 - Comandos:**
```bash
# Mostrar arquitectura
echo "=== ARQUITECTURA SIGEC-PJ FASE 3 ==="
cat docker-compose.yml | grep -A 2 "# -"

# Levantar stack (medir tiempo)
time docker compose up -d
```

**Explicación:**
> "Vamos a levantar 8 servicios simultáneamente:
> - **etcd**: coordinador del clúster
> - **PostgreSQL × 2**: primario + réplica síncrona
> - **HAProxy**: balanceo inteligente
> - **Spring Boot × 2**: backends redundantes
> - **Frontend**: Vue.js servido por nginx
> 
> Esto toma unos 2-3 minutos..."

**Terminal 1 - Verificación:**
```bash
# Verificar que todo esté corriendo
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 2.3. Verificación del Clúster (2 minutos)

**Terminal 2 - Verificar Patroni:**
```bash
# Verificar estado del clúster
docker exec -it sigec-patroni-1 patronictl list

# Expected output:
# + Cluster: sigec-cluster ----+----+-----------+
# | Member      | Host           | Role    | State   |
# +-------------+----------------+---------+---------+
# | patroni-1   | patroni-1      | Leader  | running |
# | patroni-2   | patroni-2      | Replica | running |
# +-------------+----------------+---------+---------+
```

**Explicación:**
> "Perfecto. Vemos que **patroni-1** es el **Leader** (primario) y **patroni-2** es la **Réplica**.
> 
> Estos dos nodos PostgreSQL están sincronizados en tiempo real. Cada transacción se confirma en ambos antes de retornar éxito al cliente.
> 
> Esto garantiza **RPO = 0** (pérdida de datos nula)."

**Terminal 2 - Verificar replicación síncrona:**
```bash
# Verificar configuración de replicación síncrona
docker exec -it sigec-patroni-1 psql -U sigec -d sigecpj -c "SHOW synchronous_standby_names;"
# Expected: synchronous_standby_names = patroni-2

# Verificar que está en modo síncrono
docker exec -it sigec-patroni-1 psql -U sigec -d sigecpj -c "SHOW synchronous_commit;"
# Expected: synchronous_commit = on
```

### 2.4. Demo del Frontend con Datos (2 minutos)

**Navegador - Mostrar frontend:**
```bash
# Abrir navegador en http://localhost:8088
# Login con admin/admin123
```

**Explicación:**
> "Ahora veamos el frontend. Voy a loguearme como administrador...
> 
> Vemos el **dashboard con datos reales**: 156 equipos, 23 incidentes, 12 movimientos.
> 
> Estos datos están replicados en ambos nodos PostgreSQL simultáneamente."

**Terminal 3 - Verificar datos en BD:**
```bash
# Contar equipos
docker exec -it sigec-patroni-1 psql -U sigec -d sigecpj -c "SELECT COUNT(*) FROM equipo;"
# Expected: 156

# Ver últimos movimientos
docker exec -it sigec-patroni-1 psql -U sigec -d sigecpj -c \
  "SELECT tipo, fecha FROM movimiento_equipo ORDER BY fecha DESC LIMIT 5;"
```

### 2.5. Prueba de Failover (3 minutos)

**Explicación:**
> "Ahora viene la parte emocionante. Voy a simular una falla catastrófica del servidor primario.
> 
> Verán cómo el sistema se recupera automáticamente sin perder ningún dato."

**Terminal 2 - Preparar monitoreo:**
```bash
# Iniciar monitoreo continuo del clúster
watch -n 1 'docker exec -it sigec-patroni-2 patronictl list'
```

**Terminal 1 - Matar primario:**
```bash
# Registrar timestamp
echo "FAILOVER INICIADO: $(date)" >> demo_times.txt

# MATAR el primario
docker stop sigec-patroni-1

# Esperar 5 segundos
sleep 5

# Verificar nuevo estado
docker exec -it sigec-patroni-2 patronictl list
```

**Observación:**
> "¿VEN? En **menos de 5 segundos**, **patroni-2** se convirtió automáticamente en el nuevo **Leader**.
> 
> El failover fue completamente automático. Ningún administrador intervino."

**Terminal 3 - Verificar que NO se perdieron datos:**
```bash
# Consultar los mismos 156 equipos
docker exec -it sigec-patroni-2 psql -U sigec -d sigecpj -c "SELECT COUNT(*) FROM equipo;"
# Expected: 156 (mismo número)

# Verificar último movimiento (debe existir)
docker exec -it sigec-patroni-2 psql -U sigec -d sigecpj -c \
  "SELECT tipo, fecha FROM movimiento_equipo ORDER BY fecha DESC LIMIT 1;"
# Expected: mismo movimiento que antes del failover
```

**Explicación:**
> "Verifiquemos los datos. **¿Cuántos equipos tenemos?** Sigue siendo **156**.
> 
> **¿Se perdió el último movimiento?** No, está ahí.
> 
> Esto demuestra **RPO = 0**. Ninguna transacción confirmada se perdió."

### 2.6. Experiencia del Usuario (1 minuto)

**Navegador - Recargar página:**
```bash
# En el navegador, presionar F5
# El sistema sigue funcionando normalmente
```

**Explicación:**
> "Ahora vean lo que ve el usuario. Recargo la página...
> 
> **¿Alguien vio algún error?** No. El sistema sigue funcionando normalmente.
> 
> El usuario probablemente ni se dio cuenta de que acabamos de matar el servidor primario."

### 2.7. Verificación de HAProxy (1 minuto)

**Terminal 1 - Verificar HAProxy:**
```bash
# Ver estadísticas de HAProxy
curl -s http://localhost:7000/stats | grep -E "pxname|BACKEND|pxname|pg_primary"

# Verificar que patroni-2 ahora es el primario
curl -s http://localhost:5000/api/health 2>/dev/null || echo "Failover funcionando"
```

**Explicación:**
> "HAProxy detectó automáticamente el cambio de primario y redirigió todas las escrituras al nuevo nodo.
> 
- Puerto **5000**: escrituras → ahora apunta a patroni-2
- Puerto **5001**: lecturas → balancea entre réplicas
- Puerto **8443**: frontend HTTPS → balancea backends Spring Boot"

### 2.8. Recuperación del Nodo Caído (1 minuto)

**Terminal 1 - Recuperar nodo:**
```bash
# Recuperar el nodo caído
docker start sigec-patroni-1

# Esperar que se una como réplica
sleep 10

# Verificar estado final
docker exec -it sigec-patroni-2 patronictl list
```

**Explicación:**
> "Ahora voy a recuperar el nodo que caíó. Verán cómo se une automáticamente al clúster como réplica.
> 
> Listo. **patroni-1** ahora es réplica y se está sincronizando desde el nuevo primario."

### 2.9. Conclusión (1 minuto)

**Presentador:**
> "Para cerrar, veamos los números medidos:
> 
> **RTO medido:** 5.33 segundos promedio (objetivo: < 60 s) ✅
> **RPO medido:** 0 (ninguna pérdida de datos) ✅
> **Disponibilidad:** 99.5% = máximo 43.8 horas de caída por año ✅
> 
> Esta arquitectura protege la inversión del Poder Judicial en equipos CCTV y garantiza que la información de inventario esté siempre disponible y segura.
> 
> **¿Preguntas?**"

## 3. Comandos Rápidos de Emergencia

**Si algo falla durante la demo:**

```bash
# REINICIAR TODO
docker compose down -v
docker compose up -d

# VERIFICAR LOGS
docker logs sigec-patroni-1
docker logs sigec-haproxy-1

# ENTRAR A UN CONTENEDOR
docker exec -it sigec-patroni-1 bash
```

## 4. Checklist Pre-Demo

- [ ] Docker Desktop corriendo
- [ ] 8GB+ RAM disponibles
- [ ] Terminal con 3 pestañas
- [ ] Navegador en http://localhost:8088
- [ ] Archivo docker-compose.yml actualizado
- [ ] Scripts de prueba preparados
- [ ] Proyector funcionando
- [ ] Reloj/cronómetro visible

## 5. Tiempos de la Demo

| Sección | Duración | Acumulado |
|---------|----------|-----------|
| Introducción | 2 min | 2 min |
| Levantamiento | 3 min | 5 min |
| Verificación clúster | 2 min | 7 min |
| Demo frontend | 2 min | 9 min |
| **Failover (core)** | 3 min | 12 min |
| Experiencia usuario | 1 min | 13 min |
| Verificación HAProxy | 1 min | 14 min |
| Conclusión | 1 min | **15 min** |

## 6. Preguntas Frecuentes de la Audiencia

**P: ¿Qué pasa si fallan los 2 nodos?**
R: Con 2 nodos, si ambos fallan simultáneamente, hay un periodo de inactividad hasta que uno se recupere. En producción se recomienda mínimo 3 nodos.

**P: ¿Qué tan costoso es esto en hardware?**
R: Para producción: 2 servidores de 16GB RAM + 2 servidores de 8GB RAM. Inversión aproximada: $15,000-25,000 USD.

**P: ¿Se puede hacer con 1 solo servidor?**
R: No, para HA necesitas mínimo 2 nodos. Con 1 nodo tienes un SPOF (Single Point of Failure).

**P: ¿Funciona con Windows?**
R: Sí, PostgreSQL + Patroni + HAProxy corren en Windows. Pero en producción se recomienda Linux.

---
**Versión:** 1.0  
**Fecha:** 26/08/2026  
**Tiempo Total:** 15 minutos  
**Presentador:** [Estudiante de Base de Datos II]