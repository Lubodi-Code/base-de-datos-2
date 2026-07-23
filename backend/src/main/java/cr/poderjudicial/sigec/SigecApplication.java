package cr.poderjudicial.sigec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * SIGEC-PJ — Sistema de Gestion de Equipos CCTV del Poder Judicial.
 *
 * <p>Arquitectura por capas (controlador -> servicio -> repositorio -> entidad),
 * organizada por modulos funcionales: seguridad, catalogo e inventario.</p>
 *
 * <p>{@code @EnableTransactionManagement(order = 0)} coloca el interceptor de
 * transacciones como el mas externo, de modo que el aspecto de auditoria
 * ({@code AuditTransactionAspect}) corra con la transaccion ya activa y pueda
 * fijar el parametro de sesion {@code app.id_usuario} que leen los triggers.</p>
 */
@SpringBootApplication
@EnableTransactionManagement(order = 0)
public class SigecApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigecApplication.class, args);
    }
}
