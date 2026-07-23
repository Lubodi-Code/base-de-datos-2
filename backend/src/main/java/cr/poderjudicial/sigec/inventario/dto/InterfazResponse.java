package cr.poderjudicial.sigec.inventario.dto;

public record InterfazResponse(
        Integer id,
        String direccionIp,
        String mac,
        String mascara,
        String gateway,
        String dns,
        Integer puerto
) {
}
