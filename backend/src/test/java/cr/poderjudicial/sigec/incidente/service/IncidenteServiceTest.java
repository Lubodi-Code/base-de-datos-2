package cr.poderjudicial.sigec.incidente.service;

import cr.poderjudicial.sigec.catalogo.repository.TecnicoRepository;
import cr.poderjudicial.sigec.common.ReglaNegocioException;
import cr.poderjudicial.sigec.incidente.dto.IncidenteCierreRequest;
import cr.poderjudicial.sigec.incidente.entity.EstadoIncidente;
import cr.poderjudicial.sigec.incidente.entity.Incidente;
import cr.poderjudicial.sigec.incidente.repository.IncidenteRepository;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenteServiceTest {

    @Mock private IncidenteRepository incidentes;
    @Mock private EquipoRepository equipos;
    @Mock private TecnicoRepository tecnicos;

    @InjectMocks private IncidenteService service;

    private Incidente incidente;

    @BeforeEach
    void setUp() {
        Equipo equipo = new Equipo();
        equipo.setId(1);
        equipo.setNombreEquipo("Grabador OIJ");

        incidente = new Incidente();
        incidente.setId(10);
        incidente.setEquipo(equipo);
        incidente.setFechaObservacion(LocalDate.of(2024, 10, 2));
        incidente.setEstado(EstadoIncidente.PROCESO);

        lenient().when(incidentes.save(any(Incidente.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cierreValidoMarcaCerradoYGuardaTrabajo() {
        when(incidentes.findById(10)).thenReturn(Optional.of(incidente));

        var resp = service.cerrar(10,
                new IncidenteCierreRequest(LocalDate.of(2024, 10, 5), "Fuente reemplazada"));

        assertEquals("CERRADO", resp.estado());
        assertEquals(EstadoIncidente.CERRADO, incidente.getEstado());
        assertEquals("Fuente reemplazada", incidente.getTrabajoRealizado());
        verify(incidentes).save(incidente);
    }

    @Test
    void cerrarUnoYaCerradoLanza409() {
        incidente.setEstado(EstadoIncidente.CERRADO);
        when(incidentes.findById(10)).thenReturn(Optional.of(incidente));

        assertThrows(ReglaNegocioException.class,
                () -> service.cerrar(10, new IncidenteCierreRequest(LocalDate.of(2024, 10, 5), null)));
        verify(incidentes, never()).save(any());
    }

    @Test
    void fechaReparacionAnteriorALaObservacionLanza409() {
        when(incidentes.findById(10)).thenReturn(Optional.of(incidente));

        assertThrows(ReglaNegocioException.class,
                () -> service.cerrar(10, new IncidenteCierreRequest(LocalDate.of(2024, 9, 30), null)));
        verify(incidentes, never()).save(any());
    }
}
