package cr.poderjudicial.sigec.audit;

import cr.poderjudicial.sigec.security.CurrentUser;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Propaga el usuario autenticado a la sesion de PostgreSQL para que los triggers
 * de auditoria (fn_auditoria) sepan QUIEN ejecuto el cambio.
 *
 * <p>Se ejecuta DENTRO de la transaccion abierta por {@code @Transactional}
 * (por eso el interceptor de transacciones se configura con order = 0, mas
 * externo, y este aspecto con la menor precedencia, mas interno). El SET es
 * LOCAL: vive solo lo que dura la transaccion, evitando fugas entre conexiones
 * del pool.</p>
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditTransactionAspect {

    private final JdbcTemplate jdbc;

    public AuditTransactionAspect(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Before("@within(org.springframework.transaction.annotation.Transactional) "
            + "|| @annotation(org.springframework.transaction.annotation.Transactional)")
    public void fijarUsuarioDeSesion() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        CurrentUser.id().ifPresent(id ->
                jdbc.queryForObject("SELECT set_config('app.id_usuario', ?, true)",
                        String.class, String.valueOf(id)));
    }
}
