package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "criptomonedas")
public class Criptomoneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCripto;

    private String nombre; // Ej: Bitcoin
    private String simbolo; // Ej: BTC

    // NUEVO CAMPO: Precio para calcular conversiones (Ej: 50000.0)
    private Double precioActual;

    // 1:N con Transaccion (Mantenemos esto para el historial)
    @OneToMany(mappedBy = "criptomoneda", fetch = FetchType.LAZY)
    private List<Transaccion> transacciones = new ArrayList<>();

    // NOTA: La relación directa con Cartera (@ManyToMany) se ELIMINÓ
    // porque ahora usamos la entidad intermedia 'Activo'.

    // --- Constructores ---

    public Criptomoneda() {
    }

    public Criptomoneda(String nombre, String simbolo, Double precioActual) {
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.precioActual = precioActual;
    }

    // --- Getters y Setters ---

    public Long getIdCripto() {
        return idCripto;
    }

    public void setIdCripto(Long idCripto) {
        this.idCripto = idCripto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    // Getter y Setter del nuevo campo
    public Double getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(Double precioActual) {
        this.precioActual = precioActual;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    // --- Equals y HashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criptomoneda that = (Criptomoneda) o;
        return Objects.equals(idCripto, that.idCripto) &&
                Objects.equals(simbolo, that.simbolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCripto, simbolo);
    }
}