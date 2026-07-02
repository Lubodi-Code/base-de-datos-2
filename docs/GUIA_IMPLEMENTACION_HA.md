# =============================================================================
#  GUÍA DE IMPLEMENTACIÓN DE ALTA DISPONIBILIDAD SIGEC-PJ
#  Dirigida al encargado de TI del Poder Judicial
# =============================================================================

## 1. Resumen Ejecutivo

Esta guía traduce la arquitectura de demostración (Docker Compose) a una implementación física en producción para el sistema SIGEC-PJ, garantizando una disponibilidad ≥ 99.5%.

### Objetivos de HA
- **RPO = 0**: Pérdida de datos nula (replicación síncrona)
- **RTO < 60 s**: Tiempo máximo de recuperación ante fallos
- **99.5% de disponibilidad**: Máximo 43.8 horas de caída por año

## 2. Topología de Producción Recomendada

### 2.1. Infraestructura Física Mínima

```
┌─────────────────────────────────────────────────────────────────┐
│                    Data Center Principal                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ PostgreSQL-1 │  │ PostgreSQL-2 │  │   etcd (3)   │          │
│  │  (Primario) │  │  (Réplica)   │  │   Coordinador│          │
│  │ 16GB RAM    │  │  16GB RAM    │  │   4GB RAM    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  HAProxy-1   │  │   Spring-1   │  │   Spring-2   │          │
│  │  (Balanceo)  │  │   (Backend)  │  │   (Backend)  │          │
│  │   4GB RAM    │  │   8GB RAM    │  │   8GB RAM    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    Data Center Secundario (DR)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ PostgreSQL-3 │  │  HAProxy-2   │  │  pgBackRest   │          │
│  │ (Réplica DR) │  │   (Balanceo)  │  │   (Backups)   │          │
│  │  16GB RAM    │  │   4GB RAM    │  │   8TB Disco   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2. Requisitos de Hardware por Servidor

| Rol | CPU | RAM | Disco | Red | Cantidad |
|-----|-----|-----|-------|-----|----------|
| PostgreSQL | 4 cores | 16GB | 500GB SSD RAID 10 | 10Gbps | 2 |
| etcd (cluster) | 2 cores | 4GB | 100GB SSD | 1Gbps | 3 |
| HAProxy | 2 cores | 4GB | 50GB SSD | 10Gbps | 2 |
| Spring Boot | 4 cores | 8GB | 100GB SSD | 1Gbps | 2 |
| pgBackRest | 2 cores | 8GB | 8TB SATA RAID 6 | 1Gbps | 1 |

### 2.3. Requisitos de Energía y Redundancia

**Suministro Eléctrico:**
- **UPS** por cada servidor (mínimo 1500VA, 30min autonomía)
- **Generador eléctrico** con capacidad para 100% de la carga
- **PDU** redundantes (A+B feeds)

**Red:**
- **Switches** redundantes (dos switches core 48-port 10G)
- **Cables** redundantes a cada servidor
- **VLANs** separadas para datos, backups y monitoreo

## 3. Instalación Paso a Paso

### 3.1. Preparación del Sistema Operativo

**Sistema Recomendado:** Ubuntu Server 22.04 LTS o Rocky Linux 9

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Configurar timezone Costa Rica
sudo timedatectl set-timezone America/Costa_Rica

# Optimizar kernel para PostgreSQL
echo 'kernel.shmmax=68719476736' | sudo tee -a /etc/sysctl.conf
echo 'kernel.shmall=4294967296' | sudo tee -a /etc/sysctl.conf
echo 'vm.swappiness=1' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

### 3.2. Instalación del Clúster PostgreSQL + Patroni

**En nodo 1 (192.168.1.10):**
```bash
# Instalar PostgreSQL 16 y Patroni
sudo apt install postgresql-16 postgresql-16-patroni etcd-client -y

# Configurar Patroni (similar a patroni-1.yml del compose)
sudo cp patroni-1.yml /etc/patroni/patroni.yml
sudo systemctl enable patroni
sudo systemctl start patroni
```

**En nodo 2 (192.168.1.11):**
```bash
# Instalar PostgreSQL 16 y Patroni
sudo apt install postgresql-16 postgresql-16-patroni etcd-client -y

# Configurar Patroni (similar a patroni-2.yml del compose)
sudo cp patroni-2.yml /etc/patroni/patroni.yml
sudo systemctl enable patroni
sudo systemctl start patroni
```

**Verificar clúster:**
```bash
# En cualquier nodo
sudo patronictl list
# Debería mostrar:
# + Cluster: sigec-cluster ----+----+-----------+
# | Member      | Host           | Role    | State   |
# +-------------+----------------+---------+---------+
# | postgresql1 | 192.168.1.10   | Leader  | running |
# | postgresql2 | 192.168.1.11   | Replica | running |
# +-------------+----------------+---------+---------+
```

### 3.3. Instalación de HAProxy

**En ambos nodos HAProxy (192.168.1.20, 192.168.1.21):**
```bash
# Instalar HAProxy
sudo apt install haproxy openssl -y

