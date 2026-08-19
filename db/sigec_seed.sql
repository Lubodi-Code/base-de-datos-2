-- =============================================================================
--  SIGEC-PJ  ·  Datos semilla (catálogos y ejemplos de demostración)
--  Cargar DESPUÉS de sigec_ddl.sql:
--      psql -U postgres -d sigecpj -f db/sigec_ddl.sql
--      psql -U postgres -d sigecpj -f db/sigec_seed.sql
--
--  Los catálogos usan ON CONFLICT DO NOTHING. El script completo está pensado
--  para el bootstrap de una base vacía.
--  El usuario ADMIN de demostracion se crea aqui para que los movimientos
--  iniciales puedan conservar su autoria desde el bootstrap.
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
    ('Dell',  'PowerEdge R750', 'SERVIDOR'),
    ('HP',    'ProLiant DL380', 'SERVIDOR'),
    ('Hikvision', 'DS-9664NI',  'GRABADOR'),
    ('Hikvision', 'DS-2CD2F45', 'CAMARA'),
    ('Hikvision', 'DS-7608NI',  'GRABADOR'),
    ('Axis',  'P3265-LVE',      'CAMARA'),
    ('Axis',  'M3065-V',        'CAMARA'),
    ('Cisco', 'Catalyst 2960',  'SWITCH'),
    ('Cisco', 'C9200-24T',      'SWITCH')
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
-- Equipos de ejemplo (expandido para demo con ≥ 30 equipos)
-- ---------------------------------------------------------------------------
INSERT INTO equipo (id_modelo, id_ubicacion, id_plataforma, id_contratacion,
                    nombre_equipo, serie, num_activo, estado, fecha_instalacion, venc_garantia)
SELECT m.id_modelo, u.id_ubicacion, pv.id_plataforma, ct.id_contratacion,
       v.nombre, v.serie, v.num_activo, v.estado, v.f_inst::date, v.venc::date
