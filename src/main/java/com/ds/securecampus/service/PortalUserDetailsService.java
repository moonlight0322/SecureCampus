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

    public PortalUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String numeroControl) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(numeroControl)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities("ROLE_" + usuario.getRol().getNombreRol())
                .disabled(usuario.getActivo() == null || usuario.getActivo() != 1)
                .build();
    }
}
