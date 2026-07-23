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
import org.hibernate.annotations.ColumnTransformer;

/**
 * Interfaz de red de un equipo. direccion_ip/mascara/gateway/dns son INET y mac es
 * MACADDR en PostgreSQL: se mapean como String y se castean en escritura con
 * {@code ?::inet} / {@code ?::macaddr} mediante {@link ColumnTransformer}.
 */
@Entity
@Table(name = "interfaz_red")
public class InterfazRed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interfaz")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @Column(name = "direccion_ip", nullable = false, unique = true)
    @ColumnTransformer(write = "?::inet")
    private String direccionIp;

    @Column(name = "mac", unique = true)
    @ColumnTransformer(write = "?::macaddr")
    private String mac;

    @Column(name = "mascara")
    @ColumnTransformer(write = "?::inet")
    private String mascara;

    @Column(name = "gateway")
    @ColumnTransformer(write = "?::inet")
    private String gateway;

    @Column(name = "dns")
    @ColumnTransformer(write = "?::inet")
    private String dns;

    @Column(name = "puerto")
    private Integer puerto;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }

    public String getMascara() { return mascara; }
    public void setMascara(String mascara) { this.mascara = mascara; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getDns() { return dns; }
    public void setDns(String dns) { this.dns = dns; }

    public Integer getPuerto() { return puerto; }
    public void setPuerto(Integer puerto) { this.puerto = puerto; }
}
