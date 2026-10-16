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

import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.service.AdminService;

@Controller
@RequestMapping("/jefe")
public class JefeDepartamentoController {
    private final AdminService adminService;

    public JefeDepartamentoController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        List<Materia> materias = adminService.listarMaterias();
        List<Profesor> profesores = adminService.listarProfesores();

        model.addAttribute("usuario", adminService.obtenerUsuario(authentication.getName()));
        model.addAttribute("numeroControl", authentication.getName());
        model.addAttribute("materias", materias);
        model.addAttribute("profesores", profesores);
        model.addAttribute("periodoAsignacion", adminService.periodoAsignacionActual(LocalDate.now()));
        model.addAttribute("aulas", adminService.listarNombresAulas());
        return "jefe/dashboard";
    }

    @PostMapping("/grupos/asignar")
    public String asignarProfesorAGrupo(    @RequestParam("codigoGrupo") String codigoGrupo,
    @RequestParam("materiaId") Long materiaId,
    @RequestParam("profesorControl") String profesorControl,
    @RequestParam("patronHorario") String patronHorario,
    @RequestParam("horaInicio") String horaInicio,
    @RequestParam(value = "horaCorta", required = false) String horaCorta,
    @RequestParam("aula") String aula,
    Authentication authentication) {
        adminService.crearGrupoAsignacion(codigoGrupo, materiaId, profesorControl,
                patronHorario, horaInicio, horaCorta, aula, authentication.getName());
        return "redirect:/jefe?asignado=true";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String manejarErrorValidacion(IllegalArgumentException exception, Authentication authentication,
                                         Model model) {
        dashboard(authentication, model);
        model.addAttribute("error", exception.getMessage());
        return "jefe/dashboard";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String manejarRestriccionBaseDatos(DataIntegrityViolationException exception,
                                              Authentication authentication, Model model) {
        dashboard(authentication, model);
        model.addAttribute("error",
                "No se pudo guardar el grupo. Verifica que el código no esté repetido para esa materia y periodo, y vuelve a intentarlo.");
        return "jefe/dashboard";
    }
}
