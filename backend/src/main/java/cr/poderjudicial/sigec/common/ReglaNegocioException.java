package cr.poderjudicial.sigec.common;

/** Se lanza cuando una operacion viola una regla de negocio (HTTP 409). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
