package com.ds.securecampus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ds.securecampus.service.NotificacionService;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {
    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarNotificacion(@PathVariable("id") Long id,
                                       Authentication authentication,
                                       @RequestHeader(value = "Referer", required = false) String referer) {
        if (authentication != null) {
            notificacionService.eliminar(id, authentication.getName());
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/acceso");
    }

    @PostMapping("/{id}/marcar-leida")
    public String marcarLeida(@PathVariable("id") Long id,
                              Authentication authentication,
                              @RequestHeader(value = "Referer", required = false) String referer) {
        if (authentication != null) {
            notificacionService.marcarLeida(id, authentication.getName());
        }
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/acceso");
    }
}
