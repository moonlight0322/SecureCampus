package com.ds.securecampus.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.repository.LogAuditoriaRepository;

@Service
public class AuditoriaService {
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final TransactionTemplate auditTransaction;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository,
                            PlatformTransactionManager transactionManager) {
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.auditTransaction = new TransactionTemplate(transactionManager);
        this.auditTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Registra un evento de auditoría en una transacción independiente (REQUIRES_NEW)
     * para que un fallo de audit nunca revierta la transacción del llamador.
     * El campo ROL se trunca a 30 caracteres para ajustarse a la columna.
     */
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
            auditTransaction.executeWithoutResult(status -> logAuditoriaRepository.save(log));
        } catch (RuntimeException e) {
            logger.error("No se pudo guardar el log de auditoría.", e);
        }
    }
}
