package com.example.user_demo.data.services;

import com.example.user_demo.data.model.Usuario;
import java.util.Optional;

/**
 * Interfaz para el servicio de lógica de negocio de Usuarios.
 * Define las operaciones de alto nivel que se pueden realizar.
 */
public interface UsuarioService {

    Iterable<Usuario> getAllUsuarios();

    Optional<Usuario> getUsuarioById(Long id);

    Optional<Usuario> findByEmail(String email);

    /**
     * Crea un nuevo usuario y su historial asociado.
     * Valida los datos de entrada.
     * @param nombre Nombre del usuario.
     * @param email Email del usuario (debe ser único y válido).
     * @param detalleHistorial Detalle inicial para el historial.
     * @return El usuario guardado.
     * @throws IllegalArgumentException Si el nombre/email/detalle son inválidos o el email ya existe.
     */
    Usuario crearUsuario(String nombre, String email, String detalleHistorial);

    /**
     * Elimina un usuario por su ID.
     * La lógica de cascada (borrar carteras, tx, historial) es manejada por JPA.
     * @param id ID del usuario a eliminar.
     * @throws java.util.NoSuchElementException Si el usuario no existe.
     */
    void eliminarUsuario(Long id);

    Iterable<Usuario> getUsuariosConMultiplesCarteras();
}