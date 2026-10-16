package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.ProfesorRepository;
import com.ds.securecampus.repository.SolicitudTramiteRepository;

class ProfesorServiceTests {
    @Test
    void approvedUnlockRequestOpensGradeCaptureForItsGroup() {
        Grupo grupo = new Grupo();
        grupo.setGrupoId(41L);
        Periodo periodo = new Periodo();
        periodo.setCapturaActiva("N");
        grupo.setPeriodo(periodo);
        SolicitudTramiteRepository solicitudes = mock(SolicitudTramiteRepository.class);
        when(solicitudes.existsByGrupoIdAndTipoTramiteIgnoreCaseAndEstatusIgnoreCase(
                41L, "DESBLOQUEO_CAPTURA", "APROBADA")).thenReturn(true);
        ProfesorService service = service(solicitudes);

        assertThat(service.puedeCapturar(grupo)).isTrue();

        verify(solicitudes).existsByGrupoIdAndTipoTramiteIgnoreCaseAndEstatusIgnoreCase(
                41L, "DESBLOQUEO_CAPTURA", "APROBADA");
    }

    @Test
    void pendingOrUnrelatedUnlockRequestDoesNotOpenGradeCapture() {
        Grupo grupo = new Grupo();
        grupo.setGrupoId(42L);
        Periodo periodo = new Periodo();
        periodo.setCapturaActiva("N");
        grupo.setPeriodo(periodo);
        SolicitudTramiteRepository solicitudes = mock(SolicitudTramiteRepository.class);
        when(solicitudes.existsByGrupoIdAndTipoTramiteIgnoreCaseAndEstatusIgnoreCase(
                42L, "DESBLOQUEO_CAPTURA", "APROBADA")).thenReturn(false);

        assertThat(service(solicitudes).puedeCapturar(grupo)).isFalse();
    }

    private ProfesorService service(SolicitudTramiteRepository solicitudes) {
        return new ProfesorService(
                mock(GrupoRepository.class),
                mock(CargaAlumnoRepository.class),
                mock(ProfesorRepository.class),
                mock(AuditoriaService.class),
                mock(NotificacionService.class),
                solicitudes);
    }
}
