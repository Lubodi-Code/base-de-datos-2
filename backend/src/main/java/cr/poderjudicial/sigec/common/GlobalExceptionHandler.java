package cr.poderjudicial.sigec.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatear).toList();
        return build(HttpStatus.BAD_REQUEST, "Datos invalidos", req, detalles);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Violacion de integridad de datos", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado para su rol", req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> general(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, null);
    }

    private String formatear(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String msg, HttpServletRequest req, List<String> detalles) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), msg, req.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
