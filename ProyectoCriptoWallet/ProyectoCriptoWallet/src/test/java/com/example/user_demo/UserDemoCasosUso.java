package com.example.user_demo;

import com.example.user_demo.data.model.*;
import com.example.user_demo.data.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Suite de tests completa que valida todos los casos de uso de la CriptoWallet.
 * Cada caso de uso está separado en un método @Test individual.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql("/data.sql") // Carga data.sql antes de cada test
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Limpia la BD después de cada test
public class UserDemoCasosUso {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CarteraRepository carteraRepository;
    @Autowired private CriptomonedaRepository criptomonedaRepository;
    @Autowired private HistorialRepository historialRepository;
    @Autowired private TransaccionRepository transaccionRepository;

    // --- Casos de Uso: USUARIO ---

    @Test
    @Order(1)
    @DisplayName("CU Usuario 1: Crear nuevo Usuario (y su Historial en cascada)")
    @Transactional
    public void testCrearUsuarioYHistorial() {
        long userCount = usuarioRepository.count(); // 3
        long histCount = historialRepository.count(); // 3

        Usuario david = new Usuario("David Roca", "david@email.com");
        Historial hDavid = new Historial( david,"Historial de David");
        david.setHistorial(hDavid); // Enlace 1:1

        usuarioRepository.save(david);

        assertThat(usuarioRepository.count()).isEqualTo(userCount + 1);
        assertThat(historialRepository.count()).isEqualTo(histCount + 1);
        Usuario userLeido = usuarioRepository.findByEmail("david@email.com").get();
        assertThat(userLeido.getHistorial().getDetalle()).isEqualTo("Historial de David");
    }

    @Test
    @Order(2)
    @DisplayName("CU Usuario 2: Encontrar Usuario por Email")
    void testFindUsuarioByEmail() {
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();
        assertThat(ana).isNotNull();
        assertThat(ana.getNombre()).isEqualTo("Ana López");
    }

''
    @Test
    @Order(3)
    @DisplayName("CU Usuario 3: Encontrar Usuarios por prefijo de nombre")
    void testFindUsuariosPorNombre() {
        Iterable<Usuario> anas = usuarioRepository.findByNombreStartingWith("Ana");
        assertThat(anas).hasSize(1);
    }

    @Test
    @Order(4)
    @DisplayName("CU Usuario 4: [Query] Encontrar Usuarios con múltiples carteras")
    void testFindUsuariosConMultiplesCarteras() {
        // En data.sql, solo Ana (ID 1) tiene 2 carteras (ID 1 y 4)
        Iterable<Usuario> usuarios = usuarioRepository.findUsuariosConMultiplesCarteras();
        assertThat(usuarios)
                .hasSize(1)
                .extracting(Usuario::getNombre)
                .contains("Ana López");
    }

    @Test
    @Order(5)
    @DisplayName("CU Usuario 5: [Query] Encontrar Usuarios que poseen una Cripto")
    void testFindUsuariosPorCriptoSimbolo() {
        // En data.sql, todos poseen BTC excepto Luis
        Iterable<Usuario> usuariosConBTC = usuarioRepository.findUsuariosByCriptoSimbolo("BTC");
        assertThat(usuariosConBTC)
                .hasSize(2)
                .extracting(Usuario::getEmail)
                .contains("ana@email.com", "carla@email.com");

        // Todos poseen ETH excepto Carla
        Iterable<Usuario> usuariosConETH = usuarioRepository.findUsuariosByCriptoSimbolo("ETH");
        assertThat(usuariosConETH)
                .hasSize(2)
                .extracting(Usuario::getEmail)
                .contains("ana@email.com", "luis@email.com");
    }


    // --- Casos de Uso: CARTERA ---
    @Test
    @Order(6)
    @DisplayName("CU Cartera 1: Crear Cartera para Usuario existente")
    void testCrearCartera() {
        long carteraCount = carteraRepository.count(); // 4
        Usuario luis = usuarioRepository.findByEmail("luis@email.com").get();

        // Luis crea una segunda cartera
        Cartera nuevaCartera = new Cartera(luis,999.0);
        carteraRepository.save(nuevaCartera);

        assertThat(carteraRepository.count()).isEqualTo(carteraCount + 1);
        assertThat(carteraRepository.findByUsuario(luis)).hasSize(2);
    }