FROM (VALUES
    -- PJ-01 San José (Corte Suprema)
    ('Pelco','IME-238-1ERS',   'PJ-01','R-01', 'Milestone', 'Camara entrada principal',           'SN-CAM-1001', 'ACT-0001', 'ACTIVO', '2024-01-15', '2029-01-15'),
    ('Pelco','Sarix Pro 4',    'PJ-01','R-01', 'Milestone', 'Camara recepción',                    'SN-CAM-1002', 'ACT-0005', 'ACTIVO', '2024-02-10', '2029-02-10'),
    ('Hikvision','DS-2CD2F45', 'PJ-01','R-01', 'Milestone', 'Camara pasillo principal',             'SN-CAM-1003', 'ACT-0006', 'ACTIVO', '2024-03-05', '2029-03-05'),
    ('Axis','P3265-LVE',       'PJ-01','R-01', 'Milestone', 'Camara sala audiencias',              'SN-CAM-1004', 'ACT-0007', 'ACTIVO', '2024-04-20', '2029-04-20'),
    ('Dell','PowerEdge R740',  'PJ-01','R-02', 'Milestone', 'Servidor VMS central',                 'SN-SRV-2001', 'ACT-0002', 'ACTIVO', '2023-05-10', '2028-05-10'),
    ('HP','ProLiant DL380',    'PJ-01','R-02', 'Milestone', 'Servidor backup VMS',                  'SN-SRV-2002', 'ACT-0008', 'ACTIVO', '2023-06-15', '2028-06-15'),
    ('Cisco','Catalyst 2960',  'PJ-01','R-01', NULL,       'Switch cámaras piso 1',                 'SN-SWI-3001', 'ACT-0009', 'ACTIVO', '2024-01-20', '2027-01-20'),
    ('Cisco','Catalyst 2960',  'PJ-01','R-02', NULL,       'Switch servidores',                     'SN-SWI-3002', 'ACT-0010', 'ACTIVO', '2024-01-20', '2027-01-20'),

    -- PJ-12 Alajuela
    ('Pelco','Sarix Pro 4',    'PJ-12','R-10', 'VX',        'Camara parqueo Alajuela',              'SN-CAM-1002', 'ACT-0004', 'ACTIVO', '2024-03-20', '2026-09-20'),
    ('Axis','M3065-V',         'PJ-12','R-10', 'VX',        'Camara entrada tribunal',              'SN-CAM-1005', 'ACT-0011', 'ACTIVO', '2024-05-15', '2029-05-15'),
    ('Hikvision','DS-9664NI',  'PJ-12','R-10', 'VX',        'NVR principal Alajuela',                'SN-NVR-3002', 'ACT-0012', 'ACTIVO', '2024-04-10', '2027-04-10'),
    ('Dell','PowerEdge R740',  'PJ-12','R-11', 'VX',        'Servidor VMS Alajuela',                 'SN-SRV-2003', 'ACT-0013', 'ACTIVO', '2024-02-20', '2029-02-20'),

    -- PJ-30 Atenas
    ('Hikvision','DS-9664NI',  'PJ-30','R-20', 'VX',        'Grabador OIJ Atenas',                  'SN-NVR-3001', 'ACT-0003', 'DANADO', '2023-08-01', '2026-08-01'),
    ('Axis','P3265-LVE',       'PJ-30','R-20', 'VX',        'Camara OIJ Atenas (reemplazo)',        'SN-CAM-1006', 'ACT-0014', 'ACTIVO', '2026-06-01', '2031-06-01'),
    ('Pelco','IME-238-1ERS',   'PJ-30','R-20', 'VX',        'Camara entrada OIJ',                   'SN-CAM-1007', 'ACT-0015', 'ACTIVO', '2024-07-10', '2029-07-10'),
    ('Cisco','C9200-24T',      'PJ-30','R-20', NULL,       'Switch OIJ Atenas',                     'SN-SWI-3003', 'ACT-0016', 'ACTIVO', '2024-07-15', '2027-07-15'),

    -- PJ-18 Heredia
    ('Axis','M3065-V',         'PJ-18','R-31', 'Eclipse',   'Camara tributos Heredia',              'SN-CAM-1008', 'ACT-0017', 'ACTIVO', '2024-06-20', '2029-06-20'),
    ('Hikvision','DS-7608NI',  'PJ-18','R-31', 'Eclipse',   'NVR Heredia',                          'SN-NVR-3003', 'ACT-0018', 'ACTIVO', '2024-08-05', '2027-08-05'),
    ('Dell','PowerEdge R750',  'PJ-18','R-31', 'Eclipse',   'Servidor VMS Heredia',                 'SN-SRV-2004', 'ACT-0019', 'ACTIVO', '2024-05-25', '2029-05-25'),

    -- Equipos adicionales para alcanzar ≥ 30
    ('Pelco','Sarix Pro 4',    'PJ-01','R-01', 'Milestone', 'Camara sala archivos',                 'SN-CAM-1009', 'ACT-0020', 'ACTIVO', '2024-09-10', '2029-09-10'),
    ('Axis','P3265-LVE',       'PJ-01','R-01', 'Milestone', 'Camara sala jueces',                   'SN-CAM-1010', 'ACT-0021', 'ACTIVO', '2024-09-15', '2029-09-15'),
    ('Hikvision','DS-2CD2F45', 'PJ-12','R-10', 'VX',        'Camara despacho juzgado 1',            'SN-CAM-1011', 'ACT-0022', 'ACTIVO', '2024-10-01', '2029-10-01'),
    ('Pelco','Sarix Pro 4',    'PJ-12','R-10', 'VX',        'Camara despacho juzgado 2',            'SN-CAM-1012', 'ACT-0023', 'ACTIVO', '2024-10-05', '2029-10-05'),
    ('Axis','M3065-V',         'PJ-30','R-20', 'VX',        'Camara fiscalía OIJ',                  'SN-CAM-1013', 'ACT-0024', 'ACTIVO', '2024-11-12', '2029-11-12'),
    ('Hikvision','DS-2CD2F45', 'PJ-30','R-20', 'VX',        'Camara sala detención',                 'SN-CAM-1014', 'ACT-0025', 'ACTIVO', '2024-11-15', '2029-11-15'),
    ('Cisco','C9200-24T',      'PJ-18','R-31', 'Eclipse',   'Switch Heredia piso 2',                 'SN-SWI-3004', 'ACT-0026', 'ACTIVO', '2024-08-20', '2027-08-20'),
    ('Dell','PowerEdge R750',  'PJ-01','R-02', 'Milestone', 'Servidor almacenamiento',               'SN-SRV-2005', 'ACT-0027', 'ACTIVO', '2024-03-25', '2029-03-25'),
    ('HP','ProLiant DL380',    'PJ-12','R-11', 'VX',        'Servidor backup Alajuela',              'SN-SRV-2006', 'ACT-0028', 'ACTIVO', '2024-06-30', '2029-06-30'),
    ('Pelco','Sarix Pro 4',    'PJ-18','R-31', 'Eclipse',   'Camara entrada principal Heredia',      'SN-CAM-1015', 'ACT-0029', 'ACTIVO', '2024-12-01', '2029-12-01'),
    ('Axis','P3265-LVE',       'PJ-01','R-01', 'Milestone', 'Camara seguridad perimetral',           'SN-CAM-1016', 'ACT-0030', 'ACTIVO', '2025-01-10', '2030-01-10'),
    ('Hikvision','DS-7608NI',   'PJ-01','R-02', 'Milestone', 'NVR backup San José',                  'SN-NVR-3004', 'ACT-0031', 'RETIRADO', '2025-02-15', '2028-02-15')
) AS v(marca, modelo, cuenta_pj, rack, plataforma, nombre, serie, num_activo, estado, f_inst, venc)
JOIN modelo_equipo m   ON m.marca = v.marca AND m.modelo = v.modelo
JOIN edificio ed       ON ed.cuenta_pj = v.cuenta_pj
JOIN ubicacion_fisica u ON u.id_edificio = ed.id_edificio AND u.rack = v.rack
LEFT JOIN plataforma_vms pv  ON pv.nombre = v.plataforma
LEFT JOIN contratacion ct ON ct.id_contratacion = 1
ON CONFLICT (num_activo) DO NOTHING;

