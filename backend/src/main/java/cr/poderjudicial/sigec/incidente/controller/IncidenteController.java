package cr.poderjudicial.sigec.incidente.controller;

import cr.poderjudicial.sigec.incidente.dto.IncidenteCierreRequest;
import cr.poderjudicial.sigec.incidente.dto.IncidenteRequest;
import cr.poderjudicial.sigec.incidente.dto.IncidenteResponse;
import cr.poderjudicial.sigec.incidente.dto.IncidenteResumenResponse;
import cr.poderjudicial.sigec.incidente.service.IncidenteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidentes")
public class IncidenteController {

    private final IncidenteService incidenteService;

    public IncidenteController(IncidenteService incidenteService) {
        this.incidenteService = incidenteService;
    }

    /** Listado/busqueda paginada con filtros opcionales (estado, equipo). */
    @GetMapping
    public Page<IncidenteResumenResponse> buscar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idEquipo,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return incidenteService.buscar(estado, idEquipo, pageable);
    }

    @GetMapping("/{id}")
    public IncidenteResponse obtener(@PathVariable Integer id) {
        return incidenteService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<IncidenteResponse> crear(@Valid @RequestBody IncidenteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public IncidenteResponse actualizar(@PathVariable Integer id, @Valid @RequestBody IncidenteRequest req) {
        return incidenteService.actualizar(id, req);
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public IncidenteResponse cerrar(@PathVariable Integer id, @Valid @RequestBody IncidenteCierreRequest req) {
        return incidenteService.cerrar(id, req);
    }
}
