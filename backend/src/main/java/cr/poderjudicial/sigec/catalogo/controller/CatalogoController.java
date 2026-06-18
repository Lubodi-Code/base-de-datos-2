package cr.poderjudicial.sigec.catalogo.controller;

import cr.poderjudicial.sigec.catalogo.dto.OpcionDto;
import cr.poderjudicial.sigec.catalogo.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints de solo lectura para poblar los selectores del frontend. */
@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    private final CatalogoService catalogos;

    public CatalogoController(CatalogoService catalogos) {
        this.catalogos = catalogos;
    }

    @GetMapping("/provincias")
    public List<OpcionDto> provincias() {
        return catalogos.provincias();
    }

    @GetMapping("/provincias/{idProvincia}/cantones")
    public List<OpcionDto> cantones(@PathVariable Short idProvincia) {
        return catalogos.cantones(idProvincia);
    }

    @GetMapping("/cantones/{idCanton}/edificios")
    public List<OpcionDto> edificios(@PathVariable Integer idCanton) {
        return catalogos.edificios(idCanton);
    }

    @GetMapping("/edificios/{idEdificio}/ubicaciones")
    public List<OpcionDto> ubicaciones(@PathVariable Integer idEdificio) {
        return catalogos.ubicaciones(idEdificio);
    }

    @GetMapping("/modelos")
    public List<OpcionDto> modelos() {
        return catalogos.modelos();
    }

    @GetMapping("/plataformas")
    public List<OpcionDto> plataformas() {
        return catalogos.plataformas();
    }

    @GetMapping("/contrataciones")
    public List<OpcionDto> contrataciones() {
        return catalogos.contrataciones();
    }

    @GetMapping("/tecnicos")
    public List<OpcionDto> tecnicos() {
        return catalogos.tecnicos();
    }
}
