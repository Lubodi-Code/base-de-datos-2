package cr.poderjudicial.sigec.movimiento.dto;

import java.time.OffsetDateTime;

/** Detalle completo de un movimiento de equipo. */
public record MovimientoResponse(
        Integer id,
        Integer idEquipo,
        String equipo,
        String tipo,
        OffsetDateTime fecha,
        String motivo,
        Integer idTecnico,
        String tecnico,
        Integer idUsuario,
        Integer idEquipoSustituto,
        String equipoSustituto
) {
}
