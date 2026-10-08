package com.ds.securecampus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ds.securecampus.service.EstudianteService;

@Controller
public class PortalController {
    private final EstudianteService estudianteService;

    public PortalController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping({"/", "/login"})
    public String login(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? "login"
                : "redirect:/acceso";
    }

    @GetMapping("/acceso")
    public String acceso(Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ESTUDIANTE"))) {
            return "redirect:/estudiante";
        }
        return "rol-no-disponible";
    }

    @GetMapping("/estudiante")
    public String inicioEstudiante(Authentication authentication, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(authentication.getName()));
        model.addAttribute("materias", estudianteService.obtenerHorario(authentication.getName()));
        return "estudiante/inicio";
    }

    @GetMapping("/estudiante/horario")
    public String horario(Authentication authentication, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(authentication.getName()));
        model.addAttribute("materias", estudianteService.obtenerHorario(authentication.getName()));
        return "estudiante/horario";
    }

    @GetMapping("/estudiante/kardex")
    public String kardex(Authentication authentication, Model model) {
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(authentication.getName()));
        model.addAttribute("materias", estudianteService.obtenerKardex(authentication.getName()));
        return "estudiante/kardex";
    }
}
