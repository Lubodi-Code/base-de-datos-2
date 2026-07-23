package cr.poderjudicial.sigec.seguridad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario_sistema")
public class UsuarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 40)
    private String username;

    @Column(name = "hash_password", nullable = false, length = 72)
    private String hashPassword;

    @Column(name = "nombre_completo", length = 120)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 10)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "id_tecnico")
    private Integer idTecnico;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getHashPassword() { return hashPassword; }
    public void setHashPassword(String hashPassword) { this.hashPassword = hashPassword; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Integer getIdTecnico() { return idTecnico; }
    public void setIdTecnico(Integer idTecnico) { this.idTecnico = idTecnico; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
