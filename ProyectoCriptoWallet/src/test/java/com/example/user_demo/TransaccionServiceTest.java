package com.example.user_demo;

import com.example.user_demo.data.model.Criptomoneda;
import com.example.user_demo.data.model.Historial;
import com.example.user_demo.data.model.Transaccion;
import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.CriptomonedaRepository;
import com.example.user_demo.data.repository.HistorialRepository;
import com.example.user_demo.data.repository.TransaccionRepository;
import com.example.user_demo.data.repository.UsuarioRepository;
import com.example.user_demo.data.services.TransaccionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CriptomonedaRepository criptomonedaRepository;
    @Mock
    private HistorialRepository historialRepository;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    // --- TEST 1: Transferencia Exitosa (Happy Path) ---
    @Test
    @DisplayName("Transferencia: Debería crear transacción y actualizar historiales correctamente")
    void realizarTransferencia_Exitoso() {
        // 1. Datos de prueba
        Long idOrigen = 1L;
        Long idDestino = 2L;
        String simbolo = "BTC";
        Double cantidad = 0.5;

        Usuario origen = new Usuario("Origen", "origen@test.com");
        origen.setIdUsuario(idOrigen);
        // Le damos un historial inicial para verificar que se actualiza
        origen.setHistorial(new Historial(origen, "Inicio"));

        Usuario destino = new Usuario("Destino", "destino@test.com");
        destino.setIdUsuario(idDestino);
        destino.setHistorial(new Historial(destino, "Inicio"));

        Criptomoneda bitcoin = new Criptomoneda("Bitcoin", simbolo);

        // 2. Simulamos el comportamiento de los Repositorios
        when(usuarioRepository.findById(idOrigen)).thenReturn(Optional.of(origen));
        when(usuarioRepository.findById(idDestino)).thenReturn(Optional.of(destino));
        when(criptomonedaRepository.findBySimbolo(simbolo)).thenReturn(Optional.of(bitcoin));

        // Simulamos el guardado de la transacción
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(i -> i.getArguments()[0]);

        // 3. Ejecución
        Transaccion resultado = transaccionService.realizarTransferencia(idOrigen, idDestino, simbolo, cantidad);

        // 4. Verificaciones
        assertNotNull(resultado);
        assertEquals(origen, resultado.getUsuarioOrigen());
        assertEquals(destino, resultado.getUsuarioDestino());
        assertEquals(cantidad, resultado.getCantidad());

        // Verificamos que se actualizó el texto del historial del Origen
        assertTrue(origen.getHistorial().getDetalle().contains("[ENV] TX 0,50 BTC"));
        // Nota: El formato %.2f usa coma o punto según el locale del sistema, ajusta si es necesario

        // Verificamos que se guardaron los historiales
        verify(historialRepository).saveAll(anyList());
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    // --- TEST 2: Validación de Auto-Transferencia ---
    @Test
    @DisplayName("Transferencia: Debería fallar si origen y destino son iguales")
    void realizarTransferencia_MismoUsuario() {
        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.realizarTransferencia(1L, 1L, "BTC", 10.0);
        });

        // Aseguramos que nada se guardó
        verify(transaccionRepository, never()).save(any());
    }

    // --- TEST 3: Validación de Cantidad ---
    @Test
    @DisplayName("Transferencia: Debería fallar con cantidades negativas o cero")
    void realizarTransferencia_CantidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.realizarTransferencia(1L, 2L, "BTC", -5.0);
        });
    }

    // --- TEST 4: Usuario No Encontrado ---
    @Test
    @DisplayName("Transferencia: Debería lanzar excepción si el usuario origen no existe")
    void realizarTransferencia_UsuarioNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            transaccionService.realizarTransferencia(99L, 2L, "BTC", 1.0);
        });
    }

    // --- TEST 5: Criptomoneda No Soportada ---
    @Test
    @DisplayName("Transferencia: Debería lanzar excepción si la cripto no existe")
    void realizarTransferencia_CriptoNoExiste() {
        Long idOr = 1L;
        Long idDes = 2L;

        when(usuarioRepository.findById(idOr)).thenReturn(Optional.of(new Usuario()));
        when(usuarioRepository.findById(idDes)).thenReturn(Optional.of(new Usuario()));

        // Simulamos que no encuentra la moneda
        when(criptomonedaRepository.findBySimbolo("DOGE")).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            transaccionService.realizarTransferencia(idOr, idDes, "DOGE", 100.0);
        });

        assertTrue(ex.getMessage().contains("no soportada"));
    }

    // --- TEST 6: Obtener Transacciones ---
    @Test
    @DisplayName("Get Transacciones: Debería devolver la lista encontrada por el repositorio")
    void getTransaccionesDeUsuario_Exitoso() {
        Long userId = 1L;
        Usuario user = new Usuario();
        user.setIdUsuario(userId);

        Transaccion t1 = new Transaccion();
        List<Transaccion> listaSimulada = Arrays.asList(t1);

        // Mocks
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transaccionRepository.findAllTransaccionesByUsuario(user)).thenReturn(listaSimulada);

        // Ejecución
        List<Transaccion> resultado = transaccionService.getTransaccionesDeUsuario(userId);

        // Verificación
        assertEquals(1, resultado.size());
        verify(transaccionRepository).findAllTransaccionesByUsuario(user);
    }
}
