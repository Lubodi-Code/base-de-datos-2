package cr.poderjudicial.sigec.seguridad.controller;

import cr.poderjudicial.sigec.seguridad.dto.LoginRequest;
import cr.poderjudicial.sigec.seguridad.dto.LoginResponse;
import cr.poderjudicial.sigec.seguridad.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
