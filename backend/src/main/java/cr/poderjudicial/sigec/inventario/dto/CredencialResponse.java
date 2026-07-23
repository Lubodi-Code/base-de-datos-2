package cr.poderjudicial.sigec.inventario.dto;

/** Metadatos de credencial (NUNCA expone el secreto). */
public record CredencialResponse(
        Integer id,
        Integer idEquipo,
        String tipo,
        String usuario
) {
}
