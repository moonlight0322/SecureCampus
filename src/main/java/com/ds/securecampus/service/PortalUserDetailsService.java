package com.ds.securecampus.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.UsuarioRepository;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    public PortalUserDetailsService(UsuarioRepository usuarioRepository, LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String numeroControl) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(numeroControl)) {
            int mins = loginAttemptService.getRemainingLockMinutes(numeroControl);
            throw new org.springframework.security.authentication.LockedException(
                    "Tu cuenta ha sido bloqueada temporalmente por exceso de intentos fallidos. Intenta en " + mins + " minutos.");
        }

        Usuario usuario = usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getNumeroControl())
                .password(usuario.getPasswordHash())
                .authorities("ROLE_" + usuario.getRol().getNombreRol())
                .disabled(usuario.getActivo() == null || usuario.getActivo() != 1)
                .accountLocked(loginAttemptService.isBlocked(numeroControl))
                .build();
    }
}
