package com.ds.securecampus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ds.securecampus.repository.CarreraRepository;
import com.ds.securecampus.repository.AulaRepository;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.EdificioRepository;
import com.ds.securecampus.repository.HorarioClaseRepository;
import com.ds.securecampus.repository.EstudianteRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.LogAuditoriaRepository;
import com.ds.securecampus.repository.MateriaRepository;
import com.ds.securecampus.repository.PeriodoRepository;
import com.ds.securecampus.repository.ProfesorRepository;
import com.ds.securecampus.repository.RolRepository;
import com.ds.securecampus.repository.UsuarioRepository;

class AdminServicePeriodTests {
    private final AdminService adminService = new AdminService(
            mock(UsuarioRepository.class),
            mock(RolRepository.class),
            mock(MateriaRepository.class),
            mock(CarreraRepository.class),
            mock(EstudianteRepository.class),
            mock(GrupoRepository.class),
            mock(ProfesorRepository.class),
            mock(PeriodoRepository.class),
            mock(LogAuditoriaRepository.class),
            mock(AuditoriaService.class),
            mock(PasswordEncoder.class),
            mock(AulaRepository.class),
            mock(EdificioRepository.class),
            mock(HorarioClaseRepository.class),
            mock(CargaAlumnoRepository.class));

    @Test
    void calculatesRegularAndSummerAssignmentPeriods() {
        assertEquals("Agosto - Diciembre 2026",
                adminService.periodoAsignacionActual(LocalDate.of(2026, 10, 15)));
        assertEquals("Enero - Junio 2027",
                adminService.periodoAsignacionActual(LocalDate.of(2027, 1, 18)));
        assertEquals("Verano 2027",
                adminService.periodoAsignacionActual(LocalDate.of(2027, 5, 31)));
        assertEquals("Verano 2027",
                adminService.periodoAsignacionActual(LocalDate.of(2027, 7, 25)));
        assertEquals("Agosto - Diciembre 2027",
                adminService.periodoAsignacionActual(LocalDate.of(2027, 7, 26)));
    }

    @Test
    void buildsOnlySupportedWeeklyPatternsAndEnforcesFourOrFiveHours() {
        assertEquals(2, HorarioAsignacion.crear("TR", "09:00", null).size());
        assertEquals("12:00", HorarioAsignacion.crear("MTTR", "11:00", "FIN")
                .getFirst().horaInicio());
        assertEquals("13:00", HorarioAsignacion.crear("MTTR", "11:00", "FIN")
                .getFirst().horaFin());
        assertEquals("12:00", HorarioAsignacion.crear("TTRF", "11:00", "INICIO")
                .getLast().horaFin());
        assertEquals("12:00", HorarioAsignacion.crear("MWF", "11:00", "VIERNES_FIN")
                .getLast().horaInicio());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> HorarioAsignacion.crear("MW", "08:00", null));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> HorarioAsignacion.crear("MWF", "09:00", "MIERCOLES_INICIO"));
    }
}
