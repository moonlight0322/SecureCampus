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

@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;
    private final CargaAlumnoRepository cargaAlumnoRepository;

    public EstudianteService(
            EstudianteRepository estudianteRepository,
            CargaAlumnoRepository cargaAlumnoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.cargaAlumnoRepository = cargaAlumnoRepository;
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
        return cargaAlumnoRepository.buscarKardex(numeroControl);
    }
}
