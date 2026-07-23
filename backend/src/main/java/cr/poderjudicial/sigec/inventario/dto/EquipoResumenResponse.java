package cr.poderjudicial.sigec.inventario.dto;

import cr.poderjudicial.sigec.inventario.entity.EstadoEquipo;

import java.time.LocalDate;

/** Fila de listado de equipos (proyeccion directa via JPQL, sin carga perezosa). */
public record EquipoResumenResponse(
        Integer id,
        String nombreEquipo,
        String numActivo,
        String serie,
        EstadoEquipo estado,
        String modelo,
        String tipo,
        String ubicacion,
        LocalDate vencGarantia
) {
}
