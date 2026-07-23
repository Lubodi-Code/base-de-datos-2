package cr.poderjudicial.sigec.catalogo.service;

import cr.poderjudicial.sigec.catalogo.dto.OpcionDto;
import cr.poderjudicial.sigec.catalogo.entity.UbicacionFisica;
import cr.poderjudicial.sigec.catalogo.repository.CantonRepository;
import cr.poderjudicial.sigec.catalogo.repository.ContratacionRepository;
import cr.poderjudicial.sigec.catalogo.repository.EdificioRepository;
import cr.poderjudicial.sigec.catalogo.repository.ModeloEquipoRepository;
import cr.poderjudicial.sigec.catalogo.repository.PlataformaVmsRepository;
import cr.poderjudicial.sigec.catalogo.repository.ProvinciaRepository;
import cr.poderjudicial.sigec.catalogo.repository.TecnicoRepository;
import cr.poderjudicial.sigec.catalogo.repository.UbicacionFisicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/** Consultas de catalogos para poblar selectores del aplicativo. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final ProvinciaRepository provincias;
    private final CantonRepository cantones;
    private final EdificioRepository edificios;
    private final UbicacionFisicaRepository ubicaciones;
    private final ModeloEquipoRepository modelos;
    private final PlataformaVmsRepository plataformas;
    private final ContratacionRepository contrataciones;
    private final TecnicoRepository tecnicos;

    public CatalogoService(ProvinciaRepository provincias, CantonRepository cantones,
                           EdificioRepository edificios, UbicacionFisicaRepository ubicaciones,
                           ModeloEquipoRepository modelos, PlataformaVmsRepository plataformas,
                           ContratacionRepository contrataciones, TecnicoRepository tecnicos) {
        this.provincias = provincias;
        this.cantones = cantones;
        this.edificios = edificios;
        this.ubicaciones = ubicaciones;
        this.modelos = modelos;
        this.plataformas = plataformas;
        this.contrataciones = contrataciones;
        this.tecnicos = tecnicos;
    }

    public List<OpcionDto> provincias() {
        return provincias.findAll().stream()
                .map(p -> new OpcionDto(p.getId().intValue(), p.getNombre()))
                .toList();
    }

    public List<OpcionDto> cantones(Short idProvincia) {
        return cantones.findByProvinciaIdOrderByNombre(idProvincia).stream()
                .map(c -> new OpcionDto(c.getId(), c.getNombre()))
                .toList();
    }

    public List<OpcionDto> edificios(Integer idCanton) {
        return edificios.findByCantonIdOrderByNombre(idCanton).stream()
                .map(e -> new OpcionDto(e.getId(), e.getCuentaPj() + " - " + e.getNombre()))
                .toList();
    }

    public List<OpcionDto> ubicaciones(Integer idEdificio) {
        return ubicaciones.findByEdificioId(idEdificio).stream()
                .map(u -> new OpcionDto(u.getId(), etiquetaUbicacion(u)))
                .toList();
    }

    public List<OpcionDto> modelos() {
        return modelos.findAll().stream()
                .map(m -> new OpcionDto(m.getId(), m.getMarca() + " " + m.getModelo() + " (" + m.getTipo() + ")"))
                .toList();
    }

    public List<OpcionDto> plataformas() {
        return plataformas.findAll().stream()
                .map(p -> new OpcionDto(p.getId().intValue(), p.getNombre()))
                .toList();
    }

    public List<OpcionDto> contrataciones() {
        return contrataciones.findAll().stream()
                .map(c -> new OpcionDto(c.getId(),
                        c.getNumSicop() != null ? c.getNumSicop()
                                : (c.getOrdenPedido() != null ? c.getOrdenPedido() : "Contratacion " + c.getId())))
                .toList();
    }

    public List<OpcionDto> tecnicos() {
        return tecnicos.findAll().stream()
                .map(t -> new OpcionDto(t.getId(), t.getNombre()))
                .toList();
    }

    private String etiquetaUbicacion(UbicacionFisica u) {
        String partes = Stream.of(
                        u.getPiso() != null ? "Piso " + u.getPiso() : null,
                        u.getRack() != null ? "Rack " + u.getRack() : null,
                        u.getPuertoSwitch() != null ? "Puerto " + u.getPuertoSwitch() : null)
                .filter(s -> s != null)
                .reduce((a, b) -> a + " / " + b)
                .orElse("Ubicacion " + u.getId());
        return partes;
    }
}
