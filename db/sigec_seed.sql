-- =============================================================================
--  SIGEC-PJ  ·  Datos semilla (catálogos y ejemplos de demostración)
--  Cargar DESPUÉS de sigec_ddl.sql:
--      psql -U postgres -d sigecpj -f db/sigec_ddl.sql
--      psql -U postgres -d sigecpj -f db/sigec_seed.sql
--
--  Idempotente: usa ON CONFLICT DO NOTHING, de modo que puede ejecutarse varias
--  veces sin duplicar filas. No incluye usuarios del sistema (el backend crea el
--  ADMIN inicial al arrancar).
-- =============================================================================

SET client_encoding = 'UTF8';

-- ---------------------------------------------------------------------------
-- Provincias (catálogo oficial de Costa Rica)
-- ---------------------------------------------------------------------------
INSERT INTO provincia (id_provincia, nombre) VALUES
    (1, 'San José'),
    (2, 'Alajuela'),
    (3, 'Cartago'),
    (4, 'Heredia'),
    (5, 'Guanacaste'),
    (6, 'Puntarenas'),
    (7, 'Limón')
ON CONFLICT (id_provincia) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Cantones representativos
-- ---------------------------------------------------------------------------
INSERT INTO canton (id_provincia, nombre) VALUES
    (1, 'Central'),
    (1, 'Desamparados'),
    (2, 'Central'),
    (2, 'Atenas'),
    (3, 'Central'),
    (4, 'Central'),
    (5, 'Liberia'),
    (6, 'Central'),
    (7, 'Central')
ON CONFLICT (id_provincia, nombre) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Edificios (cuenta PJ única)
-- ---------------------------------------------------------------------------
INSERT INTO edificio (id_canton, cuenta_pj, nombre, direccion)
SELECT c.id_canton, v.cuenta_pj, v.nombre, v.direccion
FROM (VALUES
    (1, 'Central',  'PJ-01', 'Corte Suprema de Justicia',        'San José, Av. 6-8, Calle 17-19'),
    (2, 'Central',  'PJ-12', 'Tribunales de Alajuela',           'Alajuela centro'),
    (2, 'Atenas',   'PJ-30', 'Tribunales y OIJ de Atenas',       'Atenas centro'),
    (4, 'Central',  'PJ-18', 'Tribunales de Heredia',            'Heredia centro')
) AS v(id_prov, canton_nombre, cuenta_pj, nombre, direccion)
JOIN canton c ON c.id_provincia = v.id_prov AND c.nombre = v.canton_nombre
ON CONFLICT (cuenta_pj) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Ubicaciones físicas
-- ---------------------------------------------------------------------------
INSERT INTO ubicacion_fisica (id_edificio, piso, modulo_torre, rack, patchpanel, puerto_switch, activo_sw)
SELECT e.id_edificio, v.piso, v.modulo, v.rack, v.patchpanel, v.puerto, v.activo
FROM (VALUES
    ('PJ-01', '1', 'Torre A', 'R-01', 'PP-01', 'G0/1',  'SW-CORE-01'),
    ('PJ-01', '2', 'Torre A', 'R-02', 'PP-02', 'G0/2',  'SW-CORE-01'),
    ('PJ-12', '1', 'Modulo B','R-10', 'PP-10', 'G1/4',  'SW-ALA-01'),
    ('PJ-30', '1', 'Modulo C','R-20', 'PP-20', 'G0/8',  'SW-ATE-01'),
    ('PJ-18', '3', 'Torre 1', 'R-31', 'PP-31', 'G2/2',  'SW-HER-01')
) AS v(cuenta_pj, piso, modulo, rack, patchpanel, puerto, activo)
JOIN edificio e ON e.cuenta_pj = v.cuenta_pj
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Plataformas VMS
-- ---------------------------------------------------------------------------
INSERT INTO plataforma_vms (nombre, version, puerto_defecto) VALUES
    ('Milestone', 'XProtect 2023', 8080),
    ('VX',        'Pelco VX 4.x',  8081),
    ('Eclipse',   'Eclipse 2.x',   80)
ON CONFLICT (nombre) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Modelos de equipo
-- ---------------------------------------------------------------------------
INSERT INTO modelo_equipo (marca, modelo, tipo) VALUES
    ('Pelco', 'IME-238-1ERS',   'CAMARA'),
    ('Pelco', 'Sarix Pro 4',    'CAMARA'),
    ('Dell',  'PowerEdge R740', 'SERVIDOR'),
    ('HP',    'ProLiant DL380', 'SERVIDOR'),
    ('Hikvision', 'DS-9664NI',  'GRABADOR'),
    ('Cisco', 'Catalyst 2960',  'SWITCH')
