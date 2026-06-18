package cr.poderjudicial.sigec.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Credencial de equipo con el secreto cifrado (AES-256-GCM) en columna BYTEA. */
@Entity
@Table(name = "credencial_equipo")
public class CredencialEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    /** SO, VMS o TOOLBOX (validado por CHECK en la BD). */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "usuario", length = 60)
    private String usuario;

    @Column(name = "secreto_cifrado", nullable = false)
    private byte[] secretoCifrado;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public byte[] getSecretoCifrado() { return secretoCifrado; }
    public void setSecretoCifrado(byte[] secretoCifrado) { this.secretoCifrado = secretoCifrado; }
}
