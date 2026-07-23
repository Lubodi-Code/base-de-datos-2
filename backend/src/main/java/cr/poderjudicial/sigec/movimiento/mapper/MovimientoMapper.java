package cr.poderjudicial.sigec.movimiento.mapper;

import cr.poderjudicial.sigec.catalogo.entity.Tecnico;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.movimiento.dto.MovimientoResponse;
import cr.poderjudicial.sigec.movimiento.entity.MovimientoEquipo;

/** Conversion entidad -> DTO. Debe invocarse dentro de una transaccion (lee asociaciones perezosas). */
public final class MovimientoMapper {

    private MovimientoMapper() {
    }

    public static MovimientoResponse aDetalle(MovimientoEquipo m) {
        Equipo eq = m.getEquipo();
        Tecnico tec = m.getTecnico();
        Equipo sust = m.getEquipoSustituto();
        return new MovimientoResponse(
                m.getId(),
                eq.getId(),
                etiquetaEquipo(eq),
                m.getTipo().name(),
                m.getFecha(),
                m.getMotivo(),
                tec != null ? tec.getId() : null,
                tec != null ? tec.getNombre() : null,
                m.getIdUsuario(),
                sust != null ? sust.getId() : null,
                sust != null ? etiquetaEquipo(sust) : null);
    }

    private static String etiquetaEquipo(Equipo e) {
        return e.getNumActivo() != null
                ? e.getNombreEquipo() + " (" + e.getNumActivo() + ")"
                : e.getNombreEquipo();
    }
}
