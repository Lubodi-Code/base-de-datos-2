package cr.poderjudicial.sigec.inventario.dto;

/** Credencial descifrada. Solo accesible por ADMIN y queda registrado en bitacora. */
public record CredencialSecretoResponse(
        Integer id,
        String tipo,
        String usuario,
        String secreto
) {
}
