package cr.poderjudicial.sigec.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EquipoRequest(
        @NotNull Integer idModelo,
        @NotNull Integer idUbicacion,
        Short idPlataforma,
        Integer idContratacion,
        @NotBlank @Size(max = 80) String nombreEquipo,
        @Size(max = 60) String serie,
        @Size(max = 20) String numActivo,
        @Pattern(regexp = "ACTIVO|DANADO|RETIRADO|REEMPLAZADO",
                message = "estado invalido") String estado,
        @Size(max = 40) String verWindows,
        @Size(max = 60) String licenciaWin,
        LocalDate fechaInstalacion,
        LocalDate vencGarantia
) {
}
