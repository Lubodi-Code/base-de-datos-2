package cr.poderjudicial.sigec.common;

import java.time.OffsetDateTime;
import java.util.List;

/** Cuerpo de respuesta uniforme para errores de la API. */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detalles
) {
    public static ApiError of(int status, String error, String message, String path, List<String> detalles) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, detalles);
    }
}
