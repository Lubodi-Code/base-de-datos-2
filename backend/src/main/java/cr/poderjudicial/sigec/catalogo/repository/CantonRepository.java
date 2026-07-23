package cr.poderjudicial.sigec.catalogo.repository;

import cr.poderjudicial.sigec.catalogo.entity.Canton;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CantonRepository extends JpaRepository<Canton, Integer> {
    List<Canton> findByProvinciaIdOrderByNombre(Short idProvincia);
}
