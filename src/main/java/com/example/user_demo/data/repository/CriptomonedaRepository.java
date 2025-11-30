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

    Optional<Criptomoneda> findBySimbolo(String simbolo);

    Optional<Criptomoneda> findByNombre(String nombre);

    /**
     * CONSULTA ARREGLADA:
     * Buscamos criptomonedas que NO existan en la tabla de Activos.
     */
    @Query("SELECT c FROM Criptomoneda c WHERE NOT EXISTS (SELECT a FROM Activo a WHERE a.criptomoneda = c)")
    List<Criptomoneda> findCriptomonedasSinCartera();

    /**
     * CONSULTA ARREGLADA:
     * Buscamos a través de la entidad Activo.
     */
    @Query("SELECT a.criptomoneda FROM Activo a WHERE a.cartera.idCartera = :carteraId")
    List<Criptomoneda> findCriptomonedasByCarterasId(@Param("carteraId") Long carteraId);
}