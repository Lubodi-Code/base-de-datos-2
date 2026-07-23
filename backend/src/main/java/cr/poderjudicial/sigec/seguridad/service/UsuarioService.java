package cr.poderjudicial.sigec.seguridad.service;

import cr.poderjudicial.sigec.common.ReglaNegocioException;
import cr.poderjudicial.sigec.seguridad.dto.CrearUsuarioRequest;
import cr.poderjudicial.sigec.seguridad.dto.UsuarioResponse;
import cr.poderjudicial.sigec.seguridad.entity.Rol;
import cr.poderjudicial.sigec.seguridad.entity.UsuarioSistema;
import cr.poderjudicial.sigec.seguridad.repository.UsuarioSistemaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioSistemaRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioSistemaRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest req) {
        if (repo.existsByUsername(req.username())) {
            throw new ReglaNegocioException("El username ya esta en uso: " + req.username());
        }
        var u = new UsuarioSistema();
        u.setUsername(req.username());
        u.setHashPassword(passwordEncoder.encode(req.password()));   // BCrypt
        u.setNombreCompleto(req.nombreCompleto());
        u.setRol(Rol.valueOf(req.rol()));
        u.setActivo(true);
        u.setIdTecnico(req.idTecnico());
        return UsuarioResponse.de(repo.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(UsuarioResponse::de).toList();
    }
}
