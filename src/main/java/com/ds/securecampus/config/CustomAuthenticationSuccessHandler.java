package com.ds.securecampus.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ds.securecampus.service.AuditoriaService;
import com.ds.securecampus.service.LoginAttemptService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final LoginAttemptService loginAttemptService;
    private final AuditoriaService auditoriaService;

    public CustomAuthenticationSuccessHandler(LoginAttemptService loginAttemptService, AuditoriaService auditoriaService) {
        this.loginAttemptService = loginAttemptService;
        this.auditoriaService = auditoriaService;
        setDefaultTargetUrl("/acceso");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        loginAttemptService.loginSucceeded(username);
        String roles = authentication.getAuthorities().toString();
        auditoriaService.registrar(username, roles, "LOGIN_EXITOSO", 
                "Inicio de sesión exitoso en el portal", request.getRemoteAddr());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
