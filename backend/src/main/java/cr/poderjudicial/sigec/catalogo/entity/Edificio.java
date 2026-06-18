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
@Table(name = "edificio")
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edificio")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_canton", nullable = false)
    private Canton canton;

    @Column(name = "cuenta_pj", nullable = false, unique = true, length = 20)
    private String cuentaPj;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "direccion", length = 200)
    private String direccion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Canton getCanton() { return canton; }
    public void setCanton(Canton canton) { this.canton = canton; }

    public String getCuentaPj() { return cuentaPj; }
    public void setCuentaPj(String cuentaPj) { this.cuentaPj = cuentaPj; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
