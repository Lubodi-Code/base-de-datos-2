package cr.poderjudicial.sigec.inventario.mapper;

import cr.poderjudicial.sigec.catalogo.entity.Contratacion;
import cr.poderjudicial.sigec.catalogo.entity.Edificio;
import cr.poderjudicial.sigec.catalogo.entity.UbicacionFisica;
import cr.poderjudicial.sigec.inventario.dto.EquipoResponse;
import cr.poderjudicial.sigec.inventario.dto.InterfazResponse;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.entity.InterfazRed;

import java.util.List;
import java.util.stream.Stream;

/** Conversion entidad -> DTO. Debe invocarse dentro de una transaccion (lee asociaciones perezosas). */
public final class EquipoMapper {

    private EquipoMapper() {
    }

    public static EquipoResponse aDetalle(Equipo e) {
        UbicacionFisica ubic = e.getUbicacion();
        Edificio edif = ubic.getEdificio();
        var provincia = edif.getCanton().getProvincia();
        var modelo = e.getModelo();

        List<InterfazResponse> interfaces = e.getInterfaces().stream()
                .map(EquipoMapper::aInterfaz)
                .toList();

        return new EquipoResponse(
                e.getId(),
                e.getNombreEquipo(),
                e.getSerie(),
                e.getNumActivo(),
                e.getEstado().name(),
                modelo.getId(),
                modelo.getMarca() + " " + modelo.getModelo(),
                modelo.getTipo(),
                ubic.getId(),
                etiquetaUbicacion(ubic),
                edif.getCuentaPj() + " - " + edif.getNombre(),
                provincia.getNombre(),
                e.getPlataforma() != null ? e.getPlataforma().getId() : null,
                e.getPlataforma() != null ? e.getPlataforma().getNombre() : null,
                e.getContratacion() != null ? e.getContratacion().getId() : null,
                e.getContratacion() != null ? etiquetaContratacion(e.getContratacion()) : null,
                e.getVerWindows(),
                e.getLicenciaWin(),
                e.getFechaInstalacion(),
                e.getVencGarantia(),
                e.getIdUsuarioRegistro(),
                interfaces);
    }

    public static InterfazResponse aInterfaz(InterfazRed i) {
        return new InterfazResponse(i.getId(), i.getDireccionIp(), i.getMac(),
                i.getMascara(), i.getGateway(), i.getDns(), i.getPuerto());
    }

    private static String etiquetaUbicacion(UbicacionFisica u) {
        return Stream.of(
                        u.getPiso() != null ? "Piso " + u.getPiso() : null,
                        u.getRack() != null ? "Rack " + u.getRack() : null,
                        u.getPuertoSwitch() != null ? "Puerto " + u.getPuertoSwitch() : null)
                .filter(s -> s != null)
                .reduce((a, b) -> a + " / " + b)
                .orElse("Ubicacion " + u.getId());
    }

    private static String etiquetaContratacion(Contratacion c) {
        if (c.getNumSicop() != null) {
            return c.getNumSicop();
        }
        return c.getOrdenPedido() != null ? c.getOrdenPedido() : "Contratacion " + c.getId();
    }
}
