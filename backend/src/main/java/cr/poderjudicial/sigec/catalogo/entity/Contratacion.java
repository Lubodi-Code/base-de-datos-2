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
@Table(name = "contratacion")
public class Contratacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contratacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "num_sicop", length = 40)
    private String numSicop;

    @Column(name = "num_proveeduria", length = 40)
    private String numProveeduria;

    @Column(name = "orden_pedido", length = 40)
    private String ordenPedido;

    @Column(name = "num_acta", length = 40)
    private String numActa;

    /** Plazo de garantia en meses. */
    @Column(name = "plazo_garantia")
    private Integer plazoGarantia;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getNumSicop() { return numSicop; }
    public void setNumSicop(String numSicop) { this.numSicop = numSicop; }

    public String getNumProveeduria() { return numProveeduria; }
    public void setNumProveeduria(String numProveeduria) { this.numProveeduria = numProveeduria; }

    public String getOrdenPedido() { return ordenPedido; }
    public void setOrdenPedido(String ordenPedido) { this.ordenPedido = ordenPedido; }

    public String getNumActa() { return numActa; }
    public void setNumActa(String numActa) { this.numActa = numActa; }

    public Integer getPlazoGarantia() { return plazoGarantia; }
    public void setPlazoGarantia(Integer plazoGarantia) { this.plazoGarantia = plazoGarantia; }
}
