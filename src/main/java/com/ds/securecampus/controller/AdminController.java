package com.ds.securecampus.controller;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping
    public String dashboard(Authentication authentication,
                            @RequestParam(defaultValue = "usuarios") String tab,
                            Model model) {
        List<Usuario> usuarios = adminService.listarUsuarios();
        List<LogAuditoria> logs = adminService.listarAuditoria();
        List<SolicitudTramite> solicitudes = solicitudService.obtenerTodas();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("numeroControl", authentication.getName());
        model.addAttribute("roles", adminService.listarRoles());
        model.addAttribute("carreras", adminService.listarCarreras());
        model.addAttribute("logs", logs);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("materias", adminService.listarMaterias());
        model.addAttribute("profesores", adminService.listarProfesores());
        model.addAttribute("periodos", adminService.listarPeriodos());
        model.addAttribute("activeTab", "solicitudes".equals(tab) ? "solicitudes" : "usuarios");
        return "admin/dashboard";
    }

    @PostMapping("/usuarios/toggle-estatus")
    public String toggleEstatus(@RequestParam("numeroControl") String numeroControl,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        try {
            adminService.alternarEstatusUsuario(numeroControl, authentication.getName());
            return "redirect:/admin?usuarioActualizado=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin?error=usuario&tab=usuarios";
        }
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
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        try {
            adminService.crearUsuario(numeroControl, password, nombre, apePat, apeMat, correo,
                    rolId, carreraId, tipoContratacion, cubiculo, authentication.getName());
            return "redirect:/admin?creado=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin?error=usuario&tab=usuarios";
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo guardar la cuenta. Revisa que número de control y correo no estén duplicados.");
            return "redirect:/admin?error=usuario&tab=usuarios";
        }
    }

    @PostMapping("/usuarios/restablecer-password")
    public String restablecerPassword(@RequestParam("numeroControl") String numeroControl,
                                      @RequestParam("password") String password,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {
        try {
            adminService.restablecerPassword(numeroControl, password, authentication.getName());
            return "redirect:/admin?passwordRestablecida=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin?error=password&tab=usuarios";
        }
    }

    @PostMapping("/grupos/asignar")
    public String asignarGrupo(@RequestParam("codigoGrupo") String codigoGrupo,
                               @RequestParam("materiaId") Long materiaId,
                               @RequestParam(value = "profesorControl", required = false) String profesorControl,
                               @RequestParam("periodoId") Long periodoId,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        try {
            adminService.crearGrupoAsignacion(codigoGrupo, materiaId, profesorControl, periodoId, authentication.getName());
            return "redirect:/admin?grupoAsignado=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin?error=grupo&tab=usuarios";
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo asignar el grupo. Comprueba que no exista ya en esa materia y periodo.");
            return "redirect:/admin?error=grupo&tab=usuarios";
        }
    }

    @PostMapping("/solicitudes/resolver")
    public String resolverSolicitud(@RequestParam("idSolicitud") Long idSolicitud,
                                    @RequestParam("estatus") String estatus,
                                    @RequestParam("respuesta") String respuesta,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        try {
            solicitudService.resolverSolicitud(idSolicitud, estatus, respuesta, authentication.getName());
            return "redirect:/admin?solicitudProcesada=true&tab=solicitudes";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin?error=solicitud&tab=solicitudes";
        }
    }
}
