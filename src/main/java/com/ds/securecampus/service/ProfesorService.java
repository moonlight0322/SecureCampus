package com.ds.securecampus.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.HorarioClase;
import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.GrupoRepository;

@Service
public class ProfesorService {
    private final GrupoRepository grupoRepository;
    private final CargaAlumnoRepository cargaAlumnoRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public ProfesorService(GrupoRepository grupoRepository,
                           CargaAlumnoRepository cargaAlumnoRepository,
                           AuditoriaService auditoriaService,
                           NotificacionService notificacionService) {
        this.grupoRepository = grupoRepository;
        this.cargaAlumnoRepository = cargaAlumnoRepository;
        this.auditoriaService = auditoriaService;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<Grupo> obtenerGruposProfesor(String numeroControl) {
        return grupoRepository.findByProfesorNumeroControl(numeroControl);
    }

    @Transactional(readOnly = true)
    public Grupo obtenerGrupoProfesor(Long grupoId, String profesorControl) {
        return grupoRepository.findByGrupoIdAndProfesorNumeroControl(grupoId, profesorControl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<CargaAlumno> obtenerAlumnosGrupoProfesor(Long grupoId, String profesorControl) {
        return cargaAlumnoRepository.findByGrupoGrupoIdAndGrupoProfesorNumeroControl(grupoId, profesorControl);
    }

    @Transactional(readOnly = true)
    public List<MateriaHorario> obtenerHorarioProfesor(String profesorControl) {
        List<MateriaHorario> horario = new ArrayList<>();
        for (Grupo grupo : grupoRepository.findByProfesorNumeroControl(profesorControl)) {
            for (HorarioClase clase : grupo.getHorarios()) {
                String dia = switch (clase.getDiaSemana()) {
                    case 1 -> "Lunes";
                    case 2 -> "Martes";
                    case 3 -> "Miércoles";
                    case 4 -> "Jueves";
                    case 5 -> "Viernes";
                    case 6 -> "Sábado";
                    default -> "Día no definido";
                };
                horario.add(new MateriaHorario(
                        grupo.getMateria().getNombre(),
                        grupo.getMateria().getClave(),
                        grupo.getCodigoGrupo(),
                        clase.getAula().getNombreAula(),
                        dia + " " + clase.getHoraInicio() + " - " + clase.getHoraFin(),
                        grupo.getProfesor().getUsuario().getNombre() + " "
                                + grupo.getProfesor().getUsuario().getApellidoPaterno(),
                        grupo.getPeriodo().getNombre()));
            }
        }
        return horario;
    }

    @Transactional(readOnly = true)
    public boolean puedeCapturar(Grupo grupo) {
        if (grupo == null || grupo.getPeriodo() == null) return false;
        Periodo per = grupo.getPeriodo();
        // Solo permite edición si captura_activa es 'S'
        return "S".equalsIgnoreCase(per.getCapturaActiva());
    }

    @Transactional
    public void guardarCalificacion(Long grupoId, Long cargaId, BigDecimal p1, BigDecimal p2, String profesorControl) {
        CargaAlumno carga = cargaAlumnoRepository.findById(cargaId)
                .orElseThrow(() -> new IllegalArgumentException("Carga no encontrada"));

        Grupo grupo = carga.getGrupo();
        if (!grupoId.equals(grupo.getGrupoId())
                || grupo.getProfesor() == null
                || !profesorControl.equals(grupo.getProfesor().getNumeroControl())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo o carga no encontrados.");
        }
        if (!puedeCapturar(grupo)) {
            throw new IllegalStateException("El periodo de captura se encuentra cerrado para este grupo. Solicite desbloqueo al administrador.");
        }

        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Captura las dos calificaciones parciales antes de guardar.");
        }
        // Validación de rango 0 a 100
        if (p1.compareTo(BigDecimal.ZERO) < 0 || p1.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La calificación parcial 1 debe estar entre 0 y 100.");
        }
        if (p2.compareTo(BigDecimal.ZERO) < 0 || p2.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La calificación parcial 2 debe estar entre 0 y 100.");
        }

        carga.setCalificacionParcial1(p1);
        carga.setCalificacionParcial2(p2);

        // Promedio automático
        BigDecimal finalScore = p1.add(p2).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        carga.setCalificacionFinal(finalScore);
        if (finalScore.compareTo(new BigDecimal("70")) >= 0) {
            carga.setEstatus("APROBADA");
        } else {
            carga.setEstatus("REPROBADA");
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
