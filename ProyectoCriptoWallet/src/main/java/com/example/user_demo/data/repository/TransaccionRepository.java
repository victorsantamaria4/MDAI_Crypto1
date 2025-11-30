package com.example.user_demo.data.repository;

import com.example.user_demo.data.model.Criptomoneda;
import com.example.user_demo.data.model.Transaccion;
import com.example.user_demo.data.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends CrudRepository<Transaccion, Long> {

    /**
     * Encuentra todas las transacciones enviadas por un usuario.
     */
    List<Transaccion> findByUsuarioOrigen(Usuario usuario);

    /**
     * Encuentra todas las transacciones recibidas por un usuario.
     */
    List<Transaccion> findByUsuarioDestino(Usuario usuario);

    /**
     * Encuentra todas las transacciones de una criptomoneda específica.
     */
    List<Transaccion> findByCriptomoneda(Criptomoneda criptomoneda);

    /**
     * (NUEVA FUNCIÓN) Busca transacciones entre un rango de fechas.
     */
    @Query("SELECT t FROM Transaccion t WHERE t.fecha BETWEEN :inicio AND :fin")
    Iterable<Transaccion> findTransaccionesEnRangoDeFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * (NUEVA FUNCIÓN) Busca transacciones donde un usuario se envió dinero a sí mismo.
     */
    @Query("SELECT t FROM Transaccion t WHERE t.usuarioOrigen = t.usuarioDestino")
    Iterable<Transaccion> findTransaccionesInternas();
    /**
     * Encuentra todas las transacciones (enviadas O recibidas)
     * de un usuario en particular.
     * Esta es la consulta compleja que une todo.
     */
    @Query("SELECT t FROM Transaccion t " +
            "WHERE t.usuarioOrigen = :usuario OR t.usuarioDestino = :usuario " +
            "ORDER BY t.fecha DESC")
    List<Transaccion> findAllTransaccionesByUsuario(@Param("usuario") Usuario usuario);
}
