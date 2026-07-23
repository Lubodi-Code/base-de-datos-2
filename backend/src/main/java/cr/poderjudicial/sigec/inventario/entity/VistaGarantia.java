package cr.poderjudicial.sigec.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

/** Proyeccion de solo lectura sobre la vista v_garantias_por_vencer. */
@Entity
@Immutable
@Table(name = "v_garantias_por_vencer")
public class VistaGarantia {

    @Id
    @Column(name = "id_equipo")
    private Integer idEquipo;

    @Column(name = "nombre_equipo")
    private String nombreEquipo;

    @Column(name = "num_activo")
    private String numActivo;

    @Column(name = "venc_garantia")
    private LocalDate vencGarantia;

    @Column(name = "dias_restantes")
    private Integer diasRestantes;

    @Column(name = "cuenta_pj")
    private String cuentaPj;

    @Column(name = "edificio")
    private String edificio;

    @Column(name = "provincia")
    private String provincia;

    public Integer getIdEquipo() { return idEquipo; }
    public String getNombreEquipo() { return nombreEquipo; }
    public String getNumActivo() { return numActivo; }
    public LocalDate getVencGarantia() { return vencGarantia; }
    public Integer getDiasRestantes() { return diasRestantes; }
    public String getCuentaPj() { return cuentaPj; }
    public String getEdificio() { return edificio; }
    public String getProvincia() { return provincia; }
}
