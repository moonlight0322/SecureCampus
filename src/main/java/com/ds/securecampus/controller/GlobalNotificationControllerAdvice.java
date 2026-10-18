package com.ds.securecampus.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ds.securecampus.model.Notificacion;
import com.ds.securecampus.service.NotificacionService;

@ControllerAdvice
public class GlobalNotificationControllerAdvice {
    private final NotificacionService notificacionService;

    public GlobalNotificationControllerAdvice(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute
    public void agregarNotificaciones(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String numControl = authentication.getName();
            List<Notificacion> notificaciones = notificacionService.obtenerNotificaciones(numControl);
            long noLeidas = notificaciones.stream().filter(n -> n.getLeida() == 0).count();
            model.addAttribute("notificacionesGlobales", notificaciones);
            model.addAttribute("totalNotificacionesNoLeidas", noLeidas);
        }
    }
}
