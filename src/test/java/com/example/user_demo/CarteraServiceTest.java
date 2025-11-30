package com.example.user_demo;

import com.example.user_demo.data.model.Cartera;
import com.example.user_demo.data.model.Criptomoneda;
import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.CarteraRepository;
import com.example.user_demo.data.repository.CriptomonedaRepository;
import com.example.user_demo.data.repository.UsuarioRepository;
import com.example.user_demo.data.services.CarteraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test Unitario completo para CarteraService.
 */
@ExtendWith(MockitoExtension.class)
public class CarteraServiceTest {

    @Mock
    private CarteraRepository carteraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CriptomonedaRepository criptomonedaRepository;

    @InjectMocks
    private CarteraServiceImpl carteraService;

    // Datos de prueba reutilizables
    private Usuario usuarioMock;
    private Cartera carteraMock;
    private Criptomoneda criptoMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario("Pepe", "pepe@test.com");
        usuarioMock.setIdUsuario(1L);

        carteraMock = new Cartera(usuarioMock, 100.0);
        carteraMock.setIdCartera(1L);
        carteraMock.setCriptomonedas(new HashSet<>()); // Set vacío inicial

        criptoMock = new Criptomoneda("Bitcoin", "BTC");
        criptoMock.setIdCripto(1L);
        criptoMock.setCarteras(new HashSet<>()); // Importante para relación bidireccional
    }

    // --- 1. Tests de Lectura (Faltaban en tu versión) ---

    @Test
    @DisplayName("Get Cartera por ID - Éxito")
    void testGetCarteraById_Exito() {
        when(carteraRepository.findById(1L)).thenReturn(Optional.of(carteraMock));

        Optional<Cartera> resultado = carteraService.getCarteraById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdCartera());
    }

    @Test
    @DisplayName("Get Cartera por ID - Fallo: ID inválido")
    void testGetCarteraById_IdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> carteraService.getCarteraById(-5L));
        assertThrows(IllegalArgumentException.class, () -> carteraService.getCarteraById(null));
    }

    @Test
    @DisplayName("Get Carteras por Email - Éxito")
    void testGetCarterasByUsuarioEmail_Exito() {
        when(usuarioRepository.findByEmail("pepe@test.com")).thenReturn(Optional.of(usuarioMock));
        when(carteraRepository.findByUsuario(usuarioMock)).thenReturn(Collections.singletonList(carteraMock));

        Iterable<Cartera> resultados = carteraService.getCarterasByUsuarioEmail("pepe@test.com");

        assertNotNull(resultados);
        assertEquals(carteraMock, resultados.iterator().next());
    }

    @Test
    @DisplayName("Get Carteras por Email - Fallo: Usuario no encontrado")
    void testGetCarterasByUsuarioEmail_UsuarioNoExiste() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                carteraService.getCarterasByUsuarioEmail("nadie@test.com")
        );
    }

    // --- 2. Tests para crearCartera ---

    @Test
    @DisplayName("Crear Cartera - Éxito: Datos válidos")
    void testCrearCartera_Exito() {
        String email = "pepe@test.com";
        Double balance = 500.0;

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioMock));
        // Mockeamos que al guardar devuelva el argumento pasado
        when(carteraRepository.save(any(Cartera.class))).thenAnswer(i -> i.getArguments()[0]);

        Cartera resultado = carteraService.crearCartera(email, balance);

        assertNotNull(resultado);
        assertEquals(balance, resultado.getBalanceTotal());
        assertEquals(usuarioMock, resultado.getUsuario());

        // Verificar que se usó el helper addCartera del usuario (la lista del usuario debe tener la cartera)
        assertTrue(usuarioMock.getCarteras().contains(resultado));

        verify(carteraRepository).save(any(Cartera.class));
    }

    @Test
    @DisplayName("Crear Cartera - Fallo: Email inválido")
    void testCrearCartera_EmailInvalido() {
        assertThrows(IllegalArgumentException.class, () -> carteraService.crearCartera("email-mal", 100.0));
        verifyNoInteractions(usuarioRepository);
    }

    // --- 3. Tests para addCriptomonedaACartera ---

    @Test
    @DisplayName("Añadir Cripto - Éxito")
    void testAddCripto_Exito() {
        Long idCartera = 1L;
        Long idCripto = 1L;

        when(carteraRepository.findById(idCartera)).thenReturn(Optional.of(carteraMock));
        when(criptomonedaRepository.findById(idCripto)).thenReturn(Optional.of(criptoMock));
        when(carteraRepository.save(any(Cartera.class))).thenAnswer(i -> i.getArguments()[0]);

        Cartera resultado = carteraService.addCriptomonedaACartera(idCartera, idCripto);

        // Verificamos relación en memoria
        assertTrue(resultado.getCriptomonedas().contains(criptoMock), "La cartera debe contener la cripto");
        assertTrue(criptoMock.getCarteras().contains(resultado), "La cripto debe tener la referencia a la cartera");

        verify(carteraRepository).save(carteraMock);
    }

    @Test
    @DisplayName("Añadir Cripto - Fallo: Criptomoneda no existe")
    void testAddCripto_CriptoNoExiste() {
        when(carteraRepository.findById(1L)).thenReturn(Optional.of(carteraMock));
        when(criptomonedaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                carteraService.addCriptomonedaACartera(1L, 99L)
        );
    }

    // --- 4. Tests para removeCriptomonedaDeCartera ---

    @Test
    @DisplayName("Quitar Cripto - Éxito")
    void testRemoveCripto_Exito() {
        // Setup: Preparamos la relación existente
        carteraMock.addCriptomoneda(criptoMock);

        when(carteraRepository.findById(1L)).thenReturn(Optional.of(carteraMock));
        when(criptomonedaRepository.findById(1L)).thenReturn(Optional.of(criptoMock));
        when(carteraRepository.save(any(Cartera.class))).thenReturn(carteraMock);

        Cartera resultado = carteraService.removeCriptomonedaDeCartera(1L, 1L);

        assertFalse(resultado.getCriptomonedas().contains(criptoMock));
        verify(carteraRepository).save(carteraMock);
    }

    @Test
    @DisplayName("Quitar Cripto - Fallo: No está en la cartera")
    void testRemoveCripto_NoEstaEnCartera() {
        // Setup: Cartera vacía
        when(carteraRepository.findById(1L)).thenReturn(Optional.of(carteraMock));
        when(criptomonedaRepository.findById(1L)).thenReturn(Optional.of(criptoMock));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                carteraService.removeCriptomonedaDeCartera(1L, 1L)
        );
        assertTrue(ex.getMessage().contains("no contiene"));
    }

    // --- 5. Tests para getBalanceTotalPorUsuario ---

    @Test
    @DisplayName("Get Balance - Éxito")
    void testGetBalance_Exito() {
        when(usuarioRepository.findByEmail("pepe@test.com")).thenReturn(Optional.of(usuarioMock));
        when(carteraRepository.getBalanceTotalPorUsuario(usuarioMock)).thenReturn(1500.0);

        Double balance = carteraService.getBalanceTotalPorUsuario("pepe@test.com");

        assertEquals(1500.0, balance);
    }
}