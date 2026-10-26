package com.ds.securecampus.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.NotificacionRepository;
import com.ds.securecampus.repository.UsuarioRepository;
import com.ds.securecampus.service.NotificacionService;

@Component
@Order(10)
public class NotificacionDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(NotificacionDataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionService notificacionService;

    public NotificacionDataInitializer(UsuarioRepository usuarioRepository,
                                       NotificacionRepository notificacionRepository,
                                       NotificacionService notificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public void run(String... args) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            int totalInicializadas = 0;

            for (Usuario u : usuarios) {
                String nc = u.getNumeroControl();
                if (nc == null || nc.isBlank()) continue;

                if (!notificacionRepository.existsByNumeroControl(nc)) {
                    sembrarNotificacionesPorRol(u);
                    totalInicializadas++;
                }
            }

            if (totalInicializadas > 0) {
                log.info("Notificaciones iniciales sembradas para {} perfiles de usuario.", totalInicializadas);
            }
        } catch (Exception e) {
            log.warn("No se pudieron inicializar notificaciones demostrativas: {}", e.getMessage());
        }
    }

    private void sembrarNotificacionesPorRol(Usuario u) {
        String nc = u.getNumeroControl();
        String rol = (u.getRol() != null && u.getRol().getNombreRol() != null)
                ? u.getRol().getNombreRol().toUpperCase() : "";

        if (rol.contains("ESTUDIANTE")) {
            notificacionService.enviar(nc, "¡Bienvenido al Semestre 2026-1!",
                    "Te damos la bienvenida al portal institucional SecureCampus. Recuerda revisar tu horario y carga académica en los paneles de navegación.");
            notificacionService.enviar(nc, "Validación de Reinscripción",
                    "Tu estatus de pago ha sido procesado. Puedes tramitar y descargar tu constancia oficial de estudios desde el menú de trámites.");
            notificacionService.enviar(nc, "Seguridad de la Cuenta",
                    "Por tu seguridad, recuerda mantener protegida tu contraseña institucional y actualizarla periódicamente.");
        } else if (rol.contains("DOCENTE") || rol.contains("PROFESOR")) {
            notificacionService.enviar(nc, "Asignación de Cargas y Horarios 2026-1",
                    "Se han registrado tus grupos y salones asignados para el periodo escolar actual. Puedes consultarlos en las secciones 'Mis grupos' y 'Mi horario'.");
            notificacionService.enviar(nc, "Captura de Calificaciones Habilitada",
                    "El sistema se encuentra abierto para la captura de calificaciones y evaluaciones de las unidades correspondientes.");
            notificacionService.enviar(nc, "Monitoreo y Estadísticas",
                    "Ya puedes analizar las métricas de aprovechamiento y promedios grupales con gráficos interactivos en el módulo de Estadísticas.");
        } else if (rol.contains("ADMIN")) {
            notificacionService.enviar(nc, "Panel de Administración y Seguridad",
                    "Módulo Central activo: gestión de identidades, control de acceso RBAC, auditoría y mitigación de ataques por fuerza bruta.");
            notificacionService.enviar(nc, "Trámites de Alumnos Pendientes",
                    "Hay solicitudes de trámites escolares en espera de revisión. Puedes gestionarlas y resolverlas desde la pestaña 'Trámites y Desbloqueos'.");
            notificacionService.enviar(nc, "Políticas de Seguridad y Auditoría",
                    "El registro de eventos de auditoría se encuentra operando en tiempo real con trazabilidad de direcciones IP.");
        } else if (rol.contains("JEFE")) {
            notificacionService.enviar(nc, "Oferta Académica Periodo 2026-1",
                    "Bienvenido a la coordinación de planes de estudio. La retícula, materias y catálogo de asignaturas se encuentran listos para gestión.");
            notificacionService.enviar(nc, "Supervisión de Grupos y Cupos",
                    "Recuerda que los grupos con menos de 15 alumnos cuentan con aviso de baja demanda y pueden ser reasignados o cerrados.");
            notificacionService.enviar(nc, "Inscripción Masiva con Lista de Espera",
                    "El módulo de inscripción múltiple de estudiantes está disponible para agregar alumnos por grupo sin empalmes de horario.");
        } else {
            notificacionService.enviar(nc, "Bienvenido a SecureCampus",
                    "Tu cuenta se encuentra activa en el portal institucional.");
        }
    }
}
