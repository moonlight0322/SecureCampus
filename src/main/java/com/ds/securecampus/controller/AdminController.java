package com.ds.securecampus.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String dashboard(Model model) {
        List<Usuario> usuarios = adminService.listarUsuarios();
        List<LogAuditoria> logs = adminService.listarAuditoria();
        List<SolicitudTramite> solicitudes = solicitudService.obtenerTodas();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", adminService.listarRoles());
        model.addAttribute("logs", logs);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("materias", adminService.listarMaterias());
        model.addAttribute("profesores", adminService.listarProfesores());
        model.addAttribute("periodos", adminService.listarPeriodos());
        return "admin/dashboard";
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
                               Authentication authentication) {
        adminService.crearUsuario(numeroControl, password, nombre, apePat, apeMat, correo, rolId, authentication.getName());
        return "redirect:/admin?creado=true";
    }

    @PostMapping("/grupos/asignar")
    public String asignarGrupo(@RequestParam("codigoGrupo") String codigoGrupo,
                               @RequestParam("materiaId") Long materiaId,
                               @RequestParam(value = "profesorControl", required = false) String profesorControl,
                               @RequestParam("periodoId") Long periodoId,
                               Authentication authentication) {
        adminService.crearGrupoAsignacion(codigoGrupo, materiaId, profesorControl, periodoId, authentication.getName());
        return "redirect:/admin?grupoAsignado=true";
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
