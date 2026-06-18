package cr.poderjudicial.sigec.catalogo.repository;

import cr.poderjudicial.sigec.catalogo.entity.UbicacionFisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbicacionFisicaRepository extends JpaRepository<UbicacionFisica, Integer> {
    List<UbicacionFisica> findByEdificioId(Integer idEdificio);
}
