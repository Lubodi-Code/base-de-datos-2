package cr.poderjudicial.sigec.dashboard.service;

import cr.poderjudicial.sigec.dashboard.dto.ConteoDto;
import cr.poderjudicial.sigec.dashboard.dto.DashboardResumen;
import cr.poderjudicial.sigec.incidente.entity.EstadoIncidente;
import cr.poderjudicial.sigec.incidente.repository.IncidenteRepository;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.inventario.repository.VistaGarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ensambla los indicadores del tablero a partir de consultas de agregacion. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final EquipoRepository equipos;
    private final IncidenteRepository incidentes;
    private final VistaGarantiaRepository garantias;

    public DashboardService(EquipoRepository equipos, IncidenteRepository incidentes,
                            VistaGarantiaRepository garantias) {
        this.equipos = equipos;
        this.incidentes = incidentes;
        this.garantias = garantias;
    }

    public DashboardResumen resumen() {
        List<ConteoDto> equiposPorEstado = aConteo(equipos.contarPorEstado());
        List<ConteoDto> equiposPorProvincia = equiposPorProvincia();
        List<ConteoDto> incidentesPorEstado = aConteo(incidentes.contarPorEstado());

        long totalEquipos = equiposPorEstado.stream().mapToLong(ConteoDto::total).sum();

        return new DashboardResumen(
                totalEquipos,
                equiposPorEstado,
                equiposPorProvincia,
                incidentes.countByEstado(EstadoIncidente.ABIERTO),
                incidentesPorEstado,
                garantias.count(),
                incidentes.tiempoMedioReparacionDias());
    }

    public List<ConteoDto> equiposPorProvincia() {
        return aConteo(equipos.contarPorProvincia());
    }

    public List<ConteoDto> incidentesPorEstado() {
        return aConteo(incidentes.contarPorEstado());
    }

    /** Convierte filas [etiqueta, total] (donde la etiqueta puede ser enum o texto). */
    private List<ConteoDto> aConteo(List<Object[]> filas) {
        return filas.stream()
                .map(f -> new ConteoDto(
                        f[0] == null ? "(sin dato)" : f[0].toString(),
                        ((Number) f[1]).longValue()))
                .toList();
    }
}
