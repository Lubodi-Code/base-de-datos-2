package cr.poderjudicial.sigec.inventario.controller;

import cr.poderjudicial.sigec.inventario.dto.CredencialRequest;
import cr.poderjudicial.sigec.inventario.dto.CredencialResponse;
import cr.poderjudicial.sigec.inventario.dto.CredencialSecretoResponse;
import cr.poderjudicial.sigec.inventario.dto.EquipoRequest;
import cr.poderjudicial.sigec.inventario.dto.EquipoResponse;
import cr.poderjudicial.sigec.inventario.dto.EquipoResumenResponse;
import cr.poderjudicial.sigec.inventario.dto.InterfazRequest;
import cr.poderjudicial.sigec.inventario.dto.InterfazResponse;
import cr.poderjudicial.sigec.inventario.entity.VistaGarantia;
import cr.poderjudicial.sigec.inventario.service.CredencialService;
import cr.poderjudicial.sigec.inventario.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipos")
public class EquipoController {

    private final EquipoService equipoService;
    private final CredencialService credencialService;

    public EquipoController(EquipoService equipoService, CredencialService credencialService) {
        this.equipoService = equipoService;
        this.credencialService = credencialService;
    }

    /** Listado/busqueda paginada con filtros opcionales (estado, provincia, texto, IP). */
    @GetMapping
    public Page<EquipoResumenResponse> buscar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Short idProvincia,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String ip,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return equipoService.buscar(estado, idProvincia, texto, ip, pageable);
    }

    @GetMapping("/garantias-por-vencer")
    public List<VistaGarantia> garantiasPorVencer() {
        return equipoService.garantiasPorVencer();
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable Integer id) {
        return equipoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<EquipoResponse> crear(@Valid @RequestBody EquipoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public EquipoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EquipoRequest req) {
        return equipoService.actualizar(id, req);
    }

    @PostMapping("/{id}/interfaces")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<InterfazResponse> agregarInterfaz(@PathVariable Integer id,
                                                            @Valid @RequestBody InterfazRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.agregarInterfaz(id, req));
    }

    @GetMapping("/{id}/credenciales")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public List<CredencialResponse> credenciales(@PathVariable Integer id) {
        return credencialService.listar(id);
    }

    @PostMapping("/{id}/credenciales")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<CredencialResponse> guardarCredencial(@PathVariable Integer id,
                                                                @Valid @RequestBody CredencialRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credencialService.guardar(id, req));
    }

    @GetMapping("/credenciales/{idCredencial}/revelar")
    @PreAuthorize("hasRole('ADMIN')")
    public CredencialSecretoResponse revelar(@PathVariable Integer idCredencial) {
        return credencialService.revelar(idCredencial);
    }
}
