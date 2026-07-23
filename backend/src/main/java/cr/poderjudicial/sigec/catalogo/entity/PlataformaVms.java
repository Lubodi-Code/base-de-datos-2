package cr.poderjudicial.sigec.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plataforma_vms")
public class PlataformaVms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plataforma")
    private Short id;

    @Column(name = "nombre", nullable = false, length = 40)
    private String nombre;

    @Column(name = "version", length = 30)
    private String version;

    @Column(name = "puerto_defecto")
    private Integer puertoDefecto;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Integer getPuertoDefecto() { return puertoDefecto; }
    public void setPuertoDefecto(Integer puertoDefecto) { this.puertoDefecto = puertoDefecto; }
}
