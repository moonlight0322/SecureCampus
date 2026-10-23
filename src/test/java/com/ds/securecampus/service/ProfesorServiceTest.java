package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.ProfesorRepository;
import com.ds.securecampus.repository.SolicitudTramiteRepository;
import com.ds.securecampus.repository.HorarioClaseRepository;

@ExtendWith(MockitoExtension.class)
class ProfesorServiceTest {
    @Mock private GrupoRepository grupoRepository;
    @Mock private CargaAlumnoRepository cargaAlumnoRepository;
    @Mock private AuditoriaService auditoriaService;
    @Mock private NotificacionService notificacionService;
    @Mock private HorarioClaseRepository horarioClaseRepository;
    
    private ProfesorService profesorService;
    
    @BeforeEach
    void setUp() {
        profesorService = new ProfesorService(grupoRepository, cargaAlumnoRepository,
                mock(ProfesorRepository.class), auditoriaService, notificacionService,
                mock(SolicitudTramiteRepository.class),
                horarioClaseRepository);
    }

    @Test
    void rejectsSavingGradeWhenUrlGroupDoesNotOwnTheCharge() {
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("profesor-1");
        Periodo periodo = new Periodo();
        periodo.setCapturaActiva("S");
        Grupo grupo = new Grupo();
        grupo.setGrupoId(10L);
        grupo.setProfesor(profesor);
        grupo.setPeriodo(periodo);
        CargaAlumno carga = new CargaAlumno();
        carga.setCargaId(20L);
        carga.setGrupo(grupo);
        when(cargaAlumnoRepository.findById(20L)).thenReturn(Optional.of(carga));

        assertThatThrownBy(() -> profesorService.guardarCalificacion(11L, 20L,
                new BigDecimal("90"), new BigDecimal("90"), "profesor-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La carga del estudiante no pertenece al grupo indicado.");

        verify(cargaAlumnoRepository, never()).save(carga);
    }
}
