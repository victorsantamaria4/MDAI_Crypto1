package com.example.user_demo;

import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.HistorialRepository;
import com.example.user_demo.data.repository.UsuarioRepository;
import com.example.user_demo.data.services.UsuarioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialRepository historialRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // --- TEST 1: Creación Exitosa (Happy Path) ---
    @Test
    @DisplayName("Crear usuario: Debería guardar usuario e historial correctamente cuando los datos son válidos")
    void crearUsuario_Exitoso() {
        // Datos de prueba
        String nombre = "JuanPerez";
        String email = "juan.perez@example.com";
        String detalle = "Creación inicial";

        // Simulamos que el email NO existe en la BD
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Simulamos el guardado: devolvemos el mismo usuario que entra
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setIdUsuario(1L); // Simulamos que la BD le asigna ID
            return u;
        });

        // Ejecución
        Usuario resultado = usuarioService.crearUsuario(nombre, email, detalle);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdUsuario());
        assertEquals("JuanPerez", resultado.getNombre());
        assertEquals(email, resultado.getEmail());

        // Verificamos que se creó y vinculó el historial
        assertNotNull(resultado.getHistorial());
        assertEquals(detalle, resultado.getHistorial().getDetalle());
        assertEquals(resultado, resultado.getHistorial().getUsuario()); // Relación bidireccional

        // Verificamos que se llamó al repositorio
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // --- TEST 2: Validaciones de Nombre ---
    @Test
    @DisplayName("Crear usuario: Debería lanzar excepción si el nombre es muy corto")
    void crearUsuario_NombreCorto() {
        // Ejecución y Verificación
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario("Jo", "test@example.com", "Detalle");
        });

        assertTrue(exception.getMessage().contains("al menos 3 caracteres"));

        // Aseguramos que NUNCA se llamó al repositorio
        verify(usuarioRepository, never()).save(any());
    }

    // --- TEST 3: Validación de Email Inválido ---
    @Test
    @DisplayName("Crear usuario: Debería lanzar excepción con formato de email inválido")
    void crearUsuario_EmailInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario("Juan", "email-sin-arroba.com", "Detalle");
        });
    }

    // --- TEST 4: Validación de Email Duplicado ---
    @Test
    @DisplayName("Crear usuario: Debería lanzar excepción si el email ya existe")
    void crearUsuario_EmailDuplicado() {
        String email = "duplicado@example.com";

        // Simulamos que YA existe un usuario con ese email
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario("NuevoUsuario", email, "Detalle");
        });

        assertTrue(exception.getMessage().contains("ya está registrado"));
        verify(usuarioRepository, never()).save(any());
    }

    // --- TEST 5: Eliminar Usuario ---
    @Test
    @DisplayName("Eliminar usuario: Debería llamar a delete si el usuario existe")
    void eliminarUsuario_Exitoso() {
        Long id = 10L;
        Usuario usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(id);

        // Simulamos que el usuario existe
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioMock));

        // Ejecución
        usuarioService.eliminarUsuario(id);

        // Verificación
        verify(usuarioRepository).delete(usuarioMock);
    }

    @Test
    @DisplayName("Eliminar usuario: Debería lanzar excepción si el usuario no existe")
    void eliminarUsuario_NoEncontrado() {
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            usuarioService.eliminarUsuario(id);
        });

        verify(usuarioRepository, never()).delete(any());
    }
}