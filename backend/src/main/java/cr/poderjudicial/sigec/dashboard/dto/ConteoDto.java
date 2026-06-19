package cr.poderjudicial.sigec.dashboard.dto;

/** Par etiqueta/total para series del tablero (por estado, por provincia, etc.). */
public record ConteoDto(
        String etiqueta,
        long total
) {
}
