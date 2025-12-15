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
import java.util.Arrays; // Importar Arrays

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TEST CRUD (CICLO DE VIDA)
 * Este test prueba el ciclo completo de Creación, Lectura, Actualización y Borrado,
 * construyendo sobre los datos existentes de data.sql.
 */
@SpringBootTest
@Sql("/data.sql") // Carga data.sql antes
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Limpia al final
 public class UserDemoCRUDTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CarteraRepository carteraRepository;
    @Autowired private CriptomonedaRepository criptomonedaRepository;
    @Autowired private HistorialRepository historialRepository;
    @Autowired private TransaccionRepository transaccionRepository;

    @Test
    @DisplayName("Test CRUD: Ciclo de vida completo de un Usuario y sus relaciones")
    @Transactional // Requerido para manejar las cargas LAZY
    public void testCicloDeVidaUsuario() {
        System.out.println("--- TEST CRUD: Iniciando ciclo de vida ---");

        // 0. VERIFICAR ESTADO INICIAL (Cargado desde data.sql)
        long userCountInicial = usuarioRepository.count();
        long criptoCountInicial = criptomonedaRepository.count();
        assertThat(userCountInicial).isEqualTo(3); // Ana, Luis, Carla
        assertThat(criptoCountInicial).isEqualTo(3); // BTC, ETH, SOL

        // 1. CREATE (Crear entidades base)
        System.out.println("CREATE: Creando entidades (DOGE, Usuario 'David')...");

        // Crear Cripto (usando criptomonedaRepository)
        Criptomoneda doge = criptomonedaRepository.save(new Criptomoneda("Dogecoin", "DOGE"));

        // Usar un usuario existente (Ana) para interactuar
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();

        // 2. CREATE (Usuario principal y todas sus relaciones)
        System.out.println("CREATE: Creando a 'David' y sus entidades relacionadas...");

        // Crear Usuario
        Usuario david = new Usuario("David Roca", "david@email.com");

        // Crear Historial (1:1)
        Historial hDavid = new Historial( david,"Historial de David");
        david.setHistorial(hDavid); // Enlace bidireccional

        // Crear Cartera 1 (1:N)
        Cartera cDavid1 = new Cartera( david,1000.0);
        cDavid1.addCriptomoneda(doge); // Relación N:M con la nueva cripto

        david.addCartera(cDavid1); // Enlace bidireccional

        // Guardar a David (debe guardar Historial y Carteras en cascada)
        usuarioRepository.save(david); // (usando usuarioRepository, carteraRepository en cascada)

        // Crear Transacciones (requieren que 'david' tenga ID)
        // (A) David envía 0.1 DOGE a Ana
        Transaccion tx1 = new Transaccion(david, ana, doge,0.1);
        // (B) Ana envía 0.5 ETH a David
        Criptomoneda eth = criptomonedaRepository.findBySimbolo("ETH").get();
        Transaccion tx2 = new Transaccion(ana, david, eth,0.5);

        transaccionRepository.saveAll(Arrays.asList(tx1, tx2)); // (usando transaccionRepository)

        // --- INICIO DE LA CORRECCIÓN ---
        // Sincronizamos los objetos Java con la BD.
        // Añadimos las transacciones a las listas en memoria de los usuarios.
        david.getTransaccionesEnviadas().add(tx1);
        ana.getTransaccionesRecibidas().add(tx1);

        ana.getTransaccionesEnviadas().add(tx2);
        david.getTransaccionesRecibidas().add(tx2);
        // --- FIN DE LA CORRECCIÓN ---

        // 3. READ (Verificar todo)
        System.out.println("READ: Verificando a 'David'...");
        Usuario davidLeido = usuarioRepository.findByEmail("david@email.com").get();

        assertThat(davidLeido.getNombre()).isEqualTo("David Roca");
        assertThat(davidLeido.getHistorial().getDetalle()).isEqualTo("Historial de David");
        assertThat(davidLeido.getCarteras()).hasSize(1);
        assertThat(davidLeido.getCarteras().get(0).getCriptomonedas()).contains(doge);

        // Verificamos que los contadores han subido
        assertThat(usuarioRepository.count()).isEqualTo(userCountInicial + 1); // 3 + David
        assertThat(criptomonedaRepository.count()).isEqualTo(criptoCountInicial + 1); // 3 + DOGE
        assertThat(transaccionRepository.count()).isEqualTo(5); // 3 (de data.sql) + 2 nuevas

        // 4. UPDATE
        System.out.println("UPDATE: Actualizando a 'David'...");
        davidLeido.setNombre("David Roca Actualizado");
        usuarioRepository.save(davidLeido);

        // Verificar
        assertThat(usuarioRepository.findById(davidLeido.getIdUsuario()).get().getNombre())
                .isEqualTo("David Roca Actualizado");

        // 5. DELETE (Borrar a David y verificar TODAS las cascadas)
        System.out.println("DELETE: Borrando a 'David'...");

        // Guardar los IDs de las entidades que deben borrarse
        // Estas líneas AHORA funcionarán gracias a la corrección
        long idHistorial = davidLeido.getHistorial().getIdHistorial();
        long idCartera1 = davidLeido.getCarteras().get(0).getIdCartera();
        long idTxOrigen = davidLeido.getTransaccionesEnviadas().get(0).getIdTransaccion();
        long idTxDestino = davidLeido.getTransaccionesRecibidas().get(0).getIdTransaccion();

        // Acción: Borrar a David
        usuarioRepository.delete(davidLeido);

        // Verificar que David y TODO lo suyo ha desaparecido
        assertThat(usuarioRepository.findByEmail("david@email.com")).isEmpty();
        assertThat(historialRepository.findById(idHistorial)).isEmpty();
        assertThat(carteraRepository.findById(idCartera1)).isEmpty();
        assertThat(transaccionRepository.findById(idTxOrigen)).isEmpty();
        assertThat(transaccionRepository.findById(idTxDestino)).isEmpty();

        // Verificar que las entidades NO relacionadas (Ana, Luis, Carla, BTC, ETH, DOGE) siguen ahí
        assertThat(usuarioRepository.count()).isEqualTo(userCountInicial); // Vuelve a 3
        assertThat(criptomonedaRepository.count()).isEqualTo(criptoCountInicial + 1); // DOGE se queda
        assertThat(criptomonedaRepository.findBySimbolo("DOGE")).isPresent();
        assertThat(usuarioRepository.findByEmail("ana@email.com")).isPresent();

        System.out.println("--- TEST CRUD: Ciclo de vida y cascadas verificadas ---");
    }
}