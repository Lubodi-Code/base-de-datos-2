package cr.poderjudicial.sigec.incidente.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/** Datos para crear o actualizar un incidente. */
public record IncidenteRequest(
        @NotNull Integer idEquipo,
        Integer idTecnico,
        @NotNull LocalDate fechaObservacion,
        String detalle,
        String trabajoRealizado,
        @Pattern(regexp = "ABIERTO|PROCESO|CERRADO",
                message = "estado invalido") String estado,
        String medidasATomar
) {
}
