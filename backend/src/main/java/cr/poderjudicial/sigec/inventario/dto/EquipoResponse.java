package cr.poderjudicial.sigec.inventario.dto;

import java.time.LocalDate;
import java.util.List;

/** Detalle completo de un equipo. */
public record EquipoResponse(
        Integer id,
        String nombreEquipo,
        String serie,
        String numActivo,
        String estado,
        Integer idModelo,
        String modelo,
        String tipo,
        Integer idUbicacion,
        String ubicacion,
        String edificio,
        String provincia,
        Short idPlataforma,
        String plataforma,
        Integer idContratacion,
        String contratacion,
        String verWindows,
        String licenciaWin,
        LocalDate fechaInstalacion,
        LocalDate vencGarantia,
        Integer idUsuarioRegistro,
        List<InterfazResponse> interfaces
) {
}