    @Test
    @Order(7)
    @DisplayName("CU Cartera 2: Encontrar Carteras por Usuario")
    void testFindByUsuario() {
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();
        List<Cartera> carterasAna = carteraRepository.findByUsuario(ana);
        assertThat(carterasAna).hasSize(2); // Carteras 1 y 4
    }

    @Test
    @Order(8)
    @DisplayName("CU Cartera 3: [Query] Calcular Balance Total de un Usuario")
    void testGetBalanceTotalPorUsuario() {
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();
        // Cartera 1 (1500) + Cartera 4 (100)
        Double balanceAna = carteraRepository.getBalanceTotalPorUsuario(ana);
        assertThat(balanceAna).isEqualTo(1600.0);
    }

    @Test
    @Order(9)
    @DisplayName("CU Cartera 4: [Relación N:M] Añadir Criptomoneda a Cartera")
    @Transactional // Para cargar .getCriptomonedas() LAZY
    void testAddCriptoACartera() {
        // Cartera 2 (de Luis) solo tiene ETH
        Cartera carteraLuis = carteraRepository.findById(2L).get();
        Criptomoneda btc = criptomonedaRepository.findBySimbolo("BTC").get();

        Set<Criptomoneda> criptos = carteraLuis.getCriptomonedas();
        assertThat(criptos).hasSize(1);

        // Acción: Luis "compra" BTC
        criptos.add(btc);
        carteraRepository.save(carteraLuis);

        // Verificación
        Cartera carteraActualizada = carteraRepository.findById(2L).get();
        assertThat(carteraActualizada.getCriptomonedas()).hasSize(2).contains(btc);
    }

    @Test
    @Order(10)
    @DisplayName("CU Cartera 5: [Relación N:M] Quitar Criptomoneda de Cartera")
    @Transactional // Para cargar .getCriptomonedas() LAZY
    void testRemoveCriptoDeCartera() {
        // Cartera 1 (de Ana) tiene BTC y ETH
        Cartera carteraAna = carteraRepository.findById(1L).get();
        Criptomoneda eth = criptomonedaRepository.findBySimbolo("ETH").get();

        Set<Criptomoneda> criptos = carteraAna.getCriptomonedas();
        assertThat(criptos).hasSize(2);

        // Acción: Ana "vende" ETH (Funciona gracias a equals/hashCode)
        criptos.remove(eth);
        carteraRepository.save(carteraAna);

        // Verificación
        Cartera carteraActualizada = carteraRepository.findById(1L).get();
        assertThat(carteraActualizada.getCriptomonedas()).hasSize(1).doesNotContain(eth);
    }

    // --- Casos de Uso: CRIPTOMONEDA ---

    @Test
    @Order(11)
    @DisplayName("CU Criptomoneda 1: Crear nueva Criptomoneda")
    void testCrearCriptomoneda() {
        long criptoCount = criptomonedaRepository.count(); // 3
        Criptomoneda doge = new Criptomoneda("Dogecoin", "DOGE");
        criptomonedaRepository.save(doge);

        assertThat(criptomonedaRepository.count()).isEqualTo(criptoCount + 1);
        assertThat(criptomonedaRepository.findBySimbolo("DOGE")).isPresent();
    }

    @Test
    @Order(12)
    @DisplayName("CU Criptomoneda 2: Encontrar Criptomoneda por Símbolo")
    void testFindBySimbolo() {
        Criptomoneda btc = criptomonedaRepository.findBySimbolo("BTC").get();
        assertThat(btc).isNotNull();
        assertThat(btc.getNombre()).isEqualTo("Bitcoin");
    }

    @Test
    @Order(13)
    @DisplayName("CU Criptomoneda 3: [Query] Encontrar Criptomonedas de una Cartera")
    void testFindCriptomonedasByCarteraId() {
        // Cartera 3 (de Carla) tiene BTC y SOL
        List<Criptomoneda> criptos = criptomonedaRepository.findCriptomonedasByCarterasId(3L);
        assertThat(criptos).hasSize(2)
                .extracting(Criptomoneda::getSimbolo)
                .containsExactlyInAnyOrder("BTC", "SOL");
    }

