package com.example.user_demo.controller;

import com.example.user_demo.data.model.Usuario;
import com.example.user_demo.data.repository.CriptomonedaRepository;
import com.example.user_demo.data.services.CarteraService;
import com.example.user_demo.data.services.TransaccionService;
import com.example.user_demo.data.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class UserDemoWebController {

    private final UsuarioService usuarioService;
    private final CarteraService carteraService;
    private final TransaccionService transaccionService;
    private final CriptomonedaRepository criptoRepo;

    @Autowired
    public UserDemoWebController(UsuarioService usuarioService,
                                 CarteraService carteraService,
                                 TransaccionService transaccionService,
                                 CriptomonedaRepository criptoRepo) {
        this.usuarioService = usuarioService;
        this.carteraService = carteraService;
        this.transaccionService = transaccionService;
        this.criptoRepo = criptoRepo;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("listaUsuarios", usuarioService.getAllUsuarios());
        return "index";
    }

    @GetMapping("/usuarios/nuevo")
    public String formNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form-usuario";
    }

    // --- MODIFICADO: AÑADIDO PARÁMETRO SALDO INICIAL ---
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String nombre,
                                 @RequestParam String email,
                                 @RequestParam(defaultValue = "0.0") Double saldoInicial, // Nuevo parámetro
                                 RedirectAttributes ra) {
        try {
            // 1. Crear el usuario
            Usuario nuevoUsuario = usuarioService.crearUsuario(nombre, email, "Cuenta creada el " + java.time.LocalDate.now());

            // 2. Crear automáticamente su primera cartera con el saldo indicado
            carteraService.crearCartera(nuevoUsuario.getEmail(), saldoInicial);

            ra.addFlashAttribute("mensaje", "Usuario y cartera registrados con éxito.");
            ra.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
            return "redirect:/usuarios/nuevo";
        }
        return "redirect:/";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes ra) {
        try {
            usuarioService.eliminarUsuario(id);
            ra.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
            ra.addFlashAttribute("tipo", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/";
    }

    @GetMapping("/usuario/{id}")
    public String dashboard(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioService.getUsuarioById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Double balance = carteraService.getBalanceTotalPorUsuario(usuario.getEmail());

        model.addAttribute("usuario", usuario);
        model.addAttribute("balanceTotal", balance);
        model.addAttribute("todasLasCriptos", criptoRepo.findAll());

        return "dashboard";
    }

    @PostMapping("/carteras/crear")
    public String crearCartera(@RequestParam Long usuarioId, @RequestParam Double balanceInicial, RedirectAttributes ra) {
        try {
            Usuario u = usuarioService.getUsuarioById(usuarioId).orElseThrow();
            carteraService.crearCartera(u.getEmail(), balanceInicial);
            ra.addFlashAttribute("mensaje", "Nueva cartera creada.");
            ra.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + usuarioId;
    }

    @PostMapping("/carteras/add-cripto")
    public String addCripto(@RequestParam Long usuarioId, @RequestParam Long carteraId, @RequestParam Long criptoId, RedirectAttributes ra) {
        try {
            carteraService.addCriptomonedaACartera(carteraId, criptoId);
            ra.addFlashAttribute("mensaje", "Criptomoneda añadida.");
            ra.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + usuarioId;
    }

    @PostMapping("/carteras/remove-cripto")
    public String removeCripto(@RequestParam Long usuarioId, @RequestParam Long carteraId, @RequestParam Long criptoId, RedirectAttributes ra) {
        try {
            carteraService.removeCriptomonedaDeCartera(carteraId, criptoId);
            ra.addFlashAttribute("mensaje", "Criptomoneda eliminada.");
            ra.addFlashAttribute("tipo", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + usuarioId;
    }

    @GetMapping("/transferencia/{idOrigen}")
    public String formTransferencia(@PathVariable("idOrigen") Long idOrigen, Model model) {
        Usuario origen = usuarioService.getUsuarioById(idOrigen).orElseThrow();
        model.addAttribute("usuarioOrigen", origen);
        model.addAttribute("listaUsuarios", usuarioService.getAllUsuarios());
        model.addAttribute("listaCriptos", criptoRepo.findAll());
        model.addAttribute("misCarteras", origen.getCarteras());
        return "transferencia";
    }

    @PostMapping("/transferencia/ejecutar")
    public String ejecutarTransferencia(@RequestParam Long origenId,
                                        @RequestParam Long destinoId,
                                        @RequestParam Long carteraOrigenId,
                                        @RequestParam String cripto,
                                        @RequestParam Double cantidad,
                                        RedirectAttributes ra) {
        try {
            transaccionService.realizarTransferencia(origenId, destinoId, carteraOrigenId, cripto, cantidad);
            ra.addFlashAttribute("mensaje", "Transferencia realizada con éxito.");
            ra.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + origenId;
    }

    @PostMapping("/carteras/invertir")
    public String invertir(@RequestParam Long usuarioId,
                           @RequestParam Long carteraId,
                           @RequestParam Long criptoId,
                           @RequestParam Double cantidadInversion,
                           RedirectAttributes ra) {
        try {
            carteraService.invertirEnCripto(carteraId, criptoId, cantidadInversion);
            ra.addFlashAttribute("mensaje", "Inversión realizada con éxito.");
            ra.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al invertir: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + usuarioId;
    }

    @PostMapping("/carteras/eliminar")
    public String eliminarCartera(@RequestParam Long carteraId, @RequestParam Long usuarioId, RedirectAttributes ra) {
        try {
            carteraService.eliminarCartera(carteraId); // Asegúrate de implementar este método en el servicio
            ra.addFlashAttribute("mensaje", "Cartera eliminada correctamente.");
            ra.addFlashAttribute("tipo", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al eliminar la cartera: " + e.getMessage());
            ra.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/usuario/" + usuarioId;
    }
}