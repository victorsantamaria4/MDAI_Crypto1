package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    // --- Métodos de consulta derivados (simples) ---

    /**
     * Busca un usuario por su email (que debería ser único).
     * @param email Email a buscar.
     * @return Optional con el Usuario si se encuentra.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuarios cuyo nombre empiece por un prefijo.
     * @param prefijo El texto por el que debe empezar el nombre.
     * @return Un iterable de Usuarios.
     */
    Iterable<Usuario> findByNombreStartingWith(String prefijo);


    // --- Método de consulta con @Query (complejo) ---

    /**
     * Encuentra todos los usuarios que posean una criptomoneda específica
     * en CUALQUIERA de sus carteras.
     * @param simbolo El símbolo de la cripto (ej. "BTC").
     * @return Una lista de usuarios únicos.
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
            "JOIN u.carteras c " +
            "JOIN c.criptomonedas cripto " +
            "WHERE cripto.simbolo = :simbolo")
    Iterable<Usuario> findUsuariosByCriptoSimbolo(@Param("simbolo") String simbolo);

    /**
     * Busca usuarios que poseen más de una cartera.
     * Esta es una consulta JPQL personalizada.
     * @return Lista de usuarios con múltiples carteras.
     */
    @Query("SELECT c.usuario FROM Cartera c GROUP BY c.usuario HAVING COUNT(c) > 1")
    Iterable<Usuario> findUsuariosConMultiplesCarteras();
}
