package com.ds.securecampus.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.UsuarioRepository;

@Service
public class CuentaService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cambiarPassword(String numeroControl, String passwordActual,
                                String passwordNueva, String confirmarPassword) {
        Usuario usuario = usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta autenticada."));
        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (passwordNueva == null || passwordNueva.length() < 12
                || passwordNueva.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La nueva contraseña debe tener de 12 a 72 bytes.");
        }
        if (!passwordNueva.equals(confirmarPassword)) {
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña.");
        }
        if (passwordEncoder.matches(passwordNueva, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuario.setDebeCambiarPass((byte) 0);
        usuarioRepository.save(usuario);
    }
}
