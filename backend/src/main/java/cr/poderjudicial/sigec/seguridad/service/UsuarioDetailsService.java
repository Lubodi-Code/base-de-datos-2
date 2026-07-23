package cr.poderjudicial.sigec.seguridad.service;

import cr.poderjudicial.sigec.seguridad.repository.UsuarioSistemaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Carga usuarios para el flujo de login (DaoAuthenticationProvider). */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioSistemaRepository repo;

    public UsuarioDetailsService(UsuarioSistemaRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getHashPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .disabled(!u.isActivo())
                .build();
    }
}
