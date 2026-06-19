package cr.poderjudicial.sigec.dashboard.controller;

import cr.poderjudicial.sigec.dashboard.dto.ConteoDto;
import cr.poderjudicial.sigec.dashboard.dto.DashboardResumen;
import cr.poderjudicial.sigec.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Indicadores del tablero (solo lectura; cualquier usuario autenticado). */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public DashboardResumen resumen() {
        return dashboardService.resumen();
    }

    @GetMapping("/equipos-por-provincia")
    public List<ConteoDto> equiposPorProvincia() {
        return dashboardService.equiposPorProvincia();
    }

    @GetMapping("/incidentes-por-estado")
    public List<ConteoDto> incidentesPorEstado() {
        return dashboardService.incidentesPorEstado();
    }
}
