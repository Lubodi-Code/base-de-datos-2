package cr.poderjudicial.sigec.inventario.service;

import cr.poderjudicial.sigec.common.RecursoNoEncontradoException;
import cr.poderjudicial.sigec.inventario.dto.CredencialRequest;
import cr.poderjudicial.sigec.inventario.dto.CredencialResponse;
import cr.poderjudicial.sigec.inventario.dto.CredencialSecretoResponse;
import cr.poderjudicial.sigec.inventario.entity.CredencialEquipo;
import cr.poderjudicial.sigec.inventario.repository.CredencialEquipoRepository;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.security.AesGcmCipherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CredencialService {

    private final CredencialEquipoRepository credenciales;
    private final EquipoRepository equipos;
    private final AesGcmCipherService cipher;

    public CredencialService(CredencialEquipoRepository credenciales, EquipoRepository equipos,
                             AesGcmCipherService cipher) {
        this.credenciales = credenciales;
        this.equipos = equipos;
        this.cipher = cipher;
    }

    @Transactional(readOnly = true)
    public List<CredencialResponse> listar(Integer idEquipo) {
        return credenciales.findByEquipoId(idEquipo).stream().map(this::aResponse).toList();
    }

    /** Crea o reemplaza la credencial (idEquipo, tipo) con el secreto cifrado en AES-256-GCM. */
    @Transactional
    public CredencialResponse guardar(Integer idEquipo, CredencialRequest req) {
        var equipo = equipos.findById(idEquipo)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", idEquipo));
        var cred = credenciales.findByEquipoIdAndTipo(idEquipo, req.tipo())
                .orElseGet(CredencialEquipo::new);
        cred.setEquipo(equipo);
        cred.setTipo(req.tipo());
        cred.setUsuario(req.usuario());
        cred.setSecretoCifrado(cipher.cifrar(req.secreto()));
        return aResponse(credenciales.save(cred));
    }

    /** Devuelve el secreto descifrado. Restringido a ADMIN en el controlador. */
    @Transactional(readOnly = true)
    public CredencialSecretoResponse revelar(Integer idCredencial) {
        var cred = credenciales.findById(idCredencial)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Credencial", idCredencial));
        return new CredencialSecretoResponse(cred.getId(), cred.getTipo(), cred.getUsuario(),
                cipher.descifrar(cred.getSecretoCifrado()));
    }

    private CredencialResponse aResponse(CredencialEquipo c) {
        return new CredencialResponse(c.getId(), c.getEquipo().getId(), c.getTipo(), c.getUsuario());
    }
}
