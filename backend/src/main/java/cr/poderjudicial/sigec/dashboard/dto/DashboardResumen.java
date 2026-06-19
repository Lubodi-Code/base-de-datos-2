package cr.poderjudicial.sigec.dashboard.dto;

import java.util.List;

/** Indicadores agregados para el tablero (doc 5.4 "Tablero"). */
public record DashboardResumen(
        long totalEquipos,
        List<ConteoDto> equiposPorEstado,
        List<ConteoDto> equiposPorProvincia,
        long incidentesAbiertos,
        List<ConteoDto> incidentesPorEstado,
        long garantiasPorVencer,
        Double tiempoMedioReparacionDias
) {
}
