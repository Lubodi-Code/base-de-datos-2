package cr.poderjudicial.sigec.seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
        @NotBlank @Size(max = 40) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 120) String nombreCompleto,
        @NotBlank @Pattern(regexp = "ADMIN|TECNICO|CONSULTA",
                message = "debe ser ADMIN, TECNICO o CONSULTA") String rol,
        Integer idTecnico
) {
}
