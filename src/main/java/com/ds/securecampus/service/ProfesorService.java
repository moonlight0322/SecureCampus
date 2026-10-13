package com.ds.securecampus.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.PeriodoRepository;

@Service
public class ProfesorService {
    private final GrupoRepository grupoRepository;
    private final CargaAlumnoRepository cargaAlumnoRepository;
    private final PeriodoRepository periodoRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public ProfesorService(GrupoRepository grupoRepository,
                           CargaAlumnoRepository cargaAlumnoRepository,
                           PeriodoRepository periodoRepository,
                           AuditoriaService auditoriaService,
                           NotificacionService notificacionService) {
        this.grupoRepository = grupoRepository;
        this.cargaAlumnoRepository = cargaAlumnoRepository;
        this.periodoRepository = periodoRepository;
        this.auditoriaService = auditoriaService;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<Grupo> obtenerGruposProfesor(String numeroControl) {
        return grupoRepository.findByProfesorNumeroControl(numeroControl);
    }

    @Transactional(readOnly = true)
    public Grupo obtenerGrupo(Long grupoId) {
        return grupoRepository.findById(grupoId).orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<CargaAlumno> obtenerAlumnosGrupo(Long grupoId) {
        return cargaAlumnoRepository.findByGrupoGrupoId(grupoId);
    }

    @Transactional(readOnly = true)
    public boolean puedeCapturar(Grupo grupo) {
        if (grupo == null || grupo.getPeriodo() == null) return false;
        Periodo per = grupo.getPeriodo();
        // Solo permite edición si captura_activa es 'S'
        return "S".equalsIgnoreCase(per.getCapturaActiva());
    }

    @Transactional
    public void guardarCalificacion(Long cargaId, BigDecimal p1, BigDecimal p2, String profesorControl) {
        CargaAlumno carga = cargaAlumnoRepository.findById(cargaId)
                .orElseThrow(() -> new IllegalArgumentException("Carga no encontrada"));

        Grupo grupo = carga.getGrupo();
        if (!puedeCapturar(grupo)) {
            throw new IllegalStateException("El periodo de captura se encuentra cerrado para este grupo. Solicite desbloqueo al administrador.");
        }

        // Validación de rango 0 a 100
        if (p1 != null && (p1.compareTo(BigDecimal.ZERO) < 0 || p1.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("La calificación parcial 1 debe estar entre 0 y 100.");
        }
        if (p2 != null && (p2.compareTo(BigDecimal.ZERO) < 0 || p2.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("La calificación parcial 2 debe estar entre 0 y 100.");
        }

        carga.setCalificacionParcial1(p1);
        carga.setCalificacionParcial2(p2);

        // Promedio automático
        if (p1 != null && p2 != null) {
            BigDecimal finalScore = p1.add(p2).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            carga.setCalificacionFinal(finalScore);
            if (finalScore.compareTo(new BigDecimal("70")) >= 0) {
                carga.setEstatus("APROBADA");
            } else {
                carga.setEstatus("REPROBADA");
            }
        }

        cargaAlumnoRepository.save(carga);

        String alumnoControl = carga.getEstudiante().getNumeroControl();
        auditoriaService.registrar(profesorControl, "PROFESOR", "CAPTURA_CALIFICACION", 
                "Calificación guardada para estudiante " + alumnoControl + " en grupo " + grupo.getCodigoGrupo() +
                ": P1=" + p1 + ", P2=" + p2 + ", Final=" + carga.getCalificacionFinal(), "127.0.0.1");

        // Notificar al estudiante sobre la publicación/actualización de notas
        notificacionService.enviar(alumnoControl, "Calificaciones Actualizadas", 
                "Tu profesor ha publicado nuevas calificaciones en la materia: " + grupo.getMateria().getNombre());
    }
}
