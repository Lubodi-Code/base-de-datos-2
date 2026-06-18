package cr.poderjudicial.sigec.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ubicacion_fisica")
public class UbicacionFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_edificio", nullable = false)
    private Edificio edificio;

    @Column(name = "piso", length = 30)
    private String piso;

    @Column(name = "modulo_torre", length = 60)
    private String moduloTorre;

    @Column(name = "rack", length = 40)
    private String rack;

    @Column(name = "patchpanel", length = 40)
    private String patchpanel;

    @Column(name = "puerto_switch", length = 40)
    private String puertoSwitch;

    @Column(name = "activo_sw", length = 60)
    private String activoSw;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Edificio getEdificio() { return edificio; }
    public void setEdificio(Edificio edificio) { this.edificio = edificio; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getModuloTorre() { return moduloTorre; }
    public void setModuloTorre(String moduloTorre) { this.moduloTorre = moduloTorre; }

    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }

    public String getPatchpanel() { return patchpanel; }
    public void setPatchpanel(String patchpanel) { this.patchpanel = patchpanel; }

    public String getPuertoSwitch() { return puertoSwitch; }
    public void setPuertoSwitch(String puertoSwitch) { this.puertoSwitch = puertoSwitch; }

    public String getActivoSw() { return activoSw; }
    public void setActivoSw(String activoSw) { this.activoSw = activoSw; }
}