    @Test
    @Order(14)
    @DisplayName("CU Criptomoneda 4: Borrar Criptomoneda (Fallo por relación)")
    void testDeleteCriptomoneda_FallaSiHayRelacion() {
        // BTC (ID 1) está en uso por Cartera 1 y 3.
        Criptomoneda btc = criptomonedaRepository.findBySimbolo("BTC").get();

        // Intentar borrarla debe lanzar una excepción de integridad
        assertThrows(DataIntegrityViolationException.class, () -> {
            criptomonedaRepository.delete(btc);
        });
    }

    @Test
    @Order(15)
    @DisplayName("CU Criptomoneda 5: Borrar Criptomoneda (Éxito)")
    @Transactional
    void testDeleteCriptomoneda_Exito() {
        // 1. Creamos una Cripto nueva
        Criptomoneda ada = criptomonedaRepository.save(new Criptomoneda("Cardano", "ADA"));
        long criptoCount = criptomonedaRepository.count(); // 4

        // 2. La asignamos a una cartera
        Cartera carteraLuis = carteraRepository.findById(2L).get();
        carteraLuis.addCriptomoneda(ada);
        carteraRepository.save(carteraLuis);

        // 3. Rompemos la relación N:M
        carteraLuis.removeCriptomoneda(ada);
        carteraRepository.save(carteraLuis);

        // 4. Borramos la Cripto (ahora es seguro)
        criptomonedaRepository.delete(ada);

        assertThat(criptomonedaRepository.count()).isEqualTo(criptoCount - 1); // Vuelve a 3
        assertThat(criptomonedaRepository.findBySimbolo("ADA")).isEmpty();
    }

    // --- Casos de Uso: TRANSACCION ---

    @Test
    @Order(16)
    @DisplayName("CU Transaccion 1: Crear nueva Transacción")
    void testCrearTransaccion() {
        long txCount = transaccionRepository.count(); // 3
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();
        Usuario luis = usuarioRepository.findByEmail("luis@email.com").get();
        Criptomoneda sol = criptomonedaRepository.findBySimbolo("SOL").get();

        Transaccion tx = new Transaccion(ana, luis, sol,5.0);
        transaccionRepository.save(tx);

        assertThat(transaccionRepository.count()).isEqualTo(txCount + 1);
    }

    @Test
    @Order(17)
    @DisplayName("CU Transaccion 2: [Query] Encontrar Transacciones (enviadas o recibidas)")
    void testFindTransaccionesPorUsuario() {
        Usuario luis = usuarioRepository.findByEmail("luis@email.com").get();
        // TX 1 (recibida) y TX 3 (enviada)
        Iterable<Transaccion> txsLuis = transaccionRepository.findAllTransaccionesByUsuario(luis);
        assertThat(txsLuis).hasSize(2);
    }

    @Test
    @Order(18)
    @DisplayName("CU Transaccion 3: [Query] Encontrar Transacciones por rango de fechas")
    void testFindTransaccionesPorRangoDeFechas() {
        // data.sql tiene 3 txs: 2025-10-28, 2025-10-29, 2025-10-30
        LocalDateTime inicio = LocalDateTime.of(2025, 10, 29, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2025, 10, 31, 0, 0);

        // Debería encontrar TX 2 y TX 3
        Iterable<Transaccion> txs = transaccionRepository.findTransaccionesEnRangoDeFechas(inicio, fin);
        assertThat(txs).hasSize(2);
    }

    @Test
    @Order(19)
    @DisplayName("CU Transaccion 4: [Query] Encontrar Transacciones internas")
    void testFindTransaccionesInternas() {
        // 1. Crear una TX interna
        Usuario ana = usuarioRepository.findByEmail("ana@email.com").get();
        Criptomoneda btc = criptomonedaRepository.findBySimbolo("BTC").get();
        transaccionRepository.save(new Transaccion( ana, ana, btc,0.1));

        // 2. Probar la consulta
        Iterable<Transaccion> txs = transaccionRepository.findTransaccionesInternas();
        assertThat(txs).hasSize(1)
                .extracting(Transaccion::getUsuarioOrigen)
                .contains(ana);
    }

