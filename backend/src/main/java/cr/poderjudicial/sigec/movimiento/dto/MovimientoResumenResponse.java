package cr.poderjudicial.sigec.movimiento.dto;

import cr.poderjudicial.sigec.movimiento.entity.TipoMovimiento;

import java.time.OffsetDateTime;

/** Fila de listado de movimientos (proyeccion directa via JPQL, sin carga perezosa). */
public record MovimientoResumenResponse(
        Integer id,
        Integer idEquipo,
        String equipo,
        TipoMovimiento tipo,
        OffsetDateTime fecha,
        String tecnico
) {
}
