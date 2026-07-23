package cr.poderjudicial.sigec.inventario.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InterfazRequest(
        @NotBlank String direccionIp,
        String mac,
        String mascara,
        String gateway,
        String dns,
        @Min(1) @Max(65535) Integer puerto
) {
}
