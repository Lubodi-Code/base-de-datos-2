package cr.poderjudicial.sigec.common;

/** Se lanza cuando no existe el recurso solicitado (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException de(String entidad, Object id) {
        return new RecursoNoEncontradoException(entidad + " no encontrado(a): " + id);
    }
}
