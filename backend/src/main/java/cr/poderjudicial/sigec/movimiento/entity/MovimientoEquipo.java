package cr.poderjudicial.sigec.movimiento.entity;

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

import java.time.OffsetDateTime;

@Entity
@Table(name = "movimiento_equipo")
public class MovimientoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    /** [#2] Usuario que ejecuto el movimiento (NOT NULL en el DDL). */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 12)
    private TipoMovimiento tipo;

    @Column(name = "fecha", nullable = false, updatable = false)
    private OffsetDateTime fecha;

    @Column(name = "motivo")
    private String motivo;

    /** Equipo sustituto (solo en REEMPLAZO). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo_sustituto")
    private Equipo equipoSustituto;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = OffsetDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Equipo getEquipoSustituto() { return equipoSustituto; }
    public void setEquipoSustituto(Equipo equipoSustituto) { this.equipoSustituto = equipoSustituto; }
}
