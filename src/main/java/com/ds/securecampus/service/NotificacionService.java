package com.ds.securecampus.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Notificacion;
import com.ds.securecampus.repository.NotificacionRepository;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificaciones(String numeroControl) {
        return notificacionRepository.findByNumeroControlOrderByFechaCreacionDesc(numeroControl);
    }

    @Transactional
    public void enviar(String numeroControl, String titulo, String mensaje) {
        Notificacion notif = new Notificacion();
        notif.setNumeroControl(numeroControl);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setFechaCreacion(LocalDateTime.now());
        notif.setLeida(0);
        notificacionRepository.save(notif);
    }
}
