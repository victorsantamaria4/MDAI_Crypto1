package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaccion;

    // N:1 con Usuario (Origen)
    @ManyToOne
    @JoinColumn(name = "id_usuario_origen", nullable = false)
    private Usuario usuarioOrigen;

    // N:1 con Usuario (Destino)
    @ManyToOne
    @JoinColumn(name = "id_usuario_destino", nullable = false)
    private Usuario usuarioDestino;

    // N:1 con Criptomoneda
    @ManyToOne
    @JoinColumn(name = "id_cripto", nullable = false)
    private Criptomoneda criptomoneda;

    private double cantidad;
    private LocalDateTime fecha;

    // Constructores, Getters y Setters

    public Transaccion() {
    }

    public Transaccion(Usuario usuarioOrigen, Usuario usuarioDestino, Criptomoneda criptomoneda, double cantidad) {
        this.usuarioOrigen = usuarioOrigen;
        this.usuarioDestino = usuarioDestino;
        this.criptomoneda = criptomoneda;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
    }

    // --- Getters y Setters ---

    public Long getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(Long idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Usuario getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public void setUsuarioOrigen(Usuario usuarioOrigen) {
        this.usuarioOrigen = usuarioOrigen;
    }

    public Usuario getUsuarioDestino() {
        return usuarioDestino;
    }

    public void setUsuarioDestino(Usuario usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }

    public Criptomoneda getCriptomoneda() {
        return criptomoneda;
    }

    public void setCriptomoneda(Criptomoneda criptomoneda) {
        this.criptomoneda = criptomoneda;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaccion that = (Transaccion) o;
        return Objects.equals(idTransaccion, that.idTransaccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTransaccion);
    }
}