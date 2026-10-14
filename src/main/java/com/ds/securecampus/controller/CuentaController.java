package com.ds.securecampus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ds.securecampus.service.CuentaService;

@Controller
public class CuentaController {
    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/cuenta/contrasena")
    public String formularioPassword(Authentication authentication, Model model) {
        model.addAttribute("numeroControl", authentication.getName());
        return "cuenta/contrasena";
    }

    @PostMapping("/cuenta/contrasena")
    public String cambiarPassword(@RequestParam("passwordActual") String passwordActual,
                                  @RequestParam("passwordNueva") String passwordNueva,
                                  @RequestParam("confirmarPassword") String confirmarPassword,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            cuentaService.cambiarPassword(authentication.getName(), passwordActual,
                    passwordNueva, confirmarPassword);
            redirectAttributes.addFlashAttribute("mensaje", "Tu contraseña se actualizó correctamente.");
            return "redirect:/cuenta/contrasena?actualizada";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cuenta/contrasena?error";
        }
    }
}
