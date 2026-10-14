package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.repository.SolicitudTramiteRepository;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {
    @Mock
    private SolicitudTramiteRepository solicitudRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Test
    void resolvesPendingRequestAndAuditsTheDecision() {
        SolicitudTramite solicitud = new SolicitudTramite();
        solicitud.setIdSolicitud(42L);
        solicitud.setFolio("SOL-42");
        solicitud.setEstatus("PENDIENTE");
        when(solicitudRepository.findById(42L)).thenReturn(Optional.of(solicitud));
        SolicitudService service = new SolicitudService(solicitudRepository, auditoriaService);

        service.resolverSolicitud(42L, "APROBADA", "  Autorizada  ", "admin");

        assertThat(solicitud.getEstatus()).isEqualTo("APROBADA");
        assertThat(solicitud.getRespuestaAdmin()).isEqualTo("Autorizada");
        verify(solicitudRepository).save(solicitud);
        verify(auditoriaService).registrar("admin", "ADMINISTRADOR", "SOLICITUD_RESUELTA",
                "Folio: SOL-42 resuelta como APROBADA", "127.0.0.1");
    }

    @Test
    void rejectsUnsupportedDecisionsAndMissingRequests() {
        SolicitudService service = new SolicitudService(solicitudRepository, auditoriaService);

        assertThatThrownBy(() -> service.resolverSolicitud(42L, "PENDIENTE", "Respuesta", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aprobada o rechazada");

        when(solicitudRepository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolverSolicitud(42L, "RECHAZADA", "Respuesta", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La solicitud ya no existe.");
    }

    @Test
    void doesNotAllowResolvingTheSameRequestTwice() {
        SolicitudTramite solicitud = new SolicitudTramite();
        solicitud.setEstatus("APROBADA");
        when(solicitudRepository.findById(42L)).thenReturn(Optional.of(solicitud));
        SolicitudService service = new SolicitudService(solicitudRepository, auditoriaService);

        assertThatThrownBy(() -> service.resolverSolicitud(42L, "RECHAZADA", "Respuesta", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esta solicitud ya fue resuelta.");
    }
}
