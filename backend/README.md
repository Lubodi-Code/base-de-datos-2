# SIGEC-PJ — Backend (API REST)

API REST de gestión de equipos CCTV del Poder Judicial. Java 21 + Spring Boot 3.3,
arquitectura **por capas** (controlador → servicio → repositorio → entidad),
organizada por módulos funcionales.

## Estado (Fase 2, primera entrega del backend)

Implementado y compilando:

- **Base del proyecto**: Spring Boot 3.3, Spring Web, Data JPA, Security, Validation, AOP, Actuator, OpenAPI/Swagger.
- **Seguridad**: login JWT (HS256), roles `ADMIN` / `TECNICO` / `CONSULTA`, contraseñas con BCrypt, `@PreAuthorize` por endpoint.
- **Auditoría**: aspecto que fija `app.id_usuario` en la sesión de PostgreSQL para que los triggers `fn_auditoria` registren **quién** hizo cada cambio (correcciones #1, #2, #4 del Avance 1).
- **Cifrado**: `AesGcmCipherService` (AES-256-GCM) para los secretos de equipos (sustituye las credenciales en texto plano del ODS).
- **Catálogos**: endpoints de lectura para poblar selectores (provincias, cantones, edificios, ubicaciones, modelos, plataformas, contrataciones, técnicos).
- **Inventario (núcleo)**: CRUD de equipos, búsqueda paginada (estado / provincia / texto / IP), interfaces de red (`inet`/`macaddr`), credenciales cifradas y vista de garantías por vencer.
- **Incidentes**: registro de fallas con flujo de estados ITIL (`ABIERTO` / `PROCESO` / `CERRADO`), asignación de técnico, cierre con fecha de reparación y trazabilidad por equipo.
- **Movimientos**: bajas, reemplazos y traslados con motivo, técnico y equipo sustituto; cada movimiento ajusta el estado del equipo (BAJA → `RETIRADO`, REEMPLAZO → `REEMPLAZADO`) y registra el usuario autor.
- **Dashboard**: endpoints REST de indicadores (totales y equipos por estado/provincia, incidentes abiertos y por estado, garantías por vencer, tiempo medio de reparación).

Pendiente (siguientes pasadas): **frontend Vue 3**, script de migración ODS → PostgreSQL y montaje/guía del clúster de alta disponibilidad.

## Endpoints principales

| Recurso | Método | Ruta |
|---------|--------|------|
| Inventario | `GET/POST/PUT` | `/api/v1/equipos` |
| Incidentes | `GET/POST/PUT` | `/api/v1/incidentes` (filtros `estado`, `idEquipo`) |
| Cerrar incidente | `PATCH` | `/api/v1/incidentes/{id}/cerrar` |
| Movimientos | `GET/POST` | `/api/v1/movimientos` (filtro `idEquipo`) |
| Dashboard | `GET` | `/api/v1/dashboard/resumen`, `/equipos-por-provincia`, `/incidentes-por-estado` |

La escritura (`POST/PUT/PATCH`) requiere rol `ADMIN` o `TECNICO`; la lectura, cualquier usuario autenticado.

## Requisitos

- JDK 21 (o compilar con la imagen `maven:3.9-eclipse-temurin-21`).
- PostgreSQL 16 con el esquema de [`../db/sigec_ddl.sql`](../db/sigec_ddl.sql) aplicado.

## Ejecutar

```bash
# 1. Base de datos (esquema)
psql -U postgres -d sigecpj -f ../db/sigec_ddl.sql

# 2. Variables (opcional; hay valores de desarrollo por defecto)
export DB_URL=jdbc:postgresql://localhost:5432/sigecpj
export DB_USER=postgres
export DB_PASSWORD=postgres

# 3. Arrancar
./mvnw spring-boot:run     # o: mvn spring-boot:run
```

Al primer arranque se crea el usuario `admin / admin123` (cambiar en producción;
desactivar con `sigec.bootstrap-admin=false`).

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## Autenticación rápida

```bash
# Login -> token
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# Usar el token
curl -s http://localhost:8080/api/v1/equipos \
  -H "Authorization: Bearer <token>"
```

## Configuración sensible

`JWT_SECRET` y `AES_KEY` (Base64) tienen valores **solo de desarrollo** en
`application.yml`. En producción deben inyectarse por variables de entorno / KMS.
