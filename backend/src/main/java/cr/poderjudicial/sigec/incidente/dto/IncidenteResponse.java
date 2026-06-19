package cr.poderjudicial.sigec.incidente.dto;

import java.time.LocalDate;

/** Detalle completo de un incidente. */
public record IncidenteResponse(
        Integer id,
        Integer idEquipo,
        String equipo,
        Integer idTecnico,
        String tecnico,
        LocalDate fechaObservacion,
        LocalDate fechaReparacion,
        String detalle,
        String trabajoRealizado,
        String estado,
        String medidasATomar,
        Integer idUsuarioRegistro
) {
}
