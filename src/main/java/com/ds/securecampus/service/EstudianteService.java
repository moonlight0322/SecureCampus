package com.ds.securecampus.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ds.securecampus.model.CalificacionMateria;
import com.ds.securecampus.model.CargaAcademicaMateria;
import com.ds.securecampus.model.HorarioClaseEstudiante;
import com.ds.securecampus.model.HorarioSemanalFila;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.EstudianteRepository;
import com.ds.securecampus.repository.KardexRepository;

@Service
public class EstudianteService {
    private static final ZoneId CAMPUS_ZONE = ZoneId.of("America/Mexico_City");

    private final EstudianteRepository estudianteRepository;
    private final CargaAlumnoRepository cargaAlumnoRepository;
    private final KardexRepository kardexRepository;

    public EstudianteService(
            EstudianteRepository estudianteRepository,
            CargaAlumnoRepository cargaAlumnoRepository,
            KardexRepository kardexRepository) {
        this.estudianteRepository = estudianteRepository;
        this.cargaAlumnoRepository = cargaAlumnoRepository;
        this.kardexRepository = kardexRepository;
    }

    @Transactional(readOnly = true)
    public PerfilEstudiante obtenerPerfil(String numeroControl) {
        return estudianteRepository.buscarPerfil(numeroControl)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario autenticado no tiene un perfil de estudiante asociado."));
    }

    @Transactional(readOnly = true)
    public List<MateriaHorario> obtenerHorario(String numeroControl) {
        return cargaAlumnoRepository.buscarHorarioActual(numeroControl, fechaActual());
    }

    @Transactional(readOnly = true)
    public List<CargaAcademicaMateria> obtenerCargaActual(String numeroControl) {
        return cargaAlumnoRepository.buscarCargaActual(numeroControl, fechaActual());
    }

    @Transactional(readOnly = true)
    public List<HorarioClaseEstudiante> obtenerClasesHoy(String numeroControl) {
        LocalDate hoy = fechaActual();
        int diaHoy = hoy.getDayOfWeek().getValue();
        return cargaAlumnoRepository.buscarHorarioSemanal(numeroControl, hoy).stream()
                .filter(clase -> clase.diaSemana() != null && clase.diaSemana().intValue() == diaHoy)
                .sorted(java.util.Comparator.comparing(HorarioClaseEstudiante::horaInicio))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HorarioSemanalFila> obtenerHorarioSemanal(String numeroControl) {
        List<HorarioClaseEstudiante> clases =
                cargaAlumnoRepository.buscarHorarioSemanal(numeroControl, fechaActual());
        Map<LocalTime, HorarioSemanalFila> filas = new TreeMap<>();
        for (HorarioClaseEstudiante clase : clases) {
            LocalTime inicio = LocalTime.parse(clase.horaInicio());
            LocalTime fin = LocalTime.parse(clase.horaFin());
            for (LocalTime hora = inicio; hora.isBefore(fin); hora = hora.plusHours(1)) {
                LocalTime siguienteHora = hora.plusHours(1);
                String tramo = hora + " - " + siguienteHora;
                HorarioSemanalFila fila = filas.computeIfAbsent(hora, ignored -> new HorarioSemanalFila(tramo));
                fila.agregarClase(clase.diaSemana(), clase.materia() + " · " + clase.salon());
            }
        }
        return List.copyOf(filas.values());
    }

    @Transactional(readOnly = true)
    public List<CalificacionMateria> obtenerCalificacionesActuales(String numeroControl) {
        return cargaAlumnoRepository.buscarCalificacionesActuales(numeroControl, fechaActual());
    }

    @Transactional(readOnly = true)
    public List<MateriaKardex> obtenerKardex(String numeroControl) {
        return kardexRepository.buscarKardex(numeroControl);
    }

    @Transactional(readOnly = true)
    public com.ds.securecampus.model.Estudiante obtenerEntidad(String numeroControl) {
        return estudianteRepository.findById(numeroControl)
                .orElseThrow(() -> new IllegalStateException("Estudiante no encontrado"));
    }

    private LocalDate fechaActual() {
        return LocalDate.now(CAMPUS_ZONE);
    }
}
