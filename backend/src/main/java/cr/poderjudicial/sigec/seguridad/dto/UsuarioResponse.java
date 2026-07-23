package cr.poderjudicial.sigec.seguridad.dto;

import cr.poderjudicial.sigec.seguridad.entity.UsuarioSistema;

public record UsuarioResponse(
        Integer id,
        String username,
        String nombreCompleto,
        String rol,
        boolean activo,
        Integer idTecnico
) {
    public static UsuarioResponse de(UsuarioSistema u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombreCompleto(),
                u.getRol().name(), u.isActivo(), u.getIdTecnico());
    }
}
