package cr.poderjudicial.sigec.movimiento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Datos para registrar un movimiento de equipo. */
public record MovimientoRequest(
        @NotNull Integer idEquipo,
        @NotNull @Pattern(regexp = "BAJA|REEMPLAZO|TRASLADO",
                message = "tipo invalido") String tipo,
        Integer idTecnico,
        String motivo,
        Integer idEquipoSustituto
) {
}
