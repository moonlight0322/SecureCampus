package com.ds.securecampus.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.service.AdminService;
import com.ds.securecampus.service.SolicitudService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final SolicitudService solicitudService;

    public AdminController(AdminService adminService, SolicitudService solicitudService) {
        this.adminService = adminService;
        this.solicitudService = solicitudService;
    }

    @GetMapping("")
    public String dashboard(Authentication authentication, Model model) {
        List<Usuario> usuarios = adminService.listarUsuarios();
        List<LogAuditoria> logs = adminService.listarAuditoria();
        List<SolicitudTramite> solicitudes = solicitudService.obtenerTodas();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("administrativo", adminService.obtenerUsuario(authentication.getName()));
        model.addAttribute("roles", adminService.listarRoles());
        model.addAttribute("logs", logs);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("materias", adminService.listarMaterias());
        model.addAttribute("profesores", adminService.listarProfesores());
        model.addAttribute("carreras", adminService.listarCarreras());
        model.addAttribute("periodoAsignacion", adminService.periodoAsignacionActual(LocalDate.now()));
        model.addAttribute("grupos", adminService.listarGruposPeriodoActual());
        model.addAttribute("estudiantes", adminService.listarEstudiantes());
        model.addAttribute("aulas", adminService.listarNombresAulas());
        return "admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboardAlias() {
        return "redirect:/admin";
    }

    @PostMapping("/usuarios/toggle-estatus")
    public String toggleEstatus(@RequestParam("numeroControl") String numeroControl, Authentication authentication) {
        adminService.alternarEstatusUsuario(numeroControl, authentication.getName());
        return "redirect:/admin?usuarioActualizado=true";
    }

    @PostMapping("/usuarios/crear")
    public String crearUsuario(@RequestParam("numeroControl") String numeroControl,
                               @RequestParam("password") String password,
                               @RequestParam("nombre") String nombre,
                               @RequestParam("apellidoPaterno") String apePat,
                               @RequestParam(value = "apellidoMaterno", required = false) String apeMat,
                               @RequestParam("correoInstitucional") String correo,
                               @RequestParam("rolId") Long rolId,
                               @RequestParam(value = "carreraId", required = false) Long carreraId,
                               @RequestParam(value = "tipoContratacion", required = false) String tipoContratacion,
                               @RequestParam(value = "cubiculo", required = false) String cubiculo,
                               @RequestParam(value = "confirmarPassword", required = false) String confirmarPassword,
                               Authentication authentication) {
        if (!password.equals(confirmarPassword)) {
            throw new IllegalArgumentException("La confirmación de la contraseña no coincide.");
        }
        adminService.crearUsuario(numeroControl, password, nombre, apePat, apeMat, correo, rolId,
                carreraId, tipoContratacion, cubiculo, authentication.getName());
        return "redirect:/admin?creado=true";
    }

    @PostMapping("/grupos/asignar")
    public String asignarGrupo(    @RequestParam("codigoGrupo") String codigoGrupo,
    @RequestParam("materiaId") Long materiaId,
    @RequestParam(value = "profesorControl", required = false) String profesorControl,
    @RequestParam("patronHorario") String patronHorario,
    @RequestParam("horaInicio") String horaInicio,
    @RequestParam(value = "horaCorta", required = false) String horaCorta,
    @RequestParam("aula") String aula,
    Authentication authentication) {
        adminService.crearGrupoAsignacion(codigoGrupo, materiaId, profesorControl,
                patronHorario, horaInicio, horaCorta, aula, authentication.getName());
        return "redirect:/admin?grupoAsignado=true";
    }

    @PostMapping("/grupos/inscribir")
    public String inscribirEstudiante(@RequestParam("grupoId") Long grupoId,
                                      @RequestParam("numeroControl") String numeroControl,
                                      Authentication authentication) {
        adminService.inscribirEstudiante(grupoId, numeroControl, authentication.getName());
        return "redirect:/admin?inscripcion=true";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String manejarErrorValidacion(IllegalArgumentException exception, Model model,
                                         Authentication authentication) {
        dashboard(authentication, model);
        model.addAttribute("errorOperacion", exception.getMessage());
        return "admin/dashboard";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String manejarRestriccionBaseDatos(DataIntegrityViolationException exception, Model model,
                                               Authentication authentication) {
        dashboard(authentication, model);
        model.addAttribute("errorOperacion",
                "No se pudo guardar por una restricción de datos. Verifica que el número de control, correo o código de grupo no estén duplicados.");
        return "admin/dashboard";
    }

    @PostMapping("/solicitudes/resolver")
    public String resolverSolicitud(@RequestParam("idSolicitud") Long idSolicitud,
                                    @RequestParam("estatus") String estatus,
                                    @RequestParam("respuesta") String respuesta,
                                    Authentication authentication) {
        solicitudService.resolverSolicitud(idSolicitud, estatus, respuesta, authentication.getName());
        return "redirect:/admin?solicitudProcesada=true";
    }
}
