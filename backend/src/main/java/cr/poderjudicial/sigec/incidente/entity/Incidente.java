package cr.poderjudicial.sigec.incidente.entity;

import cr.poderjudicial.sigec.catalogo.entity.Tecnico;
import cr.poderjudicial.sigec.inventario.entity.Equipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "incidente")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidente")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    @Column(name = "fecha_observacion", nullable = false)
    private LocalDate fechaObservacion;

    @Column(name = "fecha_reparacion")
    private LocalDate fechaReparacion;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "trabajo_realizado")
    private String trabajoRealizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoIncidente estado = EstadoIncidente.ABIERTO;

    @Column(name = "medidas_a_tomar")
    private String medidasATomar;

    /** Trazabilidad de autoria: usuario que registro/gestiono el incidente (correccion #2). */
    @Column(name = "id_usuario_registro")
    private Integer idUsuarioRegistro;

    @PrePersist
    void prePersist() {
        if (estado == null) {
            estado = EstadoIncidente.ABIERTO;
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }

    public LocalDate getFechaObservacion() { return fechaObservacion; }
    public void setFechaObservacion(LocalDate fechaObservacion) { this.fechaObservacion = fechaObservacion; }

    public LocalDate getFechaReparacion() { return fechaReparacion; }
    public void setFechaReparacion(LocalDate fechaReparacion) { this.fechaReparacion = fechaReparacion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getTrabajoRealizado() { return trabajoRealizado; }
    public void setTrabajoRealizado(String trabajoRealizado) { this.trabajoRealizado = trabajoRealizado; }

    public EstadoIncidente getEstado() { return estado; }
    public void setEstado(EstadoIncidente estado) { this.estado = estado; }

    public String getMedidasATomar() { return medidasATomar; }
    public void setMedidasATomar(String medidasATomar) { this.medidasATomar = medidasATomar; }

    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
}
