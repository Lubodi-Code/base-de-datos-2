package cr.poderjudicial.sigec.movimiento.service;

import cr.poderjudicial.sigec.catalogo.repository.TecnicoRepository;
import cr.poderjudicial.sigec.common.ReglaNegocioException;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import cr.poderjudicial.sigec.inventario.entity.EstadoEquipo;
import cr.poderjudicial.sigec.inventario.repository.EquipoRepository;
import cr.poderjudicial.sigec.movimiento.dto.MovimientoRequest;
import cr.poderjudicial.sigec.movimiento.entity.MovimientoEquipo;
import cr.poderjudicial.sigec.movimiento.repository.MovimientoRepository;
import cr.poderjudicial.sigec.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock private MovimientoRepository movimientos;
    @Mock private EquipoRepository equipos;
    @Mock private TecnicoRepository tecnicos;

    @InjectMocks private MovimientoService service;

    private Equipo equipo;

    @BeforeEach
    void setUp() {
        equipo = new Equipo();
        equipo.setId(1);
        equipo.setNombreEquipo("Grabador OIJ");
        equipo.setEstado(EstadoEquipo.ACTIVO);
        autenticarComo(new UsuarioAutenticado(7, "tecnico1", "TECNICO"));
        // save devuelve la misma entidad recibida.
        lenient().when(movimientos.save(any(MovimientoEquipo.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bajaDejaElEquipoRetirado() {
        when(equipos.findById(1)).thenReturn(Optional.of(equipo));

        var resp = service.registrar(new MovimientoRequest(1, "BAJA", null, "fin de vida", null));

        assertEquals(EstadoEquipo.RETIRADO, equipo.getEstado());
        assertEquals(7, resp.idUsuario());
        verify(movimientos).save(any(MovimientoEquipo.class));
    }

    @Test
    void reemplazoConSustitutoDejaElEquipoReemplazado() {
        Equipo sustituto = new Equipo();
        sustituto.setId(2);
        sustituto.setNombreEquipo("Grabador nuevo");
        when(equipos.findById(1)).thenReturn(Optional.of(equipo));
        when(equipos.findById(2)).thenReturn(Optional.of(sustituto));

        service.registrar(new MovimientoRequest(1, "REEMPLAZO", null, "quemado", 2));

        assertEquals(EstadoEquipo.REEMPLAZADO, equipo.getEstado());
    }

    @Test
    void reemplazoSinSustitutoLanza409() {
        when(equipos.findById(1)).thenReturn(Optional.of(equipo));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(new MovimientoRequest(1, "REEMPLAZO", null, "x", null)));
        verify(movimientos, never()).save(any());
    }

    @Test
    void sustitutoIgualAlEquipoLanza409() {
        when(equipos.findById(1)).thenReturn(Optional.of(equipo));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(new MovimientoRequest(1, "REEMPLAZO", null, "x", 1)));
    }

    @Test
    void sustitutoEnBajaLanza409() {
        when(equipos.findById(1)).thenReturn(Optional.of(equipo));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(new MovimientoRequest(1, "BAJA", null, "x", 2)));
    }

    @Test
    void sinUsuarioAutenticadoLanza409() {
        SecurityContextHolder.clearContext();

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(new MovimientoRequest(1, "BAJA", null, "x", null)));
        verify(movimientos, never()).save(any());
    }

    private static void autenticarComo(UsuarioAutenticado u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, java.util.List.of()));
    }
}
