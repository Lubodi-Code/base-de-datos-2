package cr.poderjudicial.sigec.inventario.repository;

import cr.poderjudicial.sigec.inventario.entity.InterfazRed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterfazRedRepository extends JpaRepository<InterfazRed, Integer> {
    List<InterfazRed> findByEquipoId(Integer idEquipo);
}
