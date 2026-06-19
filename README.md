# 🎥 SIGEC-PJ · Sistema de Gestión de Equipos CCTV del Poder Judicial

Proyecto de la materia **Base de Datos II** — Fase 2. Sistema para inventariar,
auditar y dar trazabilidad a los equipos de videovigilancia (CCTV) del Poder
Judicial: equipos, ubicaciones, credenciales cifradas, incidentes y movimientos,
con auditoría a nivel de base de datos.

> El diseño de la Fase 2 incorpora la retroalimentación del Avance 1
> (Prof. Julio César Sánchez). El detalle de cada corrección está en
> [`db/CORRECCIONES.md`](db/CORRECCIONES.md).

---

## 📦 Estructura del repositorio

| Carpeta | Contenido |
|---------|-----------|
| [`db/`](db/) | Diseño de la base de datos: DDL de PostgreSQL, diagrama E-R y bitácora de correcciones. |
| [`backend/`](backend/) | API REST en Java 21 + Spring Boot 3.3 (arquitectura por capas y módulos). |

```
.
├── db/
│   ├── sigec_ddl.sql      # Esquema completo (PostgreSQL 16): tablas, vistas, triggers
│   ├── diagrama_er.mmd    # Diagrama E-R en notación pata de gallo (Mermaid)
│   └── CORRECCIONES.md    # Mapeo retroalimentación Avance 1 → cambios aplicados
└── backend/
    ├── pom.xml
    └── src/main/java/cr/poderjudicial/sigec/
        ├── catalogo/      # Catálogos: provincia, cantón, edificio, ubicación, modelo…
        ├── inventario/    # Núcleo: equipos, interfaces de red, credenciales, garantías
        ├── seguridad/     # Usuarios, roles, login
        ├── security/      # JWT, cifrado AES-GCM, filtros de autenticación
        ├── audit/         # Aspecto de auditoría (propaga el usuario a PostgreSQL)
        ├── config/        # Seguridad, OpenAPI, datos iniciales
        └── common/        # Manejo global de errores
```

---

## 🗄️ Base de datos (`db/`)

PostgreSQL 16. El script [`db/sigec_ddl.sql`](db/sigec_ddl.sql) reconstruye el
esquema completo de forma idempotente (drop + create), con:

- **Catálogos geográficos y técnicos:** provincia, cantón, edificio, ubicación
  física, modelo de equipo, plataforma VMS, proveedor, contratación, técnico.
- **Núcleo de inventario:** `equipo`, `interfaz_red` (tipos `inet`/`macaddr`),
  `credencial_equipo` (secretos cifrados), e `incidente` / `movimiento_equipo`.
- **Seguridad y auditoría:** `usuario_sistema`, `bitacora_auditoria` (con
  `datos_antes` / `datos_despues` en `JSONB`) y vista `v_garantias_por_vencer`.
- **Triggers:** `fn_auditoria()` registra **quién** hizo cada cambio leyendo el
  parámetro de sesión `app.id_usuario`; `fn_touch_actualizado()` mantiene
  la marca de tiempo de modificación.

Aplicarlo:

```bash
psql -U postgres -d sigecpj -f db/sigec_ddl.sql
```

**Correcciones clave del Avance 1** (ver [`db/CORRECCIONES.md`](db/CORRECCIONES.md)):

1. No quedan tablas "flotando": `usuario_sistema` y `bitacora_auditoria` se
   integran al grafo relacional.
2. Trazabilidad de autoría: `id_usuario` / `id_usuario_registro` en tablas sensibles.
3. Diagrama E-R reescrito en notación pata de gallo (crow's foot).
4. La bitácora indica `tabla_afectada` + `id_registro` por cada cambio.

---

## ⚙️ Backend (`backend/`)

API REST de gestión de equipos CCTV. Arquitectura **por capas**
(controlador → servicio → repositorio → entidad), organizada por módulos
funcionales. Detalle ampliado en [`backend/README.md`](backend/README.md).

### Tecnologías

- **Java 21** · **Spring Boot 3.3.5**
- Spring Web · Data JPA · Security · Validation · AOP · Actuator
- **PostgreSQL** (driver runtime)
- **JWT** (jjwt, HS256) · **OpenAPI / Swagger** (springdoc)

### Funcionalidad implementada (Fase 2, primera entrega)

- **Seguridad:** login JWT, roles `ADMIN` / `TECNICO` / `CONSULTA`, contraseñas
  con BCrypt y `@PreAuthorize` por endpoint.
- **Auditoría:** aspecto que fija `app.id_usuario` en la sesión de PostgreSQL
  para que los triggers registren la autoría de cada cambio.
- **Cifrado:** `AesGcmCipherService` (AES-256-GCM) para los secretos de equipos.
- **Catálogos:** endpoints de lectura para poblar selectores.
- **Inventario:** CRUD de equipos, búsqueda paginada (estado / provincia / texto
  / IP), interfaces de red, credenciales cifradas y vista de garantías por vencer.
- **Incidentes:** flujo ITIL (`ABIERTO` / `PROCESO` / `CERRADO`) con asignación de
  técnico, cierre con fecha de reparación y trazabilidad por equipo.
- **Movimientos:** bajas, reemplazos y traslados que ajustan el estado del equipo y
  registran al usuario autor.
- **Dashboard:** endpoints REST de indicadores (equipos por estado/provincia,
  incidentes abiertos y por estado, garantías por vencer, tiempo medio de reparación).

> Pendiente en próximas pasadas: el **frontend Vue 3**, el script de migración
> ODS → PostgreSQL y el montaje/guía del clúster de alta disponibilidad.

### Requisitos

- JDK 21
- PostgreSQL 16 con el esquema de [`db/sigec_ddl.sql`](db/sigec_ddl.sql) aplicado.

### Ejecutar

```bash
cd backend

# Variables (opcional; hay valores de desarrollo por defecto)
export DB_URL=jdbc:postgresql://localhost:5432/sigecpj
export DB_USER=postgres
export DB_PASSWORD=postgres

./mvnw spring-boot:run     # o: mvn spring-boot:run
```

Al primer arranque se crea el usuario `admin / admin123` (cambiar en producción;
desactivar con `sigec.bootstrap-admin=false`).

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

### Autenticación rápida

```bash
# Login -> token
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# Usar el token
curl -s http://localhost:8080/api/v1/equipos \
  -H "Authorization: Bearer <token>"
```

> ⚠️ **Configuración sensible:** `JWT_SECRET` y `AES_KEY` (Base64) tienen valores
> **solo de desarrollo** en `application.yml`. En producción deben inyectarse por
> variables de entorno / KMS.

---

## 🤝 Flujo de trabajo colaborativo

1. Trabajar siempre en una rama propia, **nunca directo en `main`**.
2. Todo cambio entra por **Pull Request** con al menos 1 revisión.
3. Documentar cada script y módulo explicando su propósito.

```bash
git checkout -b feature/nombre-de-tu-tarea
git add .
git commit -m "feat: descripción de lo que hiciste"
git push origin feature/nombre-de-tu-tarea
```
