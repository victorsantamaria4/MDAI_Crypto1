package com.example.user_demo.data.services;

import com.example.user_demo.data.model.Transaccion;
import java.util.List;

public interface TransaccionService {
    // Añadimos Long carteraOrigenId
    Transaccion realizarTransferencia(Long origenId, Long destinoId, Long carteraOrigenId, String criptoSimbolo, Double cantidad);

    List<Transaccion> getTransaccionesDeUsuario(Long usuarioId);
}