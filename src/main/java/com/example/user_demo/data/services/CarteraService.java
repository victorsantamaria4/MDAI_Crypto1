package com.example.user_demo.data.services;

import com.example.user_demo.data.model.Cartera;
import java.util.Optional;

/**
 * Interfaz para el servicio de lógica de negocio de Carteras.
 */
public interface CarteraService {

    Optional<Cartera> getCarteraById(Long id);

    Iterable<Cartera> getCarterasByUsuarioEmail(String email);

    /**
     * Crea una nueva cartera para un usuario.
     * @param emailUsuario Email del usuario propietario.
     * @param balanceInicial Balance inicial de la cartera.
     * @return La cartera creada.
     * @throws java.util.NoSuchElementException Si el usuario no existe.
     */
    Cartera crearCartera(String emailUsuario, double balanceInicial);

    /**
     * Añade una criptomoneda a una cartera (Relación N:M).
     * @param idCartera ID de la cartera.
     * @param idCripto ID de la criptomoneda a añadir.
     * @return La cartera actualizada.
     * @throws java.util.NoSuchElementException Si la cartera o la cripto no existen.
     */
    Cartera addCriptomonedaACartera(Long idCartera, Long idCripto);

    /**
     * Quita una criptomoneda de una cartera (Relación N:M).
     * @param idCartera ID de la cartera.
     * @param idCripto ID de la criptomoneda a quitar.
     * @return La cartera actualizada.
     * @throws java.util.NoSuchElementException Si la cartera o la cripto no existen.
     */
    Cartera removeCriptomonedaDeCartera(Long idCartera, Long idCripto);

    Double getBalanceTotalPorUsuario(String emailUsuario);

    void invertirEnCripto(Long carteraId, Long criptoId, Double cantidadInversionFiat);
}