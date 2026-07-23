# SIGEC-PJ — Base de datos (`db/`)

PostgreSQL 16. Scripts del esquema y datos de demostración.

| Archivo | Propósito |
|---------|-----------|
| [`sigec_ddl.sql`](sigec_ddl.sql) | Esquema completo (tablas, vistas, triggers de auditoría). Reconstrucción idempotente (drop + create). |
| [`sigec_seed.sql`](sigec_seed.sql) | Datos semilla: catálogos (provincias, edificios, modelos, plataformas, técnicos…) y equipos de ejemplo. Idempotente (`ON CONFLICT DO NOTHING`). |
| [`diagrama_er.mmd`](diagrama_er.mmd) | Diagrama entidad-relación (Mermaid, notación pata de gallo). |
| [`CORRECCIONES.md`](CORRECCIONES.md) | Mapeo de la retroalimentación del Avance 1 a los cambios aplicados. |

## Orden de aplicación

El esquema **debe** crearse antes que los datos semilla:

```bash
psql -U postgres -d sigecpj -f db/sigec_ddl.sql    # 1. estructura
psql -U postgres -d sigecpj -f db/sigec_seed.sql   # 2. datos de demo (opcional)
```

> Con `docker compose up` esto es automático: ambos scripts se montan en
> `/docker-entrypoint-initdb.d` (con prefijos `01_`/`02_`) y Postgres los ejecuta en
> orden al inicializar un volumen de datos vacío.

El backend crea además el usuario `admin / admin123` al primer arranque (ver
[`backend/`](../backend)); el seed **no** incluye usuarios del sistema.
