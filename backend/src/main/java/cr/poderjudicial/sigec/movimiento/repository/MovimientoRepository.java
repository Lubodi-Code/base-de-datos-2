package cr.poderjudicial.sigec.movimiento.repository;

import cr.poderjudicial.sigec.movimiento.dto.MovimientoResumenResponse;
import cr.poderjudicial.sigec.movimiento.entity.MovimientoEquipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoRepository extends JpaRepository<MovimientoEquipo, Integer> {

    /**
     * Busqueda paginada con filtro opcional por equipo.
     * Proyecta directamente a MovimientoResumenResponse para evitar carga perezosa (N+1).
     */
    @Query("""
            select new cr.poderjudicial.sigec.movimiento.dto.MovimientoResumenResponse(
                m.id, e.id,
                case when e.numActivo is null then e.nombreEquipo
                     else concat(e.nombreEquipo, ' (', e.numActivo, ')') end,
                m.tipo, m.fecha, t.nombre)
            from MovimientoEquipo m
                join m.equipo e
                left join m.tecnico t
            where (:idEquipo is null or e.id = :idEquipo)
            """)
    Page<MovimientoResumenResponse> buscar(@Param("idEquipo") Integer idEquipo, Pageable pageable);
}
