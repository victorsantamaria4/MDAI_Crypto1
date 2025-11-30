package com.example.user_demo;

import com.example.user_demo.data.model.*;
import com.example.user_demo.data.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TEST GENÉRICO (LÓGICA DE NEGOCIO)
 * * Este test valida una lógica de negocio compleja usando los 5 repositorios.
 * A diferencia del test CRUD, este SÍ usa data.sql para simular un escenario
 * sobre una base de datos con datos existentes.
 */
@SpringBootTest
@Sql("/data.sql") // Carga los datos de prueba (Ana, Luis, Carla...)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Limpia la BD después
public class UserDemoGenericTests {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CarteraRepository carteraRepository;
    @Autowired private CriptomonedaRepository criptomonedaRepository;
    @Autowired private HistorialRepository historialRepository;
    @Autowired private TransaccionRepository transaccionRepository;

    @Test
    @DisplayName("Test de Lógica: Crear nueva cartera, financiarla y realizar una transferencia")
    @Transactional // Requerido para manejar las cargas LAZY de las entidades
    void testLogica_CrearCartera_Financiar_Y_Transferir() {
        System.out.println("--- TEST GENÉRICO: Iniciando Lógica de Negocio ---");

        // --- 1. LECTURA (Repositorios: Usuario, Criptomoneda) ---
        // Obtenemos los participantes y la cripto de data.sql
        System.out.println("Leyendo datos iniciales (Luis, Ana, BTC)...");
        Usuario luis = usuarioRepository.findByEmail("luis@email.com").get(); // ID 2
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();   // ID 1
        Criptomoneda btc = criptomonedaRepository.findBySimbolo("BTC").get(); // ID 1

        // Verificamos el estado inicial de Luis
        assertThat(carteraRepository.findByUsuario(luis)).hasSize(1); // Luis solo tiene 1 cartera (ID 2)
        assertThat(transaccionRepository.count()).isEqualTo(3); // 3 transacciones en total

        // --- 2. CREACIÓN (Repositorio: Cartera) ---
        // Luis decide crear una nueva cartera de ahorros.
        System.out.println("CREANDO: Nueva cartera para Luis...");
        Cartera carteraAhorroLuis = new Cartera(luis,0.0); // Balance inicial 0
        // (Usamos el método de ayuda de la entidad Usuario para la bidireccionalidad)
        luis.addCartera(carteraAhorroLuis);

        // Guardamos la cartera (la cascada desde 'luis' no está configurada para 'save',
        // así que guardamos la cartera directamente).
        carteraRepository.save(carteraAhorroLuis);

        // Verificamos
        assertThat(carteraRepository.findByUsuario(luis)).hasSize(2);
        long idNuevaCartera = carteraAhorroLuis.getIdCartera();
        assertThat(idNuevaCartera).isNotNull();

        // --- 3. ACTUALIZACIÓN (Relación N:M) (Repositorio: Cartera) ---
        // Luis "funde" su nueva cartera añadiendo BTC.
        System.out.println("ACTUALIZANDO: Financiando nueva cartera con BTC (N:M)...");
        // (Usamos el método de ayuda de la entidad Cartera)
        carteraAhorroLuis.addCriptomoneda(btc);
        carteraRepository.save(carteraAhorroLuis);

        // Verificamos
        Cartera carteraLeida = carteraRepository.findById(idNuevaCartera).get();
        assertThat(carteraLeida.getCriptomonedas()).hasSize(1);
        assertThat(carteraLeida.getCriptomonedas()).contains(btc);

        // --- 4. CREACIÓN (Repositorio: Transaccion) ---
        // Luis envía 0.1 BTC a Ana
        System.out.println("CREANDO: Nueva transacción de Luis a Ana...");
        Transaccion nuevaTx = new Transaccion(luis, ana, btc,0.1);
        transaccionRepository.save(nuevaTx);

        // Verificamos
        assertThat(transaccionRepository.count()).isEqualTo(4); // 3 + 1 nueva
        assertThat(luis.getTransaccionesEnviadas()).hasSize(2); // La TX 3 (a Carla) + la nueva

        // --- 5. ACTUALIZACIÓN (Repositorio: Historial) ---
        // Actualizamos los historiales de ambos usuarios
        System.out.println("ACTUALIZANDO: Historiales de Luis y Ana...");
        Historial historialLuis = historialRepository.findByUsuario(luis).get();
        Historial historialAna = historialRepository.findByUsuario(ana).get();

        historialLuis.setDetalle(historialLuis.getDetalle() + "\n[NUEVA TX]: Envío 0.1 BTC a Ana");
        historialAna.setDetalle(historialAna.getDetalle() + "\n[NUEVA TX]: Recibo 0.1 BTC de Luis");

        historialRepository.save(historialLuis);
        historialRepository.save(historialAna);

        // Verificamos
        assertThat(historialRepository.findById(historialLuis.getIdHistorial()).get().getDetalle())
                .endsWith("Envío 0.1 BTC a Ana");
        assertThat(historialRepository.findById(historialAna.getIdHistorial()).get().getDetalle())
                .endsWith("Recibo 0.1 BTC de Luis");

        System.out.println("--- TEST GENÉRICO: Lógica de negocio completada y verificada ---");
    }
}