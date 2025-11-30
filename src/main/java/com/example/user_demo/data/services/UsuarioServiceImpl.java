package com.example.user_demo.data.services;

import com.example.user_demo.data.model.Historial;
import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.HistorialRepository;
import com.example.user_demo.data.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementación de la lógica de negocio para Usuarios.
 */
@Service // Marca esta clase como un Bean de Servicio
public class UsuarioServiceImpl implements com.example.user_demo.data.services.UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;

    // Regex simple pero efectivo para email
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, HistorialRepository historialRepository) {
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Usuario crearUsuario(String nombre, String email, String detalleHistorial) {
        // --- VALIDACIONES ROBUSTAS ---
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío ni contener solo espacios.");
        }

        // --- NUEVA VALIDACIÓN: Longitud mínima del nombre ---
        if (nombre.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres.");
        }

        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("El formato del email no es válido: " + email);
        }
        if (detalleHistorial == null || detalleHistorial.trim().isEmpty()) {
            throw new IllegalArgumentException("El detalle del historial inicial es obligatorio.");
        }

        // Validar que el email no exista ya (Integridad de datos)
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email '" + email + "' ya está registrado en el sistema.");
        }

        // Limpieza de datos (Sanitization)
        String nombreLimpio = nombre.trim();
        String emailLimpio = email.trim();

        // --- LÓGICA DE NEGOCIO ---
        Usuario nuevoUsuario = new Usuario(nombreLimpio, emailLimpio);
        Historial nuevoHistorial = new Historial(nuevoUsuario,detalleHistorial);

        // Helper para relación bidireccional
        nuevoUsuario.setHistorial(nuevoHistorial);

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se puede eliminar. Usuario no encontrado con id: " + id));

        usuarioRepository.delete(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Usuario> getUsuariosConMultiplesCarteras() {
        return usuarioRepository.findUsuariosConMultiplesCarteras();
    }
}