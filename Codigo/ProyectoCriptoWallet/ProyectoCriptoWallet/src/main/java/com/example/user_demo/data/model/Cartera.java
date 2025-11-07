package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "carteras")
public class Cartera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartera;

    // N:1 con Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false) // Columna FK
    private Usuario usuario;

    // N:M con Criptomoneda
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cartera_cripto", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "id_cartera"),
            inverseJoinColumns = @JoinColumn(name = "id_cripto")
    )
    private Set<Criptomoneda> criptomonedas = new HashSet<>();

    // Añadido para que tenga sentido
    private double balanceTotal;

    // Constructores, Getters y Setters

    public Cartera() {
    }

    public Cartera(Usuario usuario, double balanceTotal) {
        this.usuario = usuario;
        this.balanceTotal = balanceTotal;
    }

    // --- Getters y Setters ---

    public Long getIdCartera() {
        return idCartera;
    }

    public void setIdCartera(Long idCartera) {
        this.idCartera = idCartera;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<Criptomoneda> getCriptomonedas() {
        return criptomonedas;
    }

    public void setCriptomonedas(Set<Criptomoneda> criptomonedas) {
        this.criptomonedas = criptomonedas;
    }

    public double getBalanceTotal() {
        return balanceTotal;
    }

    public void setBalanceTotal(double balanceTotal) {
        this.balanceTotal = balanceTotal;
    }

    public void addCriptomoneda(Criptomoneda cripto) {
        this.criptomonedas.add(cripto);
        cripto.getCarteras().add(this); // Mantiene la bidireccionalidad
    }

    public void removeCriptomoneda(Criptomoneda cripto) {
        this.criptomonedas.remove(cripto);
        cripto.getCarteras().remove(this); // Mantiene la bidireccionalidad
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cartera cartera = (Cartera) o;
        return Objects.equals(idCartera, cartera.idCartera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCartera);
    }
}