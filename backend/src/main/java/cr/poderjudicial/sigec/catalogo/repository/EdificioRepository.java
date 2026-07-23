package cr.poderjudicial.sigec.catalogo.repository;

import cr.poderjudicial.sigec.catalogo.entity.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EdificioRepository extends JpaRepository<Edificio, Integer> {
    List<Edificio> findByCantonIdOrderByNombre(Integer idCanton);
}
