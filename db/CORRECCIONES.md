# Correcciones del Avance 1 → Diseño de la base de datos (Fase 2)

Mapeo entre la retroalimentación del Prof. Julio César Sánchez y los cambios
aplicados en [`sigec_ddl.sql`](./sigec_ddl.sql) y [`diagrama_er.mmd`](./diagrama_er.mmd).

## El error principal: tablas flotando (punto #1)

En el Avance 1, `USUARIO_SISTEMA` y `BITACORA_AUDITORIA` solo se conectaban
entre sí, formando una isla desconectada del resto del modelo ("dos bases de
datos en una"). Por norma ANSI, un diseño transaccional no debe dejar tablas
aisladas. **Corrección:** se integraron al grafo relacional principal.

| # | Observación del docente | Corrección aplicada |
|---|--------------------------|---------------------|
| **1** | `USUARIO_SISTEMA` y `BITACORA_AUDITORIA` flotan; impresión de "dos BD en una". | `USUARIO_SISTEMA` ahora es referenciada por `EQUIPO`, `INCIDENTE` y `MOVIMIENTO_EQUIPO` (autoría) y enlaza opcionalmente con `TECNICO`. `BITACORA_AUDITORIA` referencia a `USUARIO_SISTEMA`. Ninguna tabla queda aislada. |
| **2** | Falta saber **qué usuario** hizo cada cambio (esp. en `MOVIMIENTO_EQUIPO`). | `MOVIMIENTO_EQUIPO.id_usuario` **NOT NULL** (FK → `USUARIO_SISTEMA`). Además `EQUIPO.id_usuario_registro` e `INCIDENTE.id_usuario_registro`. Si un usuario actúa fuera de su rol, queda registrado exactamente qué tocó. |
| **3** | Cambiar las flechitas por notación formal E-R. | Diagrama reescrito en **pata de gallo (crow's foot)** con Mermaid (`diagrama_er.mmd`), legible para el cliente. |
| **4** | `datos_antes`/`datos_despues` no indican a qué registro/tabla apuntan. | `BITACORA_AUDITORIA` ahora tiene `tabla_afectada` + `id_registro`: cada entrada apunta a un registro concreto. `datos_antes`/`datos_despues` son `JSONB`. Poblada por `fn_auditoria()` (trigger). |

## ¿Cómo sabe la base de datos quién hizo el cambio?

El trigger `fn_auditoria()` lee el usuario autenticado desde un parámetro de
sesión que fija la capa de negocio (Spring Boot) al inicio de cada transacción:

```sql
SET LOCAL app.id_usuario = '<id_usuario>';   -- lo ejecuta el aplicativo
-- ... INSERT/UPDATE/DELETE ...
-- el trigger lee current_setting('app.id_usuario', true) y lo guarda en la bitácora
```

Así se cierra el bucle de trazabilidad **dentro** de la base de datos, incluso
para cambios hechos por triggers (cumple ISO/IEC 27001).

## Pendientes de la Fase 2 (no son del modelo de datos)

- **#5 — Métrica de disponibilidad on-premise.** Ajustar el 99% a la realidad:
  documentar ventanas de mantenimiento fuera de horario productivo y reconocer
  que durante el parcheo de un nodo se opera sin réplica (~93–95% real
  on-premise; 99,999% solo en nube multi-región).
- **#7 — Prueba de restauración del respaldo.** Definir el procedimiento de
  restore de pgBackRest + PITR en un nodo de laboratorio y certificarlo.
- **#9 — Forma.** Presentarse como equipo al inicio de la exposición.

## Tablas del modelo (16 = 14 de negocio + 2 de seguridad ya integradas)

`provincia`, `canton`, `edificio`, `ubicacion_fisica`, `plataforma_vms`,
`modelo_equipo`, `proveedor`, `contratacion`, `tecnico`, `equipo`,
`interfaz_red`, `credencial_equipo`, `incidente`, `movimiento_equipo`
+ `usuario_sistema`, `bitacora_auditoria`.
