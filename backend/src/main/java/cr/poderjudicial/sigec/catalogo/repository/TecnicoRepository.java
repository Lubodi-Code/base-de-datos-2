package cr.poderjudicial.sigec.catalogo.repository;

import cr.poderjudicial.sigec.catalogo.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoRepository extends JpaRepository<Tecnico, Integer> {
}
