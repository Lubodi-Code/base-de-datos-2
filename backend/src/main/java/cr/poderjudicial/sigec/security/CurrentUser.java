package cr.poderjudicial.sigec.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acceso utilitario al usuario autenticado en el hilo actual. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<UsuarioAutenticado> get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public static Optional<Integer> id() {
        return get().map(UsuarioAutenticado::id);
    }
}
