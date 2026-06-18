package cr.poderjudicial.sigec.inventario.service;

import cr.poderjudicial.sigec.catalogo.repository.ContratacionRepository;
import cr.poderjudicial.sigec.catalogo.repository.ModeloEquipoRepository;
import cr.poderjudicial.sigec.catalogo.repository.PlataformaVmsRepository;
import cr.poderjudicial.sigec.catalogo.repository.UbicacionFisicaRepository;
import cr.poderjudicial.sigec.common.RecursoNoEncontradoException;
import cr.poderjudicial.sigec.inventario.dto.EquipoRequest;
import cr.poderjudicial.sigec.inventario.dto.EquipoResponse;
import cr.poderjudicial.sigec.inventario.dto.EquipoResumenResponse;
import cr.poderjudicial.sigec.inventario.dto.InterfazRequest;
import cr.poderjudicial.sigec.inventario.dto.InterfazResponse;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.entity.EstadoEquipo;
import cr.poderjudicial.sigec.inventario.entity.InterfazRed;
import cr.poderjudicial.sigec.inventario.entity.VistaGarantia;
import cr.poderjudicial.sigec.inventario.mapper.EquipoMapper;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.inventario.repository.InterfazRedRepository;
import cr.poderjudicial.sigec.inventario.repository.VistaGarantiaRepository;
import cr.poderjudicial.sigec.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipos;
    private final ModeloEquipoRepository modelos;
    private final UbicacionFisicaRepository ubicaciones;
    private final PlataformaVmsRepository plataformas;
    private final ContratacionRepository contrataciones;
    private final InterfazRedRepository interfaces;
    private final VistaGarantiaRepository garantias;

    public EquipoService(EquipoRepository equipos, ModeloEquipoRepository modelos,
                         UbicacionFisicaRepository ubicaciones, PlataformaVmsRepository plataformas,
                         ContratacionRepository contrataciones, InterfazRedRepository interfaces,
                         VistaGarantiaRepository garantias) {
        this.equipos = equipos;
        this.modelos = modelos;
        this.ubicaciones = ubicaciones;
        this.plataformas = plataformas;
        this.contrataciones = contrataciones;
        this.interfaces = interfaces;
        this.garantias = garantias;
    }

    @Transactional(readOnly = true)
    public Page<EquipoResumenResponse> buscar(String estado, Short idProvincia,
                                              String texto, String ip, Pageable pageable) {
        EstadoEquipo est = (estado != null && !estado.isBlank()) ? EstadoEquipo.valueOf(estado) : null;
        String t = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        String ipf = (ip != null && !ip.isBlank()) ? ip.trim() : null;
        return equipos.buscar(est, idProvincia, t, ipf, pageable);
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Integer id) {
        Equipo e = equipos.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", id));
        return EquipoMapper.aDetalle(e);
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest req) {
        Equipo e = new Equipo();
        aplicar(e, req);
        e.setIdUsuarioRegistro(CurrentUser.id().orElse(null));
        return EquipoMapper.aDetalle(equipos.save(e));
    }

    @Transactional
    public EquipoResponse actualizar(Integer id, EquipoRequest req) {
        Equipo e = equipos.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", id));
        aplicar(e, req);
        return EquipoMapper.aDetalle(equipos.save(e));
    }

    @Transactional
    public InterfazResponse agregarInterfaz(Integer idEquipo, InterfazRequest req) {
        Equipo e = equipos.findById(idEquipo)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Equipo", idEquipo));
        InterfazRed i = new InterfazRed();
        i.setEquipo(e);
        i.setDireccionIp(req.direccionIp());
        i.setMac(req.mac());
        i.setMascara(req.mascara());
        i.setGateway(req.gateway());
        i.setDns(req.dns());
        i.setPuerto(req.puerto());
        return EquipoMapper.aInterfaz(interfaces.save(i));
    }

    @Transactional(readOnly = true)
    public List<VistaGarantia> garantiasPorVencer() {
        return garantias.findAll();
    }

    private void aplicar(Equipo e, EquipoRequest req) {
        e.setModelo(modelos.findById(req.idModelo())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Modelo", req.idModelo())));
        e.setUbicacion(ubicaciones.findById(req.idUbicacion())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Ubicacion", req.idUbicacion())));
        e.setPlataforma(req.idPlataforma() == null ? null : plataformas.findById(req.idPlataforma())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Plataforma", req.idPlataforma())));
        e.setContratacion(req.idContratacion() == null ? null : contrataciones.findById(req.idContratacion())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Contratacion", req.idContratacion())));
        e.setNombreEquipo(req.nombreEquipo());
        e.setSerie(req.serie());
        e.setNumActivo(req.numActivo());
        if (req.estado() != null && !req.estado().isBlank()) {
            e.setEstado(EstadoEquipo.valueOf(req.estado()));
        }
        e.setVerWindows(req.verWindows());
        e.setLicenciaWin(req.licenciaWin());
        e.setFechaInstalacion(req.fechaInstalacion());
        e.setVencGarantia(req.vencGarantia());
    }
}
