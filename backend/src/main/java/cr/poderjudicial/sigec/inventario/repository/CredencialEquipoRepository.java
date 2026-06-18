package cr.poderjudicial.sigec.inventario.repository;

import cr.poderjudicial.sigec.inventario.entity.CredencialEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CredencialEquipoRepository extends JpaRepository<CredencialEquipo, Integer> {
    List<CredencialEquipo> findByEquipoId(Integer idEquipo);

    Optional<CredencialEquipo> findByEquipoIdAndTipo(Integer idEquipo, String tipo);
}
