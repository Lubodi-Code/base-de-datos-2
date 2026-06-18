package cr.poderjudicial.sigec.seguridad.repository;

import cr.poderjudicial.sigec.seguridad.entity.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Integer> {

    Optional<UsuarioSistema> findByUsername(String username);

    boolean existsByUsername(String username);
}
