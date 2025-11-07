package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "historiales")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorial;

    // 1:1 con Usuario
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true) // FK y asegura que sea único
    private Usuario usuario;

    private String detalle; // Ej: "Historial de transacciones de..."

    // Constructores, Getters y Setters

    public Historial() {
    }

    public Historial(Usuario usuario, String detalle) {
        this.usuario = usuario;
        this.detalle = detalle;
    }

    // --- Getters y Setters ---

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Historial historial = (Historial) o;
        return Objects.equals(idHistorial, historial.idHistorial) &&
                Objects.equals(usuario, historial.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHistorial, usuario);
    }
}
