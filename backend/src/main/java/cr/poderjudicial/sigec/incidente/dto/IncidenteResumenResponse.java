package cr.poderjudicial.sigec.incidente.dto;

import cr.poderjudicial.sigec.incidente.entity.EstadoIncidente;

import java.time.LocalDate;

/** Fila de listado de incidentes (proyeccion directa via JPQL, sin carga perezosa). */
public record IncidenteResumenResponse(
        Integer id,
        Integer idEquipo,
        String equipo,
        String tecnico,
        LocalDate fechaObservacion,
        LocalDate fechaReparacion,
        EstadoIncidente estado
) {
}
