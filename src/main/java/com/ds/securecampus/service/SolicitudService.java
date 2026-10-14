package com.ds.securecampus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.repository.SolicitudTramiteRepository;

@Service
public class SolicitudService {
    private final SolicitudTramiteRepository solicitudRepository;
    private final AuditoriaService auditoriaService;

    public SolicitudService(SolicitudTramiteRepository solicitudRepository, AuditoriaService auditoriaService) {
        this.solicitudRepository = solicitudRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<SolicitudTramite> obtenerPorUsuario(String numeroControl) {
        return solicitudRepository.findByNumeroControlOrderByFechaSolicitudDesc(numeroControl);
    }

    @Transactional(readOnly = true)
    public List<SolicitudTramite> obtenerTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    @Transactional
    public SolicitudTramite crearSolicitud(String numeroControl, String tipoTramite, String motivo, Long grupoId) {
        String folio = "SOL-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        SolicitudTramite sol = new SolicitudTramite();
        sol.setFolio(folio);
        sol.setNumeroControl(numeroControl);
        sol.setTipoTramite(tipoTramite);
        sol.setMotivo(motivo);
        sol.setGrupoId(grupoId);
        sol.setEstatus("PENDIENTE");
        sol.setFechaSolicitud(LocalDateTime.now());
        SolicitudTramite guardada = solicitudRepository.save(sol);

        auditoriaService.registrar(numeroControl, "USUARIO", "SOLICITUD_CREADA", 
                "Creación de trámite " + tipoTramite + " con Folio: " + folio, "127.0.0.1");
        return guardada;
    }

    @Transactional
    public void resolverSolicitud(Long idSolicitud, String nuevoEstatus, String respuestaAdmin, String adminUser) {
        if (!"APROBADA".equals(nuevoEstatus) && !"RECHAZADA".equals(nuevoEstatus)) {
            throw new IllegalArgumentException("Selecciona si la solicitud será aprobada o rechazada.");
        }
        if (respuestaAdmin == null || respuestaAdmin.isBlank() || respuestaAdmin.length() > 500) {
            throw new IllegalArgumentException("La respuesta debe contener entre 1 y 500 caracteres.");
        }

        SolicitudTramite solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud ya no existe."));
        if (!"PENDIENTE".equals(solicitud.getEstatus())) {
            throw new IllegalArgumentException("Esta solicitud ya fue resuelta.");
        }
        solicitud.setEstatus(nuevoEstatus);
        solicitud.setRespuestaAdmin(respuestaAdmin.trim());
        solicitudRepository.save(solicitud);
        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "SOLICITUD_RESUELTA",
                "Folio: " + solicitud.getFolio() + " resuelta como " + nuevoEstatus, "127.0.0.1");
    }
}