# Generar certificados TLS 1.3 de producción (NO autofirmados)
sudo openssl req -x509 -newkey rsa:4096 -keyout /etc/haproxy/certs/key.pem \
  -out /etc/haproxy/certs/cert.pem -days 365 -nodes \
  -subj "/CN=sigec.poderjudicial.go.cr"

# Copiar configuración y ajustar IPs
sudo cp haproxy.cfg /etc/haproxy/haproxy.cfg
sudo systemctl enable haproxy
sudo systemctl start haproxy

# Verificar que esté escuchando
sudo netstat -tlnp | grep haproxy
# Debería mostrar puertos 5000, 5001, 8443
```

### 3.4. Instalación de Backend Spring Boot

**En ambos nodos (192.168.1.30, 192.168.1.31):**
```bash
# Instalar Java 21
sudo apt install openjdk-21-jre -y

# Crear usuario del servicio
sudo useradd -r -s /bin/false sigec

# Desplegar JAR
sudo mkdir /opt/sigec
sudo cp sigec-backend.jar /opt/sigec/
sudo chown -R sigec:sigec /opt/sigec

# Crear systemd service
sudo tee /etc/systemd/system/sigec-backend.service > /dev/null <<EOF
[Unit]
Description=SIGEC-PJ Backend API
After=network.target haproxy.service

[Service]
User=sigec
ExecStart=/usr/bin/java -jar /opt/sigec/sigec-backend.jar
Environment="DB_URL=jdbc:postgresql://192.168.1.20:5000/sigecpj"
Environment="DB_USER=sigec"
Environment="DB_PASSWORD=<contraseña segura>"
Environment="JWT_SECRET=<clave HS256 segura>"
Environment="AES_KEY=<clave AES segura>"
Restart=always

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl enable sigec-backend
sudo systemctl start sigec-backend
```

### 3.5. Instalación de pgBackRest

**En el servidor de backups (192.168.1.40):**
```bash
# Instalar pgBackRest
sudo apt install pgbackrest -y

# Crear repositorio
sudo mkdir -p /pgbackrest/sigecpj
sudo chown postgres:postgres /pgbackrest

# Copiar configuración
sudo cp pgbackrest.conf /etc/pgbackrest/pgbackrest.conf

# Primer backup full
sudo -u postgres pgbackrest --stanza=sigecpj backup --type=full

# Configurar cron para backups automáticos
sudo tee /etc/cron.d/pgbackrest > /dev/null <<EOF
# Backup full diario a las 2 AM
0 2 * * * postgres pgbackrest --stanza=sigecpj backup --type=full

# Backup diferencial cada 6 horas
0 */6 * * * postgres pgbackrest --stanza=sigecpj backup --type=diff

# Verificación de integridad semanal
0 3 * * 0 postgres pgbackrest --stanza=sigecpj check
EOF
```

## 4. Procedimientos Operativos

### 4.1. Monitoreo de Salud del Clúster

```bash
# Script para verificar salud del clúster
#!/bin/bash
echo "=== ESTADO DEL CLÚSTER SIGEC-PJ ==="
echo ""

# Estado de Patroni
echo "1. Estado Patroni:"
patronictl list

echo ""
echo "2. Salud de HAProxy:"
curl -s http://192.168.1.20:7000/stats | grep -E "pxname|BACKEND"

echo ""
echo "3. Backends Spring Boot:"
curl -s http://192.168.1.30:8080/actuator/health | jq
curl -s http://192.168.1.31:8080/actuator/health | jq

echo ""
echo "4. Último backup pgBackRest:"
sudo -u postgres pgbackrest --stanza=sigecpj info
```

### 4.2. Recuperación ante Desastres (PITR)

**Escenario:** Un DELETE masivo accidental a las 14:32 del 15/11/2026

```bash
# 1. Detener aplicaciones
sudo systemctl stop sigec-backend

# 2. Recuperar con PITR al momento anterior al error
sudo -u postgres pgbackrest --stanza=sigecpj restore \
  --delta \
  --set="2026-11-15 14:30:00"

# 3. Iniciar PostgreSQL recuperado
sudo systemctl start postgresql

# 4. Verificar datos recuperados
psql -U sigec -d sigecpj -c "SELECT COUNT(*) FROM equipo;"

# 5. Reiniciar aplicaciones
sudo systemctl start sigec-backend
```

## 5. Contacto y Soporte

**Soporte Técnico Interno:** 
- Administrador de Base de Datos: dba@poderjudicial.go.cr
- Equipo de Infraestructura: infra@poderjudicial.go.cr

**Referencias Técnicas:**
- Documentación Patroni: https://patroni.readthedocs.io/
- Documentación pgBackRest: https://pgbackrest.org/
- Documentación HAProxy: https://www.haproxy.org/

---
**Versión:** 1.0  
**Fecha:** 26/08/2026  
**Elaborado por:** Equipo de Base de Datos II - Universidad Politécnica Internacional