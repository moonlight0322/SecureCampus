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
        solicitudRepository.findById(idSolicitud).ifPresent(sol -> {
            sol.setEstatus(nuevoEstatus);
            sol.setRespuestaAdmin(respuestaAdmin);
            solicitudRepository.save(sol);

            auditoriaService.registrar(adminUser, "ADMINISTRADOR", "SOLICITUD_RESUELTA", 
                    "Folio: " + sol.getFolio() + " resuelta como " + nuevoEstatus, "127.0.0.1");
        });
    }
}
