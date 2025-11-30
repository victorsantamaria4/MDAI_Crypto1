package com.example.user_demo.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "carteras")
public class Cartera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartera;

    // N:1 con Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private double balanceTotal; // Dinero Fiat (Dólares/Euros)

    // --- CAMBIO CLAVE: REEMPLAZAMOS @ManyToMany POR @OneToMany ---
    // Antes: Set<Criptomoneda> (Solo guardaba qué criptos tenías)
    // Ahora: List<Activo> (Guarda qué criptos y QUÉ CANTIDAD)
    @OneToMany(mappedBy = "cartera", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Activo> activos = new ArrayList<>();

    // Constructores
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

    public double getBalanceTotal() {
        return balanceTotal;
    }

    public void setBalanceTotal(double balanceTotal) {
        this.balanceTotal = balanceTotal;
    }

    // Getter y Setter para la nueva lista de Activos
    public List<Activo> getActivos() {
        return activos;
    }

    public void setActivos(List<Activo> activos) {
        this.activos = activos;
    }

    // --- Helpers (Métodos de ayuda modificados) ---

    /**
     * Nuevo helper: Añade un ACTIVO (Cripto + Cantidad)
     */
    public void addActivo(Criptomoneda cripto, Double cantidad) {
        Activo activo = new Activo(this, cripto, cantidad);
        this.activos.add(activo);
    }

    public void removeActivo(Activo activo) {
        this.activos.remove(activo);
        activo.setCartera(null);
    }

    /**
     * Calcula el valor total de la cartera sumando el dinero FIAT
     * más el valor de mercado de todos los activos cripto.
     * Thymeleaf lo usará como ${cartera.patrimonioEstimado}
     */
    public Double getPatrimonioEstimado() {
        double total = this.balanceTotal; // Empezamos con los dólares

        if (this.activos != null) {
            for (Activo activo : this.activos) {
                if (activo.getCriptomoneda() != null && activo.getCriptomoneda().getPrecioActual() != null) {
                    // Cantidad * Precio
                    total += (activo.getCantidad() * activo.getCriptomoneda().getPrecioActual());
                }
            }
        }
        return total;
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