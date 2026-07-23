package cr.poderjudicial.sigec.incidente.service;

import cr.poderjudicial.sigec.catalogo.repository.TecnicoRepository;
import cr.poderjudicial.sigec.common.RecursoNoEncontradoException;
import cr.poderjudicial.sigec.common.ReglaNegocioException;
import cr.poderjudicial.sigec.incidente.dto.IncidenteCierreRequest;
import cr.poderjudicial.sigec.incidente.dto.IncidenteRequest;
import cr.poderjudicial.sigec.incidente.dto.IncidenteResponse;
import cr.poderjudicial.sigec.incidente.dto.IncidenteResumenResponse;
import cr.poderjudicial.sigec.incidente.entity.EstadoIncidente;
import cr.poderjudicial.sigec.incidente.entity.Incidente;
import cr.poderjudicial.sigec.incidente.mapper.IncidenteMapper;
import cr.poderjudicial.sigec.incidente.repository.IncidenteRepository;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidenteService {

    private final IncidenteRepository incidentes;
    private final EquipoRepository equipos;
    private final TecnicoRepository tecnicos;

    public IncidenteService(IncidenteRepository incidentes, EquipoRepository equipos,
                            TecnicoRepository tecnicos) {
        this.incidentes = incidentes;
        this.equipos = equipos;
        this.tecnicos = tecnicos;
    }

    @Transactional(readOnly = true)
    public Page<IncidenteResumenResponse> buscar(String estado, Integer idEquipo, Pageable pageable) {
        EstadoIncidente est = (estado != null && !estado.isBlank()) ? EstadoIncidente.valueOf(estado) : null;
        return incidentes.buscar(est, idEquipo, pageable);
    }

    @Transactional(readOnly = true)
    public IncidenteResponse obtener(Integer id) {
        return IncidenteMapper.aDetalle(buscarEntidad(id));
    }

    @Transactional
    public IncidenteResponse crear(IncidenteRequest req) {
        Incidente i = new Incidente();
        aplicar(i, req);
        i.setIdUsuarioRegistro(CurrentUser.id().orElse(null));
        return IncidenteMapper.aDetalle(incidentes.save(i));
    }

    @Transactional
    public IncidenteResponse actualizar(Integer id, IncidenteRequest req) {
        Incidente i = buscarEntidad(id);
        aplicar(i, req);
        return IncidenteMapper.aDetalle(incidentes.save(i));
    }

    @Transactional
    public IncidenteResponse cerrar(Integer id, IncidenteCierreRequest req) {
        Incidente i = buscarEntidad(id);
        if (i.getEstado() == EstadoIncidente.CERRADO) {
            throw new ReglaNegocioException("El incidente ya esta cerrado.");
        }
        if (req.fechaReparacion().isBefore(i.getFechaObservacion())) {
            throw new ReglaNegocioException(
                    "La fecha de reparacion no puede ser anterior a la fecha de observacion.");
        }
        i.setFechaReparacion(req.fechaReparacion());
        if (req.trabajoRealizado() != null && !req.trabajoRealizado().isBlank()) {
            i.setTrabajoRealizado(req.trabajoRealizado());
        }
        i.setEstado(EstadoIncidente.CERRADO);
        return IncidenteMapper.aDetalle(incidentes.save(i));
    }

    private Incidente buscarEntidad(Integer id) {
        return incidentes.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Incidente", id));
    }

    private void aplicar(Incidente i, IncidenteRequest req) {
        i.setEquipo(equipos.findById(req.idEquipo())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", req.idEquipo())));
        i.setTecnico(req.idTecnico() == null ? null : tecnicos.findById(req.idTecnico())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Tecnico", req.idTecnico())));
        i.setFechaObservacion(req.fechaObservacion());
        i.setDetalle(req.detalle());
        i.setTrabajoRealizado(req.trabajoRealizado());
        i.setMedidasATomar(req.medidasATomar());
        if (req.estado() != null && !req.estado().isBlank()) {
            i.setEstado(EstadoIncidente.valueOf(req.estado()));
        }
    }
}
