package cr.poderjudicial.sigec.security;

/** Principal almacenado en el SecurityContext tras validar el JWT. */
public record UsuarioAutenticado(Integer id, String username, String rol) {
}
