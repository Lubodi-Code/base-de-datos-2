package cr.poderjudicial.sigec.incidente.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Datos para cerrar un incidente (transicion a CERRADO). */
public record IncidenteCierreRequest(
        @NotNull LocalDate fechaReparacion,
        String trabajoRealizado
) {
}
