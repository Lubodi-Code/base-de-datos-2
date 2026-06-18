package cr.poderjudicial.sigec.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CredencialRequest(
        @NotBlank @Pattern(regexp = "SO|VMS|TOOLBOX", message = "tipo debe ser SO, VMS o TOOLBOX") String tipo,
        @Size(max = 60) String usuario,
        @NotBlank String secreto
) {
}
