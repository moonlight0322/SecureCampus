package com.ds.securecampus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.UsuarioRepository;
import com.ds.securecampus.service.PoliticaContrasena;

@Controller
public class CuentaController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cuenta/contrasena")
    public String cambiarContrasena() {
        return "cuenta/contrasena";
    }

    @PostMapping("/cuenta/contrasena")
    public String guardarContrasena(
            Authentication authentication,
            @RequestParam String contrasenaActual,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            Model model) {
        Usuario usuario = usuarioRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "La cuenta autenticada no existe en el registro de usuarios."));

        if (!passwordEncoder.matches(contrasenaActual, usuario.getPasswordHash())) {
            model.addAttribute("error", "La contraseña actual no coincide con la cuenta iniciada.");
            return "cuenta/contrasena";
        }
        if (!PoliticaContrasena.esValida(nuevaContrasena)) {
            model.addAttribute("error", PoliticaContrasena.requisitos());
            return "cuenta/contrasena";
        }
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            model.addAttribute("error", "La confirmación no coincide con la nueva contraseña.");
            return "cuenta/contrasena";
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        usuario.setDebeCambiarPass((byte) 0);
        usuarioRepository.save(usuario);
        model.addAttribute("exito", "La contraseña de esta cuenta se actualizó correctamente.");
        return "cuenta/contrasena";
    }
}
