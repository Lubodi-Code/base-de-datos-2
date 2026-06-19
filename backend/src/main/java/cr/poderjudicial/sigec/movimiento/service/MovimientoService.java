package cr.poderjudicial.sigec.movimiento.service;

import cr.poderjudicial.sigec.catalogo.repository.TecnicoRepository;
import cr.poderjudicial.sigec.common.RecursoNoEncontradoException;
import cr.poderjudicial.sigec.common.ReglaNegocioException;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.entity.EstadoEquipo;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.movimiento.dto.MovimientoRequest;
import cr.poderjudicial.sigec.movimiento.dto.MovimientoResponse;
import cr.poderjudicial.sigec.movimiento.dto.MovimientoResumenResponse;
import cr.poderjudicial.sigec.movimiento.entity.MovimientoEquipo;
import cr.poderjudicial.sigec.movimiento.entity.TipoMovimiento;
import cr.poderjudicial.sigec.movimiento.mapper.MovimientoMapper;
import cr.poderjudicial.sigec.movimiento.repository.MovimientoRepository;
import cr.poderjudicial.sigec.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientos;
    private final EquipoRepository equipos;
    private final TecnicoRepository tecnicos;

    public MovimientoService(MovimientoRepository movimientos, EquipoRepository equipos,
                             TecnicoRepository tecnicos) {
        this.movimientos = movimientos;
        this.equipos = equipos;
        this.tecnicos = tecnicos;
    }

    @Transactional(readOnly = true)
    public Page<MovimientoResumenResponse> buscar(Integer idEquipo, Pageable pageable) {
        return movimientos.buscar(idEquipo, pageable);
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Integer id) {
        MovimientoEquipo m = movimientos.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Movimiento", id));
        return MovimientoMapper.aDetalle(m);
    }

    @Transactional
    public MovimientoResponse registrar(MovimientoRequest req) {
        Integer idUsuario = CurrentUser.id()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay un usuario autenticado para registrar el movimiento."));

        TipoMovimiento tipo = TipoMovimiento.valueOf(req.tipo());
        Equipo equipo = equipos.findById(req.idEquipo())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", req.idEquipo()));

        Equipo sustituto = resolverSustituto(tipo, req, equipo);

        MovimientoEquipo m = new MovimientoEquipo();
        m.setEquipo(equipo);
        m.setTipo(tipo);
        m.setMotivo(req.motivo());
        m.setIdUsuario(idUsuario);
        m.setEquipoSustituto(sustituto);
        m.setTecnico(req.idTecnico() == null ? null : tecnicos.findById(req.idTecnico())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Tecnico", req.idTecnico())));

        // Efecto de dominio sobre el equipo segun el tipo de movimiento.
        switch (tipo) {
            case BAJA -> equipo.setEstado(EstadoEquipo.RETIRADO);
            case REEMPLAZO -> equipo.setEstado(EstadoEquipo.REEMPLAZADO);
            case TRASLADO -> { /* sin cambio de estado; el traslado de ubicacion lo hace inventario */ }
        }

        return MovimientoMapper.aDetalle(movimientos.save(m));
    }

    /** Valida y resuelve el equipo sustituto segun las reglas del DDL (CHECK de movimiento_equipo). */
    private Equipo resolverSustituto(TipoMovimiento tipo, MovimientoRequest req, Equipo equipo) {
        if (tipo != TipoMovimiento.REEMPLAZO) {
            if (req.idEquipoSustituto() != null) {
                throw new ReglaNegocioException(
                        "El equipo sustituto solo aplica en movimientos de tipo REEMPLAZO.");
            }
            return null;
        }
        if (req.idEquipoSustituto() == null) {
            throw new ReglaNegocioException(
                    "El movimiento REEMPLAZO requiere un equipo sustituto.");
        }
        if (req.idEquipoSustituto().equals(equipo.getId())) {
            throw new ReglaNegocioException(
                    "El equipo sustituto no puede ser el mismo equipo reemplazado.");
        }
        return equipos.findById(req.idEquipoSustituto())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo sustituto", req.idEquipoSustituto()));
    }
}
