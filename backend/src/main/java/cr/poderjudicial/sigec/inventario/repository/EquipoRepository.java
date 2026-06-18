package cr.poderjudicial.sigec.inventario.repository;

import cr.poderjudicial.sigec.inventario.dto.EquipoResumenResponse;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.entity.EstadoEquipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    boolean existsByNumActivo(String numActivo);

    /**
     * Busqueda paginada con filtros opcionales (todos pueden ir nulos).
     * Proyecta directamente a EquipoResumenResponse para evitar carga perezosa (N+1).
     * El filtro por IP usa EXISTS para no multiplicar filas en la paginacion.
     */
    @Query(value = """
            select new cr.poderjudicial.sigec.inventario.dto.EquipoResumenResponse(
                e.id, e.nombreEquipo, e.numActivo, e.serie, e.estado,
                concat(m.marca, ' ', m.modelo), m.tipo,
                concat(ed.cuentaPj, ' / ', pr.nombre), e.vencGarantia)
            from Equipo e
                join e.modelo m
                join e.ubicacion u
                join u.edificio ed
                join ed.canton c
                join c.provincia pr
            where (:estado is null or e.estado = :estado)
              and (:idProvincia is null or pr.id = :idProvincia)
              and (:texto is null
                   or lower(e.nombreEquipo) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(e.numActivo, '')) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(e.serie, '')) like lower(concat('%', cast(:texto as string), '%')))
              and (:ip is null or exists (
                   select 1 from InterfazRed i
                   where i.equipo = e and function('host', i.direccionIp) = cast(:ip as string)))
            """,
            countQuery = """
            select count(e)
            from Equipo e
                join e.ubicacion u
                join u.edificio ed
                join ed.canton c
                join c.provincia pr
            where (:estado is null or e.estado = :estado)
              and (:idProvincia is null or pr.id = :idProvincia)
              and (:texto is null
                   or lower(e.nombreEquipo) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(e.numActivo, '')) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(e.serie, '')) like lower(concat('%', cast(:texto as string), '%')))
              and (:ip is null or exists (
                   select 1 from InterfazRed i
                   where i.equipo = e and function('host', i.direccionIp) = cast(:ip as string)))
            """)
    Page<EquipoResumenResponse> buscar(@Param("estado") EstadoEquipo estado,
                                       @Param("idProvincia") Short idProvincia,
                                       @Param("texto") String texto,
                                       @Param("ip") String ip,
                                       Pageable pageable);
}
