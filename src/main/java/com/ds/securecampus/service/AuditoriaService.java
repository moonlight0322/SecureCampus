package com.ds.securecampus.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.repository.LogAuditoriaRepository;

@Service
public class AuditoriaService {
    private final LogAuditoriaRepository logAuditoriaRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    /**
     * Registra un evento de auditoría en una transacción independiente (REQUIRES_NEW)
     * para que un fallo de audit nunca revierta la transacción del llamador.
     * El campo ROL se trunca a 30 caracteres (límite de la columna Oracle).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String numeroControl, String rol, String operacion, String detalles, String ip) {
        try {
            String rolTruncado = (rol != null && rol.length() > 30) ? rol.substring(0, 30) : rol;
            LogAuditoria log = new LogAuditoria();
            log.setFechaHora(LocalDateTime.now());
            log.setNumeroControl(numeroControl);
            log.setRol(rolTruncado);
            log.setOperacion(operacion);
            log.setDetalles(detalles != null && detalles.length() > 500 ? detalles.substring(0, 500) : detalles);
            log.setIpOrigen(ip);
            logAuditoriaRepository.save(log);
        } catch (Exception e) {
            // No romper el flujo principal si el log falla
            System.err.println("Error al guardar log de auditoría: " + e.getMessage());
        }
    }
}
