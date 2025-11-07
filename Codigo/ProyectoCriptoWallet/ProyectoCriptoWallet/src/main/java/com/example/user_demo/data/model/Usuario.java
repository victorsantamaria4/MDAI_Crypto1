package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;
    private String email; // Añadido para un ejemplo más real

    // 1:N con Cartera
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cartera> carteras = new ArrayList<>();

    // 1:1 con Historial
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Historial historial;

    // 1:N con Transaccion (como origen)
    @OneToMany(mappedBy = "usuarioOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaccion> transaccionesEnviadas = new ArrayList<>();

    // 1:N con Transaccion (como destino)
    @OneToMany(mappedBy = "usuarioDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaccion> transaccionesRecibidas = new ArrayList<>();

    // Constructores, Getters y Setters

    public Usuario() {
    }

    public Usuario(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    // --- Getters y Setters ---

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Cartera> getCarteras() {
        return carteras;
    }

    public void setCarteras(List<Cartera> carteras) {
        this.carteras = carteras;
    }

    public Historial getHistorial() {
        return historial;
    }

    public void setHistorial(Historial historial) {
        this.historial = historial;
    }

    public List<Transaccion> getTransaccionesEnviadas() {
        return transaccionesEnviadas;
    }


    public void addCartera(Cartera cartera) {
        this.carteras.add(cartera);
        cartera.setUsuario(this);
    }


    public void removeCartera(Cartera cartera) {
        this.carteras.remove(cartera);
        cartera.setUsuario(null);
    }

    public void setTransaccionesEnviadas(List<Transaccion> transaccionesEnviadas) {
        this.transaccionesEnviadas = transaccionesEnviadas;
    }

    public List<Transaccion> getTransaccionesRecibidas() {
        return transaccionesRecibidas;
    }

    public void setTransaccionesRecibidas(List<Transaccion> transaccionesRecibidas) {
        this.transaccionesRecibidas = transaccionesRecibidas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(idUsuario, usuario.idUsuario) &&
                Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, email);
    }
}