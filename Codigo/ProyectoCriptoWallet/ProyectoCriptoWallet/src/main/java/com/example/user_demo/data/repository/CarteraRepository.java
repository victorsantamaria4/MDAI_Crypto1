package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Cartera;
import com.example.user_demo.data.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarteraRepository extends CrudRepository<Cartera, Long> {

    /**
     * Encuentra todas las carteras pertenecientes a un usuario específico.
     */
    List<Cartera> findByUsuario(Usuario usuario);

    /**
     * Encuentra carteras que tengan un balance superior a una cantidad dada.
     */
    List<Cartera> findByBalanceTotalGreaterThan(Double balance);

    /**
     * Suma el balance total de todas las carteras de un usuario.
     * @param usuario El usuario del que se quiere sumar el balance.
     * @return Un Double con el total, o 0.0 si no tiene carteras.
     */
    @Query("SELECT COALESCE(SUM(c.balanceTotal), 0.0) FROM Cartera c WHERE c.usuario = :usuario")
    Double getBalanceTotalPorUsuario(@Param("usuario") Usuario usuario);
}

