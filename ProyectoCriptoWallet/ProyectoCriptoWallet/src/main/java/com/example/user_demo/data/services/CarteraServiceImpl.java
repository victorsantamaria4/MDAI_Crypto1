package com.example.user_demo.data.services;

import com.example.user_demo.data.model.Activo;
import com.example.user_demo.data.model.Cartera;
import com.example.user_demo.data.model.Criptomoneda;
import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.ActivoRepository; // <--- NUEVO
import com.example.user_demo.data.repository.CarteraRepository;
import com.example.user_demo.data.repository.CriptomonedaRepository;
import com.example.user_demo.data.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CarteraServiceImpl implements CarteraService {

    private final CarteraRepository carteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final CriptomonedaRepository criptomonedaRepository;
    private final ActivoRepository activoRepository; // <--- Inyectamos el repo de Activos

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    @Autowired
    public CarteraServiceImpl(CarteraRepository carteraRepository,
                              UsuarioRepository usuarioRepository,
                              CriptomonedaRepository criptomonedaRepository,
                              ActivoRepository activoRepository) {
        this.carteraRepository = carteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.criptomonedaRepository = criptomonedaRepository;
        this.activoRepository = activoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cartera> getCarteraById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID de cartera inválido.");
        return carteraRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Cartera> getCarterasByUsuarioEmail(String email) {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("Formato de email inválido.");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + email));
        return carteraRepository.findByUsuario(usuario);
    }

    @Override
    @Transactional
    public Cartera crearCartera(String emailUsuario, double balanceInicial) {
        if (emailUsuario == null || !Pattern.matches(EMAIL_REGEX, emailUsuario)) {
            throw new IllegalArgumentException("Email inválido para crear cartera.");
        }
        if (balanceInicial < 0) {
            throw new IllegalArgumentException("El balance inicial no puede ser negativo.");
        }

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NoSuchElementException("No se puede crear cartera. Usuario no encontrado: " + emailUsuario));

        Cartera nuevaCartera = new Cartera(usuario, balanceInicial);
        usuario.addCartera(nuevaCartera);

        return carteraRepository.save(nuevaCartera);
    }

    @Override
    @Transactional
    public Cartera addCriptomonedaACartera(Long idCartera, Long idCripto) {
        if (idCartera == null || idCripto == null) {
            throw new IllegalArgumentException("Los IDs son obligatorios.");
        }

        Cartera cartera = carteraRepository.findById(idCartera)
                .orElseThrow(() -> new NoSuchElementException("Cartera no encontrada (ID: " + idCartera + ")"));
        Criptomoneda cripto = criptomonedaRepository.findById(idCripto)
                .orElseThrow(() -> new NoSuchElementException("Criptomoneda no encontrada (ID: " + idCripto + ")"));

        // --- LÓGICA NUEVA CON ACTIVOS ---
        // Verificamos si ya existe el activo en esa cartera
        Optional<Activo> activoExistente = activoRepository.findByCarteraAndCriptomoneda(cartera, cripto);

        if (activoExistente.isPresent()) {
            throw new IllegalArgumentException("La cartera ya contiene esa criptomoneda.");
        }

        // Si no existe, creamos un nuevo Activo con cantidad 0.0
        Activo nuevoActivo = new Activo(cartera, cripto, 0.0);
        activoRepository.save(nuevoActivo);

        // Forzamos la actualización de la lista en memoria para el retorno
        cartera.getActivos().add(nuevoActivo);

        return carteraRepository.save(cartera);
    }

    @Override
    @Transactional
    public Cartera removeCriptomonedaDeCartera(Long idCartera, Long idCripto) {
        if (idCartera == null || idCripto == null) throw new IllegalArgumentException("IDs obligatorios.");

        Cartera cartera = carteraRepository.findById(idCartera)
                .orElseThrow(() -> new NoSuchElementException("Cartera no encontrada."));
        Criptomoneda cripto = criptomonedaRepository.findById(idCripto)
                .orElseThrow(() -> new NoSuchElementException("Criptomoneda no encontrada."));

        // --- LÓGICA NUEVA CON ACTIVOS ---
        // Buscamos el activo específico
        Activo activo = activoRepository.findByCarteraAndCriptomoneda(cartera, cripto)
                .orElseThrow(() -> new IllegalArgumentException("No se puede eliminar. La cartera no contiene " + cripto.getSimbolo()));

        // Lo borramos
        activoRepository.delete(activo);

        // Actualizamos la lista en memoria para el retorno (opcional pero recomendado)
        cartera.getActivos().remove(activo);

        return carteraRepository.save(cartera);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getBalanceTotalPorUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

        double patrimonioTotal = 0.0;

        for (Cartera cartera : usuario.getCarteras()) {
            // 1. Sumar dinero FIAT (Dólares en la cartera)
            patrimonioTotal += cartera.getBalanceTotal();

            // 2. Sumar valor de los ACTIVOS (Cripto * Precio)
            for (Activo activo : cartera.getActivos()) {
                patrimonioTotal += (activo.getCantidad() * activo.getCriptomoneda().getPrecioActual());
            }
        }
        return patrimonioTotal;
    }

    // Inyectamos también ActivoRepository y CriptomonedaRepository

    @Override
    @Transactional
    public void invertirEnCripto(Long carteraId, Long criptoId, Double cantidadInversionFiat) {
        // 1. Validaciones
        if (cantidadInversionFiat <= 0) throw new IllegalArgumentException("La inversión debe ser positiva.");

        Cartera cartera = carteraRepository.findById(carteraId)
                .orElseThrow(() -> new NoSuchElementException("Cartera no encontrada"));

        Criptomoneda cripto = criptomonedaRepository.findById(criptoId)
                .orElseThrow(() -> new NoSuchElementException("Criptomoneda no encontrada"));

        // 2. Verificar si tiene saldo (Efectivo) suficiente
        if (cartera.getBalanceTotal() < cantidadInversionFiat) {
            throw new IllegalArgumentException(String.format("Saldo insuficiente. Tienes $%.2f, intentas invertir $%.2f",
                    cartera.getBalanceTotal(), cantidadInversionFiat));
        }

        // 3. Calcular cuánta cripto compra (Ej: 100$ / 50000$ = 0.002 BTC)
        Double precio = cripto.getPrecioActual();
        Double cantidadCriptoComprada = cantidadInversionFiat / precio;

        // 4. Ejecutar la transacción interna
        // A) Restamos el dinero
        cartera.setBalanceTotal(cartera.getBalanceTotal() - cantidadInversionFiat);

        // B) Sumamos (o creamos) el activo
        Activo activo = activoRepository.findByCarteraAndCriptomoneda(cartera, cripto)
                .orElse(new Activo(cartera, cripto, 0.0));

        activo.setCantidad(activo.getCantidad() + cantidadCriptoComprada);

        // 5. Guardar
        activoRepository.save(activo);
        carteraRepository.save(cartera);
    }

    @Override
    @Transactional
    public void eliminarCartera(Long id) {
        if (!carteraRepository.existsById(id)) {
            throw new NoSuchElementException("La cartera no existe.");
        }
        carteraRepository.deleteById(id);
    }
}