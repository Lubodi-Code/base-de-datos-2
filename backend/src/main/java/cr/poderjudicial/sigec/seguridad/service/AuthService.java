package cr.poderjudicial.sigec.seguridad.service;

import cr.poderjudicial.sigec.security.JwtService;
import cr.poderjudicial.sigec.seguridad.dto.LoginRequest;
import cr.poderjudicial.sigec.seguridad.dto.LoginResponse;
import cr.poderjudicial.sigec.seguridad.repository.UsuarioSistemaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UsuarioSistemaRepository repo;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authManager, UsuarioSistemaRepository repo, JwtService jwtService) {
        this.authManager = authManager;
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest req) {
        // Lanza AuthenticationException si las credenciales no son validas (-> 401).
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var u = repo.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException(req.username()));
        String token = jwtService.generar(u.getId(), u.getUsername(), u.getRol().name());
        return new LoginResponse(token, "Bearer",
                jwtService.getExpirationMinutes() * 60, u.getUsername(), u.getRol().name());
    }
}
