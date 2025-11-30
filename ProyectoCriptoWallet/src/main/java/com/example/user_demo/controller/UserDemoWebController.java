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

    // 1. Declaramos el repositorio aquí
    private final CriptomonedaRepository criptoRepo; // <--- NUEVO

    @Autowired
    public UserDemoWebController(UsuarioService usuarioService,
                                 CarteraService carteraService,
                                 TransaccionService transaccionService,
                                 CriptomonedaRepository criptoRepo) {
        this.usuarioService = usuarioService;
        this.carteraService = carteraService;
        this.transaccionService = transaccionService;
        this.criptoRepo = criptoRepo; // <--- ASIGNAMOS AQUÍ
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

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String nombre, @RequestParam String email, RedirectAttributes ra) {
        try {
            usuarioService.crearUsuario(nombre, email, "Cuenta creada el " + java.time.LocalDate.now());
            ra.addFlashAttribute("mensaje", "Usuario registrado con éxito.");
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
            ra.addFlashAttribute("mensaje", "Usuario eliminado.");
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

        // Usamos el repositorio nuevo para el desplegable del dashboard
        model.addAttribute("todasLasCriptos", criptoRepo.findAll());

        return "dashboard";
    }

    @PostMapping("/carteras/crear")
    public String crearCartera(@RequestParam Long usuarioId, @RequestParam Double balanceInicial, RedirectAttributes ra) {
        Usuario u = usuarioService.getUsuarioById(usuarioId).orElseThrow();
        carteraService.crearCartera(u.getEmail(), balanceInicial);
        ra.addFlashAttribute("mensaje", "Nueva cartera creada.");
        ra.addFlashAttribute("tipo", "success");
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

    // --- MÉTODO ACTUALIZADO ---
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
                                        @RequestParam Long carteraOrigenId, // <--- 1. AÑADE ESTE PARÁMETRO
                                        @RequestParam String cripto,
                                        @RequestParam Double cantidad,
                                        RedirectAttributes ra) {
        try {
            // 2. PASA EL PARÁMETRO AL SERVICIO (Ahora son 5 argumentos)
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
                           @RequestParam Double cantidadInversion, // Dinero a gastar
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
}