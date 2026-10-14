package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private CuentaService cuentaService;

    @BeforeEach
    void setUp() {
        cuentaService = new CuentaService(usuarioRepository, passwordEncoder);
    }

    @Test
    void changesPasswordAfterVerifyingCurrentPassword() {
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("12345678");
        usuario.setPasswordHash(passwordEncoder.encode("ContrasenaActual123"));
        usuario.setDebeCambiarPass((byte) 1);
        when(usuarioRepository.findByNumeroControl("12345678")).thenReturn(Optional.of(usuario));

        cuentaService.cambiarPassword("12345678", "ContrasenaActual123",
                "ContrasenaNueva456", "ContrasenaNueva456");

        assertThat(passwordEncoder.matches("ContrasenaNueva456", usuario.getPasswordHash())).isTrue();
        assertThat(usuario.getDebeCambiarPass()).isEqualTo((byte) 0);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("12345678");
        usuario.setPasswordHash(passwordEncoder.encode("ContrasenaActual123"));
        when(usuarioRepository.findByNumeroControl("12345678")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> cuentaService.cambiarPassword("12345678", "incorrecta",
                "ContrasenaNueva456", "ContrasenaNueva456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actual");
    }
}
