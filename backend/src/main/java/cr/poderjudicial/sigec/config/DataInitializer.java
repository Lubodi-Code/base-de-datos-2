package cr.poderjudicial.sigec.config;

import cr.poderjudicial.sigec.seguridad.entity.Rol;
import cr.poderjudicial.sigec.seguridad.entity.UsuarioSistema;
import cr.poderjudicial.sigec.seguridad.repository.UsuarioSistemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea un usuario ADMIN inicial si la tabla esta vacia, para poder iniciar sesion
 * en ambiente de demostracion. Desactivar en produccion con
 * {@code sigec.bootstrap-admin=false}.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioSistemaRepository usuarios;
    private final PasswordEncoder encoder;
    private final boolean habilitado;

    public DataInitializer(UsuarioSistemaRepository usuarios, PasswordEncoder encoder,
                           @Value("${sigec.bootstrap-admin:true}") boolean habilitado) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.habilitado = habilitado;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!habilitado || usuarios.count() > 0) {
            return;
        }
        var admin = new UsuarioSistema();
        admin.setUsername("admin");
        admin.setHashPassword(encoder.encode("admin123"));
        admin.setNombreCompleto("Administrador SIGEC");
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarios.save(admin);
        log.warn("Usuario ADMIN inicial creado: admin / admin123 (CAMBIAR en produccion).");
    }
}
