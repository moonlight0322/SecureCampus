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

import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.service.AdminService;

@Controller
@RequestMapping("/jefe")
public class JefeDepartamentoController {
    private final AdminService adminService;

    public JefeDepartamentoController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        List<Carrera> carreras = adminService.listarCarreras();
        List<Materia> materias = adminService.listarMaterias();
        List<Profesor> profesores = adminService.listarProfesores();
        List<Periodo> periodos = adminService.listarPeriodos();

        model.addAttribute("carreras", carreras);
        model.addAttribute("numeroControl", authentication.getName());
        model.addAttribute("materias", materias);
        model.addAttribute("profesores", profesores);
        model.addAttribute("periodos", periodos);
        return "jefe/dashboard";
    }

    @PostMapping("/materias/alta")
    public String altaMateria(@RequestParam("clave") String clave,
                             @RequestParam("nombre") String nombre,
                             @RequestParam("creditos") Integer creditos,
                             @RequestParam("carreraId") Long carreraId,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            adminService.altaMateria(clave, nombre, creditos, carreraId, authentication.getName());
            return "redirect:/jefe?materiaCreada=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/jefe?error=materia";
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo dar de alta la materia. Verifica que la clave no esté duplicada.");
            return "redirect:/jefe?error=materia";
        }
    }

    @PostMapping("/grupos/asignar")
    public String asignarProfesorAGrupo(@RequestParam("codigoGrupo") String codigoGrupo,
                                        @RequestParam("materiaId") Long materiaId,
                                        @RequestParam("profesorControl") String profesorControl,
                                        @RequestParam("periodoId") Long periodoId,
                                        RedirectAttributes redirectAttributes,
                                        Authentication authentication) {
        try {
            adminService.crearGrupoAsignacion(codigoGrupo, materiaId, profesorControl, periodoId, authentication.getName());
            return "redirect:/jefe?asignado=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/jefe?error=grupo";
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo asignar el grupo. Comprueba que no exista ya para la materia y periodo.");
            return "redirect:/jefe?error=grupo";
        }
    }
}
