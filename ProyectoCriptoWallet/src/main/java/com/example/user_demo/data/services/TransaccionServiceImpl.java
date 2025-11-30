package com.example.user_demo.data.services;

import com.example.user_demo.data.model.*;
import com.example.user_demo.data.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CriptomonedaRepository criptomonedaRepository;
    private final HistorialRepository historialRepository;
    private final CarteraRepository carteraRepository;
    private final ActivoRepository activoRepository; // Necesario para gestionar cantidades exactas

    @Autowired
    public TransaccionServiceImpl(TransaccionRepository transaccionRepository,
                                  UsuarioRepository usuarioRepository,
                                  CriptomonedaRepository criptomonedaRepository,
                                  HistorialRepository historialRepository,
                                  CarteraRepository carteraRepository,
                                  ActivoRepository activoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.criptomonedaRepository = criptomonedaRepository;
        this.historialRepository = historialRepository;
        this.carteraRepository = carteraRepository;
        this.activoRepository = activoRepository;
    }

    @Override
    @Transactional
    public Transaccion realizarTransferencia(Long origenId, Long destinoId, Long carteraOrigenId, String criptoSimbolo, Double cantidadFiat) {

        // --- 1. VALIDACIONES BÁSICAS ---
        if (origenId == null || destinoId == null || carteraOrigenId == null) {
            throw new IllegalArgumentException("IDs obligatorios.");
        }
        if (cantidadFiat == null || cantidadFiat <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva.");
        }
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("No puedes enviarte activos a ti mismo.");
        }

        // --- 2. BUSCAR ENTIDADES ---
        Usuario origen = usuarioRepository.findById(origenId)
                .orElseThrow(() -> new NoSuchElementException("Usuario Origen no encontrado."));

        Usuario destino = usuarioRepository.findById(destinoId)
                .orElseThrow(() -> new NoSuchElementException("Usuario Destino no encontrado."));

        Criptomoneda cripto = criptomonedaRepository.findBySimbolo(criptoSimbolo)
                .orElseThrow(() -> new NoSuchElementException("Criptomoneda no soportada: " + criptoSimbolo));

        // --- 3. LÓGICA DE CONVERSIÓN (FIAT -> CRIPTO) ---
        // Aquí arreglamos el problema: convertimos los Dólares del formulario a unidades de Cripto
        if (cripto.getPrecioActual() == null || cripto.getPrecioActual() <= 0) {
            throw new IllegalStateException("La criptomoneda " + cripto.getSimbolo() + " no tiene un precio configurado.");
        }

        // Ejemplo: Envío $50,000 y el Bitcoin vale $50,000 -> Son 1.0 BTC
        Double unidadesCripto = cantidadFiat / cripto.getPrecioActual();

        // --- 4. GESTIÓN DE LA CARTERA DE ORIGEN ---
        Cartera carteraOrigen = carteraRepository.findById(carteraOrigenId)
                .orElseThrow(() -> new NoSuchElementException("La cartera seleccionada no existe."));

        // Seguridad
        if (!carteraOrigen.getUsuario().getIdUsuario().equals(origenId)) {
            throw new SecurityException("Error de seguridad: La cartera no pertenece al usuario.");
        }

        // --- GESTIÓN DE ACTIVOS (RESTAR AL ORIGEN) ---

        // Buscamos si el usuario tiene esa cripto (Activo)
        Activo activoOrigen = activoRepository.findByCarteraAndCriptomoneda(carteraOrigen, cripto)
                .orElseThrow(() -> new IllegalArgumentException("No posees " + cripto.getNombre() + " en esta cartera."));

        // Validamos si tiene suficientes UNIDADES (no dólares)
        if (activoOrigen.getCantidad() < unidadesCripto) {
            throw new IllegalArgumentException(String.format(
                    "Saldo insuficiente. Tienes %.4f %s (Valor: $%.2f), intentas enviar $%.2f",
                    activoOrigen.getCantidad(), cripto.getSimbolo(),
                    activoOrigen.getCantidad() * cripto.getPrecioActual(), cantidadFiat));
        }

        // Restamos las unidades
        activoOrigen.setCantidad(activoOrigen.getCantidad() - unidadesCripto);
        activoRepository.save(activoOrigen);

        // --- 5. GESTIÓN DE LA CARTERA DESTINO (SUMAR AL DESTINO) ---
        List<Cartera> carterasDestino = carteraRepository.findByUsuario(destino);
        if (carterasDestino.isEmpty()) {
            throw new IllegalStateException("El usuario destino no tiene carteras creadas para recibir activos.");
        }
        Cartera carteraDestino = carterasDestino.get(0); // Usamos la primera disponible

        // Buscamos o creamos el activo en el destino
        Activo activoDestino = activoRepository.findByCarteraAndCriptomoneda(carteraDestino, cripto)
                .orElse(new Activo(carteraDestino, cripto, 0.0));

        // Sumamos las unidades
        activoDestino.setCantidad(activoDestino.getCantidad() + unidadesCripto);
        activoRepository.save(activoDestino);

        // --- 6. REGISTRAR TRANSACCIÓN ---
        // Guardamos la cantidad en unidades cripto (para referencia técnica)
        Transaccion tx = new Transaccion(origen, destino, cripto, unidadesCripto);
        Transaccion guardada = transaccionRepository.save(tx);

        // --- 7. ACTUALIZAR HISTORIALES ---
        // Mostramos ambas cantidades para que el usuario entienda qué pasó
        actualizarHistoriales(origen, destino, cripto, cantidadFiat, unidadesCripto);

        return guardada;
    }

    private void actualizarHistoriales(Usuario origen, Usuario destino, Criptomoneda cripto, Double fiat, Double unidades) {
        // Formato: "TX $100.00 (0.0020 BTC)"
        String detalle = String.format("TX $%.2f (%.4f %s)", fiat, unidades, cripto.getSimbolo());

        if (origen.getHistorial() != null) {
            String log = "\n[ENV] " + detalle + " a " + destino.getNombre();
            origen.getHistorial().setDetalle(origen.getHistorial().getDetalle() + log);
            historialRepository.save(origen.getHistorial());
        }
        if (destino.getHistorial() != null) {
            String log = "\n[REC] " + detalle + " de " + origen.getNombre();
            destino.getHistorial().setDetalle(destino.getHistorial().getDetalle() + log);
            historialRepository.save(destino.getHistorial());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> getTransaccionesDeUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        return StreamSupport.stream(transaccionRepository.findAllTransaccionesByUsuario(usuario).spliterator(), false)
                .collect(Collectors.toList());
    }
}