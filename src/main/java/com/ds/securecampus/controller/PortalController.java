package com.ds.securecampus.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.CalificacionMateria;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.model.HorarioSemanalFila;
import com.ds.securecampus.service.DocumentoPdfService;
import com.ds.securecampus.service.EstudianteService;
import com.ds.securecampus.service.NotificacionService;
import com.ds.securecampus.service.SolicitudService;

@Controller
public class PortalController {
    private final EstudianteService estudianteService;
    private final DocumentoPdfService documentoPdfService;
    private final SolicitudService solicitudService;
    private final NotificacionService notificacionService;

    public PortalController(EstudianteService estudianteService,
                            DocumentoPdfService documentoPdfService,
                            SolicitudService solicitudService,
                            NotificacionService notificacionService) {
        this.estudianteService = estudianteService;
        this.documentoPdfService = documentoPdfService;
        this.solicitudService = solicitudService;
        this.notificacionService = notificacionService;
    }

    @GetMapping({"/", "/login"})
    public String login(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? "login"
                : "redirect:/acceso";
    }

    @GetMapping("/recuperar")
    public String recuperarContrasena() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String procesarRecuperacion(@RequestParam("numeroControl") String numeroControl, Model model) {
        model.addAttribute("mensaje", "La recuperación automática por correo aún no está habilitada. Solicita el restablecimiento de tu contraseña al administrador escolar.");
        return "recuperar";
    }

    @GetMapping("/acceso")
    public String acceso(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDIANTE"))) {
            return "redirect:/estudiante";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
            return "redirect:/profesor";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_ADMINISTRATIVO"))) {
            return "redirect:/admin";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_JEFE_DEPARTAMENTO"))) {
            return "redirect:/jefe/dashboard";
        }
        return "rol-no-disponible";
    }

    @GetMapping("/estudiante")
    public String inicioEstudiante(Authentication authentication, Model model) {
        String numControl = authentication.getName();
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numControl));
        model.addAttribute("materias", estudianteService.obtenerCargaActual(numControl));
        model.addAttribute("clasesHoy", estudianteService.obtenerClasesHoy(numControl));
        model.addAttribute("notificaciones", notificacionService.obtenerNotificaciones(numControl));
        model.addAttribute("solicitudes", solicitudService.obtenerPorUsuario(numControl));
        return "estudiante/inicio";
    }

    @GetMapping("/estudiante/horario")
    public String horario(Authentication authentication, Model model) {
        String numControl = authentication.getName();
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numControl));
        model.addAttribute("filasHorario", estudianteService.obtenerHorarioSemanal(numControl));
        return "estudiante/horario";
    }

    @GetMapping({"/estudiante/kardex", "/estudiante/cardex"})
    public String kardex(Authentication authentication, Model model) {
        String numControl = authentication.getName();
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numControl));
        model.addAttribute("materias", estudianteService.obtenerKardex(numControl));
        return "estudiante/cardex";
    }

    @GetMapping("/estudiante/carga")
    public String cargaAcademica(Authentication authentication, Model model) {
        String numeroControl = authentication.getName();
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numeroControl));
        model.addAttribute("materias", estudianteService.obtenerCargaActual(numeroControl));
        return "estudiante/carga";
    }

    @GetMapping("/estudiante/calificaciones")
    public String calificaciones(Authentication authentication, Model model) {
        String numeroControl = authentication.getName();
        List<CalificacionMateria> calificaciones =
                estudianteService.obtenerCalificacionesActuales(numeroControl);
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numeroControl));
        model.addAttribute("calificaciones", calificaciones);
        return "estudiante/calificaciones";
    }

    @GetMapping("/estudiante/solicitudes")
    public String solicitudes(Authentication authentication, Model model) {
        String numControl = authentication.getName();
        model.addAttribute("estudiante", estudianteService.obtenerPerfil(numControl));
        model.addAttribute("solicitudes", solicitudService.obtenerPorUsuario(numControl));
        return "estudiante/solicitudes";
    }

    @PostMapping("/estudiante/solicitudes/crear")
    public String crearSolicitud(Authentication authentication,
                                 @RequestParam("tipoTramite") String tipoTramite,
                                 @RequestParam("motivo") String motivo) {
        solicitudService.crearSolicitud(authentication.getName(), tipoTramite, motivo, null);
        return "redirect:/estudiante/solicitudes?creada=true";
    }

    @GetMapping("/estudiante/solicitudes/{id}/pdf")
    public ResponseEntity<byte[]> descargarTramiteAprobado(
            Authentication authentication, @PathVariable Long id) {
        SolicitudTramite solicitud = solicitudService.obtenerAprobada(id, authentication.getName())
                .orElse(null);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        PerfilEstudiante perfil = estudianteService.obtenerPerfil(authentication.getName());
        byte[] pdf = documentoPdfService.generarComprobanteTramite(perfil, solicitud);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Tramite_" + solicitud.getFolio() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // --- Descargas de Documentos PDF Oficiales ---

    @GetMapping("/estudiante/constancia/pdf")
    public ResponseEntity<byte[]> descargarConstancia(Authentication authentication) {
        String numControl = authentication.getName();
        PerfilEstudiante perfil = estudianteService.obtenerPerfil(numControl);
        Estudiante est = estudianteService.obtenerEntidad(numControl);

        // Validación de regla de negocio: debe estar inscrito
        if (!"S".equalsIgnoreCase(est.getPagoReinscripcion())) {
            return ResponseEntity.status(403)
                    .contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
                    .body("No es posible generar la constancia: el alumno no cuenta con estatus inscrito vigente."
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        byte[] pdf = documentoPdfService.generarConstanciaEstudios(perfil, est);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Constancia_" + numControl + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estudiante/kardex/pdf")
    public ResponseEntity<byte[]> descargarKardexPdf(Authentication authentication) {
        String numControl = authentication.getName();
        PerfilEstudiante perfil = estudianteService.obtenerPerfil(numControl);
        List<MateriaKardex> kardex = estudianteService.obtenerKardex(numControl);

        byte[] pdf = documentoPdfService.generarKardexPdf(perfil, kardex);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Kardex_" + numControl + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estudiante/horario/pdf")
    public ResponseEntity<byte[]> descargarHorarioPdf(Authentication authentication) {
        String numControl = authentication.getName();
        PerfilEstudiante perfil = estudianteService.obtenerPerfil(numControl);
        List<HorarioSemanalFila> filas = estudianteService.obtenerHorarioSemanal(numControl);

        byte[] pdf = documentoPdfService.generarHorarioSemanalPdf(perfil, filas);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Horario_" + numControl + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
