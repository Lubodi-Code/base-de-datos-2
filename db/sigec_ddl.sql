-- =============================================================================
--  SIGEC-PJ  ·  Sistema de Gestión de Equipos CCTV del Poder Judicial
--  Script DDL de la base de datos propuesta (PostgreSQL 16)  ·  Apéndice A
--  Base de Datos II — Fase 2 (incorpora retroalimentación del Avance 1)
-- =============================================================================
--
--  CORRECCIONES DEL AVANCE 1 (retroalimentación del Prof. Julio César Sánchez)
--  ---------------------------------------------------------------------------
--  [#1] No hay tablas flotando: USUARIO_SISTEMA y BITACORA_AUDITORIA quedan
--       integradas al grafo relacional. USUARIO_SISTEMA es referenciada por
--       EQUIPO, INCIDENTE y MOVIMIENTO_EQUIPO (autoría) y opcionalmente enlaza
--       con TECNICO; BITACORA_AUDITORIA referencia a USUARIO_SISTEMA.
--  [#2] Trazabilidad de autoría: MOVIMIENTO_EQUIPO.id_usuario (NOT NULL) y, en
--       tablas sensibles, id_usuario_registro. Se sabe SIEMPRE quién tocó qué.
--  [#4] Auditoría funcional: BITACORA_AUDITORIA indica tabla_afectada +
--       id_registro, de modo que datos_antes / datos_despues (JSONB) apuntan a
--       un registro concreto. El usuario que ejecuta el cambio se propaga a los
--       triggers vía el parámetro de sesión app.id_usuario (ver fn_auditoria).
--
--  Convenciones: identificadores en minúscula y snake_case; valores de dominio
--  en MAYÚSCULA y ASCII (DANADO sin 'ñ') para evitar problemas de codificación.
-- =============================================================================

SET client_encoding = 'UTF8';

-- -----------------------------------------------------------------------------
-- 0. Reconstrucción limpia (ambiente de laboratorio / demo)
--    El orden inverso respeta las dependencias de llaves foráneas.
-- -----------------------------------------------------------------------------
DROP VIEW  IF EXISTS v_garantias_por_vencer       CASCADE;
DROP TABLE IF EXISTS bitacora_auditoria           CASCADE;
DROP TABLE IF EXISTS movimiento_equipo            CASCADE;
DROP TABLE IF EXISTS incidente                    CASCADE;
DROP TABLE IF EXISTS credencial_equipo            CASCADE;
DROP TABLE IF EXISTS interfaz_red                 CASCADE;
DROP TABLE IF EXISTS equipo                       CASCADE;
DROP TABLE IF EXISTS usuario_sistema              CASCADE;
DROP TABLE IF EXISTS tecnico                      CASCADE;
DROP TABLE IF EXISTS contratacion                 CASCADE;
DROP TABLE IF EXISTS proveedor                    CASCADE;
DROP TABLE IF EXISTS modelo_equipo                CASCADE;
DROP TABLE IF EXISTS plataforma_vms               CASCADE;
DROP TABLE IF EXISTS ubicacion_fisica             CASCADE;
DROP TABLE IF EXISTS edificio                     CASCADE;
DROP TABLE IF EXISTS canton                       CASCADE;
DROP TABLE IF EXISTS provincia                    CASCADE;
DROP FUNCTION IF EXISTS fn_auditoria()            CASCADE;
DROP FUNCTION IF EXISTS fn_touch_actualizado()    CASCADE;

-- =============================================================================
--  CATÁLOGOS GEOGRÁFICOS
-- =============================================================================

CREATE TABLE provincia (
    id_provincia  SMALLINT     PRIMARY KEY,
    nombre        VARCHAR(40)  NOT NULL UNIQUE
);

CREATE TABLE canton (
    id_canton     SERIAL       PRIMARY KEY,
    id_provincia  SMALLINT     NOT NULL REFERENCES provincia(id_provincia),
    nombre        VARCHAR(60)  NOT NULL,
    UNIQUE (id_provincia, nombre)
);

-- =============================================================================
--  INFRAESTRUCTURA FÍSICA
-- =============================================================================

CREATE TABLE edificio (
    id_edificio   SERIAL        PRIMARY KEY,
    id_canton     INT           NOT NULL REFERENCES canton(id_canton),
    cuenta_pj     VARCHAR(20)   NOT NULL UNIQUE,        -- ej. PJ-30
    nombre        VARCHAR(120)  NOT NULL,
    direccion     VARCHAR(200)
);

CREATE TABLE ubicacion_fisica (
    id_ubicacion  SERIAL        PRIMARY KEY,
    id_edificio   INT           NOT NULL REFERENCES edificio(id_edificio),
    piso          VARCHAR(30),
    modulo_torre  VARCHAR(60),
    rack          VARCHAR(40),
    patchpanel    VARCHAR(40),
    puerto_switch VARCHAR(40),
    activo_sw     VARCHAR(60)
);

-- =============================================================================
--  CATÁLOGOS TÉCNICOS
-- =============================================================================

CREATE TABLE plataforma_vms (
    id_plataforma   SMALLSERIAL PRIMARY KEY,
    nombre          VARCHAR(40) NOT NULL UNIQUE,        -- Milestone, VX, Eclipse
    version         VARCHAR(30),
    puerto_defecto  INT         CHECK (puerto_defecto BETWEEN 1 AND 65535)
);

CREATE TABLE modelo_equipo (
    id_modelo  SERIAL       PRIMARY KEY,
    marca      VARCHAR(60)  NOT NULL,
    modelo     VARCHAR(80)  NOT NULL,
    tipo       VARCHAR(12)  NOT NULL
        CHECK (tipo IN ('CAMARA','GRABADOR','SERVIDOR','SWITCH')),
    UNIQUE (marca, modelo)
);

-- =============================================================================
--  DIMENSIÓN CONTRACTUAL
-- =============================================================================

CREATE TABLE proveedor (
    id_proveedor    SERIAL        PRIMARY KEY,
    nombre_empresa  VARCHAR(150)  NOT NULL UNIQUE
);

CREATE TABLE contratacion (
    id_contratacion  SERIAL       PRIMARY KEY,
    id_proveedor     INT          NOT NULL REFERENCES proveedor(id_proveedor),
    num_sicop        VARCHAR(40),                        -- ej. 2023LE-000016-0001300001
    num_proveeduria  VARCHAR(40),
    orden_pedido     VARCHAR(40),
    num_acta         VARCHAR(40),
    plazo_garantia   INT          CHECK (plazo_garantia >= 0)   -- meses
);

-- =============================================================================
--  PERSONAL TÉCNICO
-- =============================================================================

CREATE TABLE tecnico (
    id_tecnico  SERIAL        PRIMARY KEY,
    nombre      VARCHAR(120)  NOT NULL,
    correo      VARCHAR(120)  UNIQUE,
    telefono    VARCHAR(30)
);

-- =============================================================================
--  SEGURIDAD DEL APLICATIVO
--  [#1] Se crea ANTES de las tablas operativas porque ahora es referenciada
--       por ellas (autoría). Así deja de ser una tabla "isla".
-- =============================================================================

CREATE TABLE usuario_sistema (
    id_usuario       SERIAL        PRIMARY KEY,
    username         VARCHAR(40)   NOT NULL UNIQUE,
    hash_password    VARCHAR(72)   NOT NULL,            -- BCrypt
    nombre_completo  VARCHAR(120),
    rol              VARCHAR(10)   NOT NULL
        CHECK (rol IN ('ADMIN','TECNICO','CONSULTA')),
    activo           BOOLEAN       NOT NULL DEFAULT TRUE,
    -- Enlace opcional: un usuario con rol TECNICO corresponde a una ficha de
    -- TECNICO. Refuerza la integración de USUARIO_SISTEMA al modelo (#1).
    id_tecnico       INT           REFERENCES tecnico(id_tecnico),
    creado_en        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- =============================================================================
--  ENTIDAD CENTRAL
-- =============================================================================

CREATE TABLE equipo (
    id_equipo           SERIAL       PRIMARY KEY,
    id_modelo           INT          NOT NULL REFERENCES modelo_equipo(id_modelo),
    id_ubicacion        INT          NOT NULL REFERENCES ubicacion_fisica(id_ubicacion),
    id_plataforma       SMALLINT     REFERENCES plataforma_vms(id_plataforma),
    id_contratacion     INT          REFERENCES contratacion(id_contratacion),
    nombre_equipo       VARCHAR(80)  NOT NULL,
    serie               VARCHAR(60),
    num_activo          VARCHAR(20)  UNIQUE,
    estado              VARCHAR(12)  NOT NULL DEFAULT 'ACTIVO'
        CHECK (estado IN ('ACTIVO','DANADO','RETIRADO','REEMPLAZADO')),
    ver_windows         VARCHAR(40),
    licencia_win        VARCHAR(60),
    fecha_instalacion   DATE,
    venc_garantia       DATE,
    -- [#2] Trazabilidad de autoría (quién dio de alta el equipo).
    id_usuario_registro INT          REFERENCES usuario_sistema(id_usuario),
    creado_en           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ,
    CHECK (venc_garantia IS NULL OR fecha_instalacion IS NULL
           OR venc_garantia >= fecha_instalacion)
);

-- =============================================================================
--  DEPENDIENTES DE EQUIPO
-- =============================================================================

CREATE TABLE interfaz_red (
    id_interfaz   SERIAL   PRIMARY KEY,
    id_equipo     INT      NOT NULL REFERENCES equipo(id_equipo) ON DELETE CASCADE,
    direccion_ip  INET     NOT NULL UNIQUE,             -- valida formato IP
    mac           MACADDR  UNIQUE,                      -- valida formato MAC
    mascara       INET,
    gateway       INET,
    dns           INET,
    puerto        INT      CHECK (puerto BETWEEN 1 AND 65535)
);

CREATE TABLE credencial_equipo (
    id_credencial    SERIAL       PRIMARY KEY,
    id_equipo        INT          NOT NULL REFERENCES equipo(id_equipo) ON DELETE CASCADE,
    tipo             VARCHAR(10)  NOT NULL
        CHECK (tipo IN ('SO','VMS','TOOLBOX')),
    usuario          VARCHAR(60),
    secreto_cifrado  BYTEA        NOT NULL,             -- AES-256-GCM (texto cifrado)
    UNIQUE (id_equipo, tipo)
);

CREATE TABLE incidente (
    id_incidente        SERIAL       PRIMARY KEY,
    id_equipo           INT          NOT NULL REFERENCES equipo(id_equipo),
    id_tecnico          INT          REFERENCES tecnico(id_tecnico),
    fecha_observacion   DATE         NOT NULL,
    fecha_reparacion    DATE,
    detalle             TEXT,
    trabajo_realizado   TEXT,
    estado              VARCHAR(12)  NOT NULL DEFAULT 'ABIERTO'
        CHECK (estado IN ('ABIERTO','PROCESO','CERRADO')),
    medidas_a_tomar     TEXT,
    -- [#2] Quién registró/gestionó el incidente.
    id_usuario_registro INT          REFERENCES usuario_sistema(id_usuario),
    CHECK (fecha_reparacion IS NULL OR fecha_reparacion >= fecha_observacion)
);

CREATE TABLE movimiento_equipo (
    id_movimiento        SERIAL        PRIMARY KEY,
    id_equipo            INT           NOT NULL REFERENCES equipo(id_equipo),
    id_tecnico           INT           REFERENCES tecnico(id_tecnico),
    -- [#2] CORRECCIÓN CLAVE: queda registrado qué usuario ejecutó el movimiento.
    id_usuario           INT           NOT NULL REFERENCES usuario_sistema(id_usuario),
    tipo                 VARCHAR(12)   NOT NULL
        CHECK (tipo IN ('BAJA','REEMPLAZO','TRASLADO')),
    fecha                TIMESTAMPTZ   NOT NULL DEFAULT now(),
    motivo               TEXT,
    -- Autorreferencia al equipo sustituto (en reemplazos).
    id_equipo_sustituto  INT           REFERENCES equipo(id_equipo),
    CHECK (id_equipo_sustituto IS NULL OR id_equipo_sustituto <> id_equipo),
    -- El sustituto solo aplica en REEMPLAZO.
    CHECK (tipo = 'REEMPLAZO' OR id_equipo_sustituto IS NULL)
);

-- =============================================================================
--  AUDITORÍA  (ISO/IEC 27001)
--  [#4] datos_antes / datos_despues apuntan a un registro concreto mediante
--       (tabla_afectada, id_registro). id_usuario indica quién lo cambió.
-- =============================================================================

CREATE TABLE bitacora_auditoria (
    id_bitacora     BIGSERIAL    PRIMARY KEY,
    id_usuario      INT          REFERENCES usuario_sistema(id_usuario),  -- quién (NULL = acción de sistema)
    tabla_afectada  VARCHAR(63)  NOT NULL,             -- a qué tabla apunta el cambio
    id_registro     BIGINT       NOT NULL,             -- PK del registro afectado
    accion          VARCHAR(6)   NOT NULL
        CHECK (accion IN ('INSERT','UPDATE','DELETE')),
    datos_antes     JSONB,                              -- imagen previa  (NULL en INSERT)
    datos_despues   JSONB,                              -- imagen posterior (NULL en DELETE)
    fecha_hora      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- =============================================================================
--  FUNCIÓN Y TRIGGERS DE AUDITORÍA AUTOMÁTICA
-- =============================================================================
--
--  El aplicativo (capa de negocio) fija el usuario autenticado al inicio de
--  cada transacción con:
--        SET LOCAL app.id_usuario = '<id_usuario>';
--  La función lo lee con current_setting('app.id_usuario', true): así la base
--  de datos sabe QUIÉN hizo el cambio aunque el trigger corra dentro del motor.
--  El nombre de la PK de cada tabla se pasa como argumento del trigger.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_auditoria() RETURNS trigger AS $$
DECLARE
    v_usuario INT;
    v_id      BIGINT;
BEGIN
    v_usuario := NULLIF(current_setting('app.id_usuario', true), '')::INT;

    IF (TG_OP = 'DELETE') THEN
        v_id := (to_jsonb(OLD) ->> TG_ARGV[0])::BIGINT;
        INSERT INTO bitacora_auditoria
            (id_usuario, tabla_afectada, id_registro, accion, datos_antes, datos_despues)
        VALUES (v_usuario, TG_TABLE_NAME, v_id, 'DELETE', to_jsonb(OLD), NULL);
        RETURN OLD;

    ELSIF (TG_OP = 'UPDATE') THEN
        v_id := (to_jsonb(NEW) ->> TG_ARGV[0])::BIGINT;
        INSERT INTO bitacora_auditoria
            (id_usuario, tabla_afectada, id_registro, accion, datos_antes, datos_despues)
        VALUES (v_usuario, TG_TABLE_NAME, v_id, 'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;

    ELSE  -- INSERT
        v_id := (to_jsonb(NEW) ->> TG_ARGV[0])::BIGINT;
        INSERT INTO bitacora_auditoria
            (id_usuario, tabla_afectada, id_registro, accion, datos_antes, datos_despues)
        VALUES (v_usuario, TG_TABLE_NAME, v_id, 'INSERT', NULL, to_jsonb(NEW));
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Auditoría sobre las tablas sensibles (el segundo argumento es el nombre de su PK).
CREATE TRIGGER trg_aud_equipo
    AFTER INSERT OR UPDATE OR DELETE ON equipo
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_equipo');

CREATE TRIGGER trg_aud_movimiento
    AFTER INSERT OR UPDATE OR DELETE ON movimiento_equipo
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_movimiento');

CREATE TRIGGER trg_aud_incidente
    AFTER INSERT OR UPDATE OR DELETE ON incidente
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_incidente');

CREATE TRIGGER trg_aud_credencial
    AFTER INSERT OR UPDATE OR DELETE ON credencial_equipo
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_credencial');

CREATE TRIGGER trg_aud_interfaz
    AFTER INSERT OR UPDATE OR DELETE ON interfaz_red
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_interfaz');

CREATE TRIGGER trg_aud_usuario
    AFTER INSERT OR UPDATE OR DELETE ON usuario_sistema
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria('id_usuario');

-- -----------------------------------------------------------------------------
-- Mantiene equipo.actualizado_en en cada modificación.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_touch_actualizado() RETURNS trigger AS $$
BEGIN
    NEW.actualizado_en := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_touch_equipo
    BEFORE UPDATE ON equipo
    FOR EACH ROW EXECUTE FUNCTION fn_touch_actualizado();

-- =============================================================================
--  VISTA: garantías por vencer (horizonte de 90 días) — módulo de alertas
-- =============================================================================
CREATE OR REPLACE VIEW v_garantias_por_vencer AS
SELECT e.id_equipo,
       e.nombre_equipo,
       e.num_activo,
       e.venc_garantia,
       (e.venc_garantia - CURRENT_DATE)        AS dias_restantes,
       ed.cuenta_pj,
       ed.nombre                                AS edificio,
       p.nombre                                 AS provincia
FROM   equipo            e
JOIN   ubicacion_fisica  u  ON u.id_ubicacion = e.id_ubicacion
JOIN   edificio          ed ON ed.id_edificio = u.id_edificio
JOIN   canton            c  ON c.id_canton    = ed.id_canton
JOIN   provincia         p  ON p.id_provincia = c.id_provincia
WHERE  e.venc_garantia IS NOT NULL
  AND  e.estado <> 'RETIRADO'
  AND  e.venc_garantia BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '90 days'
ORDER BY e.venc_garantia;

-- =============================================================================
--  ÍNDICES DE APOYO
-- =============================================================================
CREATE INDEX idx_equipo_ubicacion        ON equipo(id_ubicacion);
CREATE INDEX idx_equipo_estado           ON equipo(estado);
CREATE INDEX idx_equipo_venc_garantia    ON equipo(venc_garantia);
CREATE INDEX idx_incidente_equipo        ON incidente(id_equipo);
CREATE INDEX idx_incidente_estado        ON incidente(estado);
CREATE INDEX idx_movimiento_equipo       ON movimiento_equipo(id_equipo);
CREATE INDEX idx_movimiento_usuario      ON movimiento_equipo(id_usuario);
CREATE INDEX idx_bitacora_registro       ON bitacora_auditoria(tabla_afectada, id_registro);
CREATE INDEX idx_bitacora_usuario        ON bitacora_auditoria(id_usuario);
CREATE INDEX idx_bitacora_fecha          ON bitacora_auditoria(fecha_hora);

-- =============================================================================
--  FIN DEL SCRIPT
-- =============================================================================