    // --- Casos de Uso: HISTORIAL ---

    @Test
    @Order(20)
    @DisplayName("CU Historial 1: Encontrar Historial por Usuario")
    void testFindByUsuario_Historial() {
        Usuario carla = usuarioRepository.findByEmail("carla@email.com").get();
        Historial hCarla = historialRepository.findByUsuario(carla).get();

        assertThat(hCarla.getDetalle()).isEqualTo("Historial de Carla Diaz");
    }

    @Test
    @Order(21)
    @DisplayName("CU Historial 2: Actualizar detalle de Historial")
    void testActualizarDetalleHistorial() {
        Historial hAna = historialRepository.findById(1L).get();
        String detalleOriginal = hAna.getDetalle();

        hAna.setDetalle(detalleOriginal + "\nNUEVA ENTRADA");
        historialRepository.save(hAna);

        Historial hActualizado = historialRepository.findById(1L).get();
        assertThat(hActualizado.getDetalle()).endsWith("NUEVA ENTRADA");
    }

    @Test
    @Order(22)
    @DisplayName("CU Historial 3: [Query] Contar Historiales de usuarios activos")
    void testCountHistorialesConTransacciones() {
        // En data.sql, los 3 usuarios (Ana, Luis, Carla) participan en transacciones.
        Long count = historialRepository.countHistorialesConTransacciones();
        assertThat(count).isEqualTo(3);
    }

    // --- Caso de Uso: BORRADO EN CASCADA (El más importante) ---

    @Test
    @Order(23)
    @DisplayName("CU Borrado: Eliminar Usuario y verificar cascada completa")
    @Transactional // Requerido para que delete() cargue las relaciones a borrar en cascada
    void testEliminarUsuarioEnCascada() {
        System.out.println("--- TEST BORRADO: Eliminando a 'Luis' (Usuario 2) ---");

        // 1. Obtener IDs de las entidades de Luis (de data.sql)
        long luisId = 2L;
        long historialLuisId = 2L;
        long carteraLuisId = 2L;
        long txOrigenLuisId = 3L;  // TX 3 (Luis -> Carla)
        long txDestinoLuisId = 1L; // TX 1 (Ana -> Luis)

        // 2. Verificar que todo existe
        assertThat(usuarioRepository.findById(luisId)).isPresent();
        assertThat(historialRepository.findById(historialLuisId)).isPresent();
        assertThat(carteraRepository.findById(carteraLuisId)).isPresent();
        assertThat(transaccionRepository.findById(txOrigenLuisId)).isPresent();
        assertThat(transaccionRepository.findById(txDestinoLuisId)).isPresent();

        // 3. Acción: Borrar a Luis
        // Gracias a CascadeType.ALL y orphanRemoval=true en la entidad Usuario,
        // todo lo de Luis debe ser borrado.
        usuarioRepository.deleteById(luisId);

        // 4. Verificación (Todo lo de Luis debe haber desaparecido)
        assertThat(usuarioRepository.findById(luisId)).isEmpty();
        assertThat(historialRepository.findById(historialLuisId)).isEmpty();
        assertThat(carteraRepository.findById(carteraLuisId)).isEmpty();
        assertThat(transaccionRepository.findById(txOrigenLuisId)).isEmpty();
        assertThat(transaccionRepository.findById(txDestinoLuisId)).isEmpty();

        // 5. Verificación (Entidades de otros usuarios deben seguir existiendo)
        assertThat(usuarioRepository.count()).isEqualTo(2); // Quedan Ana y Carla
        assertThat(carteraRepository.findById(1L)).isPresent(); // Cartera 1 (Ana)
        assertThat(carteraRepository.findById(3L)).isPresent(); // Cartera 3 (Carla)
        assertThat(transaccionRepository.findById(2L)).isPresent(); // TX 2 (Ana -> Carla)

        System.out.println("--- TEST BORRADO: Cascada verificada ---");
    }
}