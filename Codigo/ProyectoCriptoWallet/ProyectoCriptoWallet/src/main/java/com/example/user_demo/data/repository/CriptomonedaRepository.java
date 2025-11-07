package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Criptomoneda;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriptomonedaRepository extends CrudRepository<Criptomoneda, Long> {

    /**
     * Busca una criptomoneda por su símbolo (ej. "BTC", "ETH").
     */
    Optional<Criptomoneda> findBySimbolo(String simbolo);

    /**
     * Busca una criptomoneda por su nombre completo.
     */
    Optional<Criptomoneda> findByNombre(String nombre);

    /**
     * Encuentra todas las criptomonedas que no están en ninguna cartera.
     * Útil para saber qué criptos "nuevas" o "sin usar" hay en el sistema.
     */
    @Query("SELECT c FROM Criptomoneda c WHERE c.carteras IS EMPTY")
    List<Criptomoneda> findCriptomonedasSinCartera();

    @Query("SELECT c FROM Criptomoneda c JOIN c.carteras cartera WHERE cartera.id = :carteraId")
    List<Criptomoneda> findCriptomonedasByCarterasId(@Param("carteraId") Long carteraId);
}

