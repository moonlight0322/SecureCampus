package com.ds.securecampus.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes_tramite")
public class SolicitudTramite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(nullable = false, unique = true, length = 50)
    private String folio;

    @Column(name = "numero_control", nullable = false, length = 20)
    private String numeroControl;

    @Column(name = "tipo_tramite", nullable = false, length = 50)
    private String tipoTramite;

    @Column(length = 500)
    private String motivo;

    @Column(nullable = false, length = 20)
    private String estatus = "PENDIENTE";

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "respuesta_admin", length = 500)
    private String respuestaAdmin;
}
