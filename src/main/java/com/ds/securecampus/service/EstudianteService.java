package com.ds.securecampus.service;

import java.util.List;
import java.time.LocalDate;

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
        return cargaAlumnoRepository.buscarHorarioActual(numeroControl, LocalDate.now());
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
}
