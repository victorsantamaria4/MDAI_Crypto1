package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Activo;
import com.example.user_demo.data.model.Cartera;
import com.example.user_demo.data.model.Criptomoneda;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ActivoRepository extends CrudRepository<Activo, Long> {
    // Buscar un activo específico (ej: Bitcoin) dentro de una cartera concreta
    Optional<Activo> findByCarteraAndCriptomoneda(Cartera cartera, Criptomoneda criptomoneda);
}
