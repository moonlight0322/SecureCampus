package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ds.securecampus.model.HorarioClaseEstudiante;
import com.ds.securecampus.model.HorarioSemanalFila;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.EstudianteRepository;
import com.ds.securecampus.repository.KardexRepository;

class EstudianteServiceTests {
    private static final ZoneId CAMPUS_ZONE = ZoneId.of("America/Mexico_City");

    @Test
    void obtainsOnlyTheClassesScheduledForTodayInTheCampusTimeZone() {
        LocalDate today = LocalDate.now(CAMPUS_ZONE);
        byte dayToday = (byte) today.getDayOfWeek().getValue();
        byte anotherDay = (byte) (dayToday % 7 + 1);
        CargaAlumnoRepository cargaRepository = mock(CargaAlumnoRepository.class);
        when(cargaRepository.buscarHorarioSemanal(eq("22280691"), any(LocalDate.class)))
                .thenReturn(List.of(
                        new HorarioClaseEstudiante(dayToday, "11:00", "13:00", "Materia uno", "E-3"),
                        new HorarioClaseEstudiante(dayToday, "13:00", "15:00", "Materia dos", "E-2"),
                        new HorarioClaseEstudiante(dayToday, "15:00", "16:00", "Materia tres", "U-3"),
                        new HorarioClaseEstudiante(anotherDay, "09:00", "11:00", "Otra materia", "B2-1")));
        EstudianteService service = service(cargaRepository);

        List<HorarioClaseEstudiante> classesToday = service.obtenerClasesHoy("22280691");

        assertThat(classesToday).extracting(HorarioClaseEstudiante::materia)
                .containsExactly("Materia uno", "Materia dos", "Materia tres");
    }

    @Test
    void rendersTheWeeklyTimetableAsUniqueOneHourRows() {
        CargaAlumnoRepository cargaRepository = mock(CargaAlumnoRepository.class);
        when(cargaRepository.buscarHorarioSemanal(eq("22280691"), any(LocalDate.class)))
                .thenReturn(List.of(
                        new HorarioClaseEstudiante((byte) 2, "11:00", "13:00", "Estructura de Datos", "E-3"),
                        new HorarioClaseEstudiante((byte) 1, "12:00", "13:00", "Cálculo", "E-2"),
                        new HorarioClaseEstudiante((byte) 3, "13:00", "14:00", "Programación", "U-3")));
        EstudianteService service = service(cargaRepository);

        List<HorarioSemanalFila> rows = service.obtenerHorarioSemanal("22280691");

        assertThat(rows).extracting(HorarioSemanalFila::getHorario)
                .containsExactly("11:00 - 12:00", "12:00 - 13:00", "13:00 - 14:00");
        assertThat(rows.get(0).getMartes()).contains("Estructura de Datos");
        assertThat(rows.get(1).getMartes()).contains("Estructura de Datos");
        assertThat(rows.get(1).getLunes()).contains("Cálculo");
        assertThat(rows.get(2).getMiercoles()).contains("Programación");
    }

    private EstudianteService service(CargaAlumnoRepository cargaRepository) {
        return new EstudianteService(
                mock(EstudianteRepository.class),
                cargaRepository,
                mock(KardexRepository.class));
    }
}
