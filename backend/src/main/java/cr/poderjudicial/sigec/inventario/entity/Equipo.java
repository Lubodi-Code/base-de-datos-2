package cr.poderjudicial.sigec.inventario.entity;

import cr.poderjudicial.sigec.catalogo.entity.Contratacion;
import cr.poderjudicial.sigec.catalogo.entity.ModeloEquipo;
import cr.poderjudicial.sigec.catalogo.entity.PlataformaVms;
import cr.poderjudicial.sigec.catalogo.entity.UbicacionFisica;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modelo", nullable = false)
    private ModeloEquipo modelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private UbicacionFisica ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plataforma")
    private PlataformaVms plataforma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contratacion")
    private Contratacion contratacion;

    @Column(name = "nombre_equipo", nullable = false, length = 80)
    private String nombreEquipo;

    @Column(name = "serie", length = 60)
    private String serie;

    @Column(name = "num_activo", unique = true, length = 20)
    private String numActivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoEquipo estado = EstadoEquipo.ACTIVO;

    @Column(name = "ver_windows", length = 40)
    private String verWindows;

    @Column(name = "licencia_win", length = 60)
    private String licenciaWin;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(name = "venc_garantia")
    private LocalDate vencGarantia;

    /** Trazabilidad de autoria: usuario que dio de alta el equipo (correccion #2). */
    @Column(name = "id_usuario_registro")
    private Integer idUsuarioRegistro;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    /** Mantenido por el trigger trg_touch_equipo; de solo lectura para Hibernate. */
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private OffsetDateTime actualizadoEn;

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<InterfazRed> interfaces = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = EstadoEquipo.ACTIVO;
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ModeloEquipo getModelo() { return modelo; }
    public void setModelo(ModeloEquipo modelo) { this.modelo = modelo; }

    public UbicacionFisica getUbicacion() { return ubicacion; }
    public void setUbicacion(UbicacionFisica ubicacion) { this.ubicacion = ubicacion; }

    public PlataformaVms getPlataforma() { return plataforma; }
    public void setPlataforma(PlataformaVms plataforma) { this.plataforma = plataforma; }

    public Contratacion getContratacion() { return contratacion; }
    public void setContratacion(Contratacion contratacion) { this.contratacion = contratacion; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getNumActivo() { return numActivo; }
    public void setNumActivo(String numActivo) { this.numActivo = numActivo; }

    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }

    public String getVerWindows() { return verWindows; }
    public void setVerWindows(String verWindows) { this.verWindows = verWindows; }

    public String getLicenciaWin() { return licenciaWin; }
    public void setLicenciaWin(String licenciaWin) { this.licenciaWin = licenciaWin; }

    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(LocalDate fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }

    public LocalDate getVencGarantia() { return vencGarantia; }
    public void setVencGarantia(LocalDate vencGarantia) { this.vencGarantia = vencGarantia; }

    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }

    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }

    public List<InterfazRed> getInterfaces() { return interfaces; }
}
