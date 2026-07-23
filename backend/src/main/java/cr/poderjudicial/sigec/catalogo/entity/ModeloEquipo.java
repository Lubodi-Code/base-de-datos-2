package cr.poderjudicial.sigec.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "modelo_equipo")
public class ModeloEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modelo")
    private Integer id;

    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 80)
    private String modelo;

    /** CAMARA, GRABADOR, SERVIDOR o SWITCH (validado por CHECK en la BD). */
    @Column(name = "tipo", nullable = false, length = 12)
    private String tipo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
