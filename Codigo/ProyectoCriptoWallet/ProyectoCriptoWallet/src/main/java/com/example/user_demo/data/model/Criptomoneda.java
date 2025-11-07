package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "criptomonedas")
public class Criptomoneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCripto;

    private String nombre; // Ej: Bitcoin
    private String simbolo; // Ej: BTC

    // 1:N con Transaccion
    @OneToMany(mappedBy = "criptomoneda", fetch = FetchType.LAZY)
    private List<Transaccion> transacciones = new ArrayList<>();

    // N:M con Cartera
    @ManyToMany(mappedBy = "criptomonedas", fetch = FetchType.LAZY)
    private Set<Cartera> carteras = new HashSet<>();

    // Constructores, Getters y Setters

    public Criptomoneda() {
    }

    public Criptomoneda(String nombre, String simbolo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
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

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    public Set<Cartera> getCarteras() {
        return carteras;
    }

    public void setCarteras(Set<Cartera> carteras) {
        this.carteras = carteras;
    }

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
