package com.ds.securecampus.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ds.securecampus.service.AuditoriaService;
import com.ds.securecampus.service.LoginAttemptService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final LoginAttemptService loginAttemptService;
    private final AuditoriaService auditoriaService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService, AuditoriaService auditoriaService) {
        this.loginAttemptService = loginAttemptService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("numeroControl");
        String ip = request.getRemoteAddr();

        if (username != null && !username.isBlank()) {
            loginAttemptService.loginFailed(username);
            auditoriaService.registrar(username, "ANONIMO", "LOGIN_FALLIDO", 
                    "Intento fallido de inicio de sesión: " + exception.getMessage(), ip);
        }

        if (loginAttemptService.isBlocked(username)) {
            auditoriaService.registrar(username, "SISTEMA", "BLOQUEO_CUENTA", 
                    "Cuenta bloqueada temporalmente por 5 intentos fallidos", ip);
            getRedirectStrategy().sendRedirect(request, response, "/login?blocked=true");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
        }
    }
}
