package com.example.user_demo.data.model;

import jakarta.persistence.*;

@Entity
@Table(name = "activos")
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActivo;

    // Relación con la Cartera (N:1)
    @ManyToOne
    @JoinColumn(name = "id_cartera", nullable = false)
    private Cartera cartera;

    // Relación con la Criptomoneda (N:1)
    @ManyToOne
    @JoinColumn(name = "id_cripto", nullable = false)
    private Criptomoneda criptomoneda;

    // EL DATO NUEVO: La cantidad exacta
    private Double cantidad;

    public Activo() {
    }

    public Activo(Cartera cartera, Criptomoneda criptomoneda, Double cantidad) {
        this.cartera = cartera;
        this.criptomoneda = criptomoneda;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public Long getIdActivo() { return idActivo; }
    public void setIdActivo(Long idActivo) { this.idActivo = idActivo; }
    public Cartera getCartera() { return cartera; }
    public void setCartera(Cartera cartera) { this.cartera = cartera; }
    public Criptomoneda getCriptomoneda() { return criptomoneda; }
    public void setCriptomoneda(Criptomoneda criptomoneda) { this.criptomoneda = criptomoneda; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
}