ON CONFLICT (marca, modelo) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Proveedor y contratación
-- ---------------------------------------------------------------------------
INSERT INTO proveedor (nombre_empresa) VALUES
    ('Sistemas de Seguridad CR S.A.'),
    ('Tecnología Integral del Valle S.A.')
ON CONFLICT (nombre_empresa) DO NOTHING;

INSERT INTO contratacion (id_proveedor, num_sicop, num_proveeduria, orden_pedido, num_acta, plazo_garantia)
SELECT p.id_proveedor, v.num_sicop, v.num_prov, v.orden, v.acta, v.garantia
FROM (VALUES
    ('Sistemas de Seguridad CR S.A.',         '2023LE-000016-0001300001', 'PROV-2023-016', 'OP-1045', 'ACTA-220', 60),
    ('Tecnología Integral del Valle S.A.',    '2024LA-000033-0001300001', 'PROV-2024-033', 'OP-2210', 'ACTA-455', 36)
) AS v(empresa, num_sicop, num_prov, orden, acta, garantia)
JOIN proveedor p ON p.nombre_empresa = v.empresa
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Técnicos
-- ---------------------------------------------------------------------------
INSERT INTO tecnico (nombre, correo, telefono) VALUES
    ('Juan Pérez Vargas',   'juan.perez@poder-judicial.go.cr',   '2295-3000'),
    ('María Solano Rojas',  'maria.solano@poder-judicial.go.cr', '2295-3001'),
    ('Carlos Mora Jiménez', 'carlos.mora@poder-judicial.go.cr',  '2295-3002')
ON CONFLICT (correo) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Equipos de ejemplo (con interfaz de red)
-- ---------------------------------------------------------------------------
INSERT INTO equipo (id_modelo, id_ubicacion, id_plataforma, id_contratacion,
                    nombre_equipo, serie, num_activo, estado, fecha_instalacion, venc_garantia)
SELECT m.id_modelo, u.id_ubicacion, pv.id_plataforma, ct.id_contratacion,
       v.nombre, v.serie, v.num_activo, v.estado, v.f_inst::date, v.venc::date
FROM (VALUES
    ('Pelco','IME-238-1ERS',   'PJ-01','R-01', 'Milestone', 'Camara entrada principal',  'SN-CAM-1001', 'ACT-0001', 'ACTIVO', '2024-01-15', '2029-01-15'),
    ('Dell','PowerEdge R740',  'PJ-01','R-02', 'Milestone', 'Servidor VMS central',      'SN-SRV-2001', 'ACT-0002', 'ACTIVO', '2023-05-10', '2028-05-10'),
    ('Hikvision','DS-9664NI',  'PJ-30','R-20', 'VX',        'Grabador OIJ Atenas',       'SN-NVR-3001', 'ACT-0003', 'ACTIVO', '2023-08-01', '2026-08-01'),
    ('Pelco','Sarix Pro 4',    'PJ-12','R-10', 'VX',        'Camara parqueo Alajuela',   'SN-CAM-1002', 'ACT-0004', 'ACTIVO', '2024-03-20', '2026-09-20')
) AS v(marca, modelo, cuenta_pj, rack, plataforma, nombre, serie, num_activo, estado, f_inst, venc)
JOIN modelo_equipo m   ON m.marca = v.marca AND m.modelo = v.modelo
JOIN edificio ed       ON ed.cuenta_pj = v.cuenta_pj
JOIN ubicacion_fisica u ON u.id_edificio = ed.id_edificio AND u.rack = v.rack
JOIN plataforma_vms pv  ON pv.nombre = v.plataforma
LEFT JOIN contratacion ct ON ct.id_contratacion = 1
ON CONFLICT (num_activo) DO NOTHING;

INSERT INTO interfaz_red (id_equipo, direccion_ip, mac, mascara, gateway, dns, puerto)
SELECT e.id_equipo, v.ip::inet, v.mac::macaddr, v.mascara::inet, v.gw::inet, v.dns::inet, v.puerto
FROM (VALUES
    ('ACT-0001', '172.24.24.50',  'a4:bb:6d:11:22:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0002', '172.24.24.10',  'a4:bb:6d:11:22:02', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0003', '172.24.28.41',  'a4:bb:6d:11:22:03', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0004', '172.24.26.77',  'a4:bb:6d:11:22:04', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081)
) AS v(num_activo, ip, mac, mascara, gw, dns, puerto)
JOIN equipo e ON e.num_activo = v.num_activo
ON CONFLICT (direccion_ip) DO NOTHING;

-- =============================================================================
--  FIN DEL SEED
-- =============================================================================
