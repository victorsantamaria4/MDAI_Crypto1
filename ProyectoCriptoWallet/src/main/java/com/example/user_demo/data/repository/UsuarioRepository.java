package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Iterable<Usuario> findByNombreStartingWith(String prefijo);

    @Query("SELECT DISTINCT u FROM Usuario u " +
            "JOIN u.carteras c " +
            "JOIN c.activos a " +          // <-- Cambiado
            "JOIN a.criptomoneda cripto " + // <-- Cambiado
            "WHERE cripto.simbolo = :simbolo")
    Iterable<Usuario> findUsuariosByCriptoSimbolo(@Param("simbolo") String simbolo);

    @Query("SELECT c.usuario FROM Cartera c GROUP BY c.usuario HAVING COUNT(c) > 1")
    Iterable<Usuario> findUsuariosConMultiplesCarteras();
}