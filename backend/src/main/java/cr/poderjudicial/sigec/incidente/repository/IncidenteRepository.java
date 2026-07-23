package cr.poderjudicial.sigec.incidente.repository;

import cr.poderjudicial.sigec.incidente.dto.IncidenteResumenResponse;
import cr.poderjudicial.sigec.incidente.entity.EstadoIncidente;
import cr.poderjudicial.sigec.incidente.entity.Incidente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidenteRepository extends JpaRepository<Incidente, Integer> {

    /**
     * Busqueda paginada con filtros opcionales (estado, equipo).
     * Proyecta directamente a IncidenteResumenResponse para evitar carga perezosa (N+1).
     */
    @Query("""
            select new cr.poderjudicial.sigec.incidente.dto.IncidenteResumenResponse(
                i.id, e.id,
                case when e.numActivo is null then e.nombreEquipo
                     else concat(e.nombreEquipo, ' (', e.numActivo, ')') end,
                t.nombre, i.fechaObservacion, i.fechaReparacion, i.estado)
            from Incidente i
                join i.equipo e
                left join i.tecnico t
            where (:estado is null or i.estado = :estado)
              and (:idEquipo is null or e.id = :idEquipo)
            """)
    Page<IncidenteResumenResponse> buscar(@Param("estado") EstadoIncidente estado,
                                          @Param("idEquipo") Integer idEquipo,
                                          Pageable pageable);

    // ---- Agregaciones para el dashboard ----

    long countByEstado(EstadoIncidente estado);

    /** Conteo de incidentes agrupados por estado: [estado, total]. */
    @Query("select i.estado, count(i) from Incidente i group by i.estado")
    List<Object[]> contarPorEstado();

    /**
     * Tiempo medio de reparacion en dias sobre incidentes cerrados con ambas fechas.
     * Nativa porque la resta de fechas es propia de PostgreSQL.
     */
    @Query(value = """
            select avg(fecha_reparacion - fecha_observacion)
            from incidente
            where estado = 'CERRADO' and fecha_reparacion is not null
            """, nativeQuery = true)
    Double tiempoMedioReparacionDias();
}
