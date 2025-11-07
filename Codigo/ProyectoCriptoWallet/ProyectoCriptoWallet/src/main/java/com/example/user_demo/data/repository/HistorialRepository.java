package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Historial;
import com.example.user_demo.data.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialRepository extends CrudRepository<Historial, Long> {

    /**
     * Encuentra el historial asociado a un usuario.
     */
    Optional<Historial> findByUsuario(Usuario usuario);

    /**
     * Encuentra un historial buscando por el email del usuario asociado.
     */
    Optional<Historial> findByUsuario_Email(String email);

    /**
     * Cuenta cuántos historiales tienen al menos una transacción.
     */
    @Query("SELECT COUNT(h) FROM Historial h WHERE h.usuario.transaccionesEnviadas IS NOT EMPTY OR h.usuario.transaccionesRecibidas IS NOT EMPTY")
    Long countHistorialesConTransacciones();
}