INSERT INTO interfaz_red (id_equipo, direccion_ip, mac, mascara, gateway, dns, puerto)
SELECT e.id_equipo, v.ip::inet, v.mac::macaddr, v.mascara::inet, v.gw::inet, v.dns::inet, v.puerto
FROM (VALUES
    ('ACT-0001', '172.24.24.50',  'a4:bb:6d:11:22:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0002', '172.24.24.10',  'a4:bb:6d:11:22:02', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0003', '172.24.28.41',  'a4:bb:6d:11:22:03', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0004', '172.24.26.77',  'a4:bb:6d:11:22:04', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    -- Interfaces adicionales para los nuevos equipos
    ('ACT-0005', '172.24.24.51',  'a4:bb:6d:11:23:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0006', '172.24.24.52',  'a4:bb:6d:11:23:02', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0007', '172.24.24.53',  'a4:bb:6d:11:23:03', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0008', '172.24.24.11',  'a4:bb:6d:11:23:04', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0009', '172.24.24.12',  'a4:bb:6d:11:23:05', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0010', '172.24.24.13',  'a4:bb:6d:11:23:06', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0011', '172.24.26.78',  'a4:bb:6d:11:24:01', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0012', '172.24.26.79',  'a4:bb:6d:11:24:02', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0013', '172.24.26.80',  'a4:bb:6d:11:24:03', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0014', '172.24.28.42',  'a4:bb:6d:11:25:01', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0015', '172.24.28.43',  'a4:bb:6d:11:25:02', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0016', '172.24.28.44',  'a4:bb:6d:11:25:03', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0017', '172.24.27.50',  'a4:bb:6d:11:26:01', '255.255.255.0', '172.24.27.1', '172.24.1.10', 80),
    ('ACT-0018', '172.24.27.51',  'a4:bb:6d:11:26:02', '255.255.255.0', '172.24.27.1', '172.24.1.10', 80),
    ('ACT-0019', '172.24.27.52',  'a4:bb:6d:11:26:03', '255.255.255.0', '172.24.27.1', '172.24.1.10', 80),
    ('ACT-0020', '172.24.24.54',  'a4:bb:6d:11:27:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0021', '172.24.24.55',  'a4:bb:6d:11:27:02', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0022', '172.24.26.81',  'a4:bb:6d:11:28:01', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0023', '172.24.26.82',  'a4:bb:6d:11:28:02', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0024', '172.24.28.45',  'a4:bb:6d:11:29:01', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0025', '172.24.28.46',  'a4:bb:6d:11:29:02', '255.255.255.0', '172.24.28.1', '172.24.1.10', 8081),
    ('ACT-0026', '172.24.27.53',  'a4:bb:6d:11:30:01', '255.255.255.0', '172.24.27.1', '172.24.1.10', 80),
    ('ACT-0027', '172.24.24.14',  'a4:bb:6d:11:31:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0028', '172.24.26.83',  'a4:bb:6d:11:31:02', '255.255.255.0', '172.24.26.1', '172.24.1.10', 8081),
    ('ACT-0029', '172.24.27.54',  'a4:bb:6d:11:32:01', '255.255.255.0', '172.24.27.1', '172.24.1.10', 80),
    ('ACT-0030', '172.24.24.56',  'a4:bb:6d:11:33:01', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080),
    ('ACT-0031', '172.24.24.57',  'a4:bb:6d:11:33:02', '255.255.255.0', '172.24.24.1', '172.24.1.10', 8080)
) AS v(num_activo, ip, mac, mascara, gw, dns, puerto)
JOIN equipo e ON e.num_activo = v.num_activo
ON CONFLICT (direccion_ip) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Incidentes de ejemplo (≥ 10 incidentes variados)
-- ---------------------------------------------------------------------------
INSERT INTO incidente (id_equipo, id_tecnico, fecha_observacion, fecha_reparacion, detalle, trabajo_realizado, estado, medidas_a_tomar)
SELECT e.id_equipo, t.id_tecnico, v.f_observacion::date, v.f_reparacion::date, v.detalle, v.trabajo, v.estado, v.medidas
FROM (VALUES
    -- Incidentes activos y cerrados
    ('ACT-0003', 'juan.perez@poder-judicial.go.cr', '2026-05-12', '2026-05-15',
     'NVR se reinicia intermitentemente', 'Se reemplazó fuente de poder y se actualizó firmware', 'CERRADO',
     'Monitorear por 2 semanas para verificar estabilidad'),

    ('ACT-0010', 'maria.solano@poderjudicial.go.cr', '2026-06-20', NULL,
     'Switch no responde a pings', 'Pendiente diagnóstico en sitio', 'ABIERTO',
     'Verificar cableado y configuración de red'),

    ('ACT-0014', 'carlos.mora@poder-judicial.go.cr', '2026-07-01', '2026-07-03',
     'Cámara con imagen distorsionada', 'Se ajustó enfoque y se limpió lente', 'CERRADO',
     'Capacitar al personal sobre mantenimiento básico'),

    ('ACT-0008', 'juan.perez@poder-judicial.go.cr', '2026-07-10', NULL,
     'Servidor VMS alto consumo de CPU', 'En análisis, posible actualización de software', 'PROCESO',
     'Revisar logs y planificar actualización'),

    ('ACT-0018', 'maria.solano@poderjudicial.go.cr', '2026-07-15', '2026-07-18',
     'NVR Heredia no graba continuamente', 'Se expandió almacenamiento y se reconfiguró RAID', 'CERRADO',
     'Implementar monitoreo de espacio en disco'),

    ('ACT-0025', 'carlos.mora@poder-judicial.go.cr', '2026-07-20', NULL,
     'Cámara sala detención sin video', 'Verificar conexión PoE y cableado', 'ABIERTO',
     'Revisar switch y reemplazar cable si es necesario'),

    ('ACT-0002', 'juan.perez@poder-judicial.go.cr', '2026-07-22', '2026-07-23',
     'Servidor VMS requirió reinicio', 'Se aplicaron parches de seguridad y se reinició', 'CERRADO',
     'Planificar ventana de mantenimiento regular'),

    ('ACT-0012', 'maria.solano@poderjudicial.go.cr', '2026-07-25', NULL,
     'NVR Alajuela alerta de disco lleno', 'Se liberó espacio eliminando grabaciones antiguas', 'PROCESO',
     'Expandir almacenamiento o reducir retención'),

    ('ACT-0031', 'carlos.mora@poder-judicial.go.cr', '2026-07-28', '2026-07-30',
     'NVR backup San José fuera de línea', 'Se reemplazó disco duro defectuoso', 'CERRADO',
     'Implementar respaldo redundante'),

    ('ACT-0020', 'juan.perez@poder-judicial.go.cr', '2026-08-01', NULL,
     'Cámara archivos con calidad de video baja', 'Pendiente ajuste de configuración', 'ABIERTO',
     'Verificar ancho de banda y configuración de bitrate'),

    ('ACT-0016', 'maria.solano@poderjudicial.go.cr', '2026-08-05', '2026-08-07',
     'Cámara entrada Heredia reiniciando', 'Se actualizó firmware y se estabilizó', 'CERRADO',
     'Monitorear comportamiento tras actualización'),

    ('ACT-0027', 'carlos.mora@poder-judicial.go.cr', '2026-08-10', NULL,
     'Servidor almacenamiento alerta de temperatura', 'Se verificó ventilación y se limpiaron filtros', 'PROCESO',
     'Instalar monitoreo de temperatura ambiental')
) AS v(num_activo, correo_tecnico, f_observacion, f_reparacion, detalle, trabajo, estado, medidas)
JOIN equipo e ON e.num_activo = v.num_activo
JOIN tecnico t ON t.correo = v.correo_tecnico
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Movimientos de equipo (≥ 5 movimientos incluyendo reemplazo de equipo dañado)
-- ---------------------------------------------------------------------------
-- Usuario de demostracion. El hash BCrypt corresponde a admin123 y debe
-- sustituirse o deshabilitarse fuera de ambientes de prueba.
INSERT INTO usuario_sistema (username, hash_password, nombre_completo, rol, activo)
VALUES ('admin', '$2a$12$xNESx1q8b2TVjVpXiDBbWOh6MK46Uhaga0XsM4kvicgrHbWm2F1bG',
        'Administrador SIGEC', 'ADMIN', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO movimiento_equipo (id_equipo, id_tecnico, id_usuario, tipo, fecha, motivo, id_equipo_sustituto)
SELECT e.id_equipo, t.id_tecnico, u.id_usuario, v.tipo, v.fecha::timestamp, v.motivo, es.id_equipo
FROM (VALUES
    -- Reemplazo del equipo dañado ACT-0003 por ACT-0014
    ('ACT-0003', 'carlos.mora@poder-judicial.go.cr', 'admin', 'REEMPLAZO', '2026-06-01 10:30:00',
     'NVR OIJ Atenas falló tras tormenta eléctrica, se reemplaza por unidad nueva', 'ACT-0014'),

    -- Otros movimientos de ejemplo
    ('ACT-0031', 'juan.perez@poder-judicial.go.cr', 'admin', 'BAJA', '2026-07-15 14:00:00',
     'NVR backup alcanzó fin de vida útil, se retiró de servicio', NULL),

    ('ACT-0018', 'maria.solano@poder-judicial.go.cr', 'admin', 'TRASLADO', '2026-06-10 09:15:00',
     'NVR Heredia trasladado de R-30 a R-31 por reorganización de rack', NULL),

    ('ACT-0002', 'carlos.mora@poder-judicial.go.cr', 'admin', 'TRASLADO', '2026-05-20 11:45:00',
     'Servidor VMS trasladado para mejorar ventilación', NULL),

    ('ACT-0008', 'juan.perez@poder-judicial.go.cr', 'admin', 'BAJA', '2026-07-25 16:30:00',
     'Switch obsoleto dado de baja tras actualización de infraestructura', NULL),

    ('ACT-0012', 'maria.solano@poder-judicial.go.cr', 'admin', 'TRASLADO', '2026-08-02 08:00:00',
     'NVR trasladado temporalmente por mantenimiento de rack', NULL),

    ('ACT-0027', 'carlos.mora@poder-judicial.go.cr', 'admin', 'TRASLADO', '2026-08-08 13:20:00',
     'Servidor almacenamiento reubicado por expansión de capacidad', NULL)
) AS v(num_activo, correo_tecnico, username, tipo, fecha, motivo, sustituto)
JOIN equipo e ON e.num_activo = v.num_activo
JOIN tecnico t ON t.correo = v.correo_tecnico
JOIN usuario_sistema u ON u.username = v.username
LEFT JOIN equipo es ON es.num_activo = v.sustituto
WHERE NOT EXISTS (
    SELECT 1
    FROM movimiento_equipo m
    WHERE m.id_equipo = e.id_equipo
      AND m.tipo = v.tipo
      AND m.fecha = v.fecha::timestamp
)
ON CONFLICT DO NOTHING;

-- =============================================================================
--  FIN DEL SEED
-- =============================================================================
