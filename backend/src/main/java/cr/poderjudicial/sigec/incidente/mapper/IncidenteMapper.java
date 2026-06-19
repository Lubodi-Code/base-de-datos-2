package cr.poderjudicial.sigec.incidente.mapper;

import cr.poderjudicial.sigec.catalogo.entity.Tecnico;
import cr.poderjudicial.sigec.incidente.dto.IncidenteResponse;
import cr.poderjudicial.sigec.incidente.entity.Incidente;
import cr.poderjudicial.sigec.inventario.entity.Equipo;

/** Conversion entidad -> DTO. Debe invocarse dentro de una transaccion (lee asociaciones perezosas). */
public final class IncidenteMapper {

    private IncidenteMapper() {
    }

    public static IncidenteResponse aDetalle(Incidente i) {
        Equipo eq = i.getEquipo();
        Tecnico tec = i.getTecnico();
        return new IncidenteResponse(
                i.getId(),
                eq.getId(),
                etiquetaEquipo(eq),
                tec != null ? tec.getId() : null,
                tec != null ? tec.getNombre() : null,
                i.getFechaObservacion(),
                i.getFechaReparacion(),
                i.getDetalle(),
                i.getTrabajoRealizado(),
                i.getEstado().name(),
                i.getMedidasATomar(),
                i.getIdUsuarioRegistro());
    }

    private static String etiquetaEquipo(Equipo e) {
        return e.getNumActivo() != null
                ? e.getNombreEquipo() + " (" + e.getNumActivo() + ")"
                : e.getNombreEquipo();
    }
}
