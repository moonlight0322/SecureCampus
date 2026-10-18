package com.ds.securecampus.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.EstadisticaGrupo;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Grupo;
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
        List<Estudiante> estudiantes = adminService.listarEstudiantes();
        List<Grupo> grupos = adminService.listarGruposPeriodoActual();
        List<EstadisticaGrupo> estadisticas = adminService.obtenerEstadisticasTodosGrupos();

        model.addAttribute("usuario", adminService.obtenerUsuario(authentication.getName()));
        model.addAttribute("numeroControl", authentication.getName());
        model.addAttribute("materias", materias);
        model.addAttribute("profesores", profesores);
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("grupos", grupos);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("periodoAsignacion", adminService.periodoAsignacionActual(LocalDate.now()));
        model.addAttribute("aulas", adminService.listarNombresAulas());
        return "jefe/dashboard";
    }

    @GetMapping("/estudiante/{numeroControl}")
    public String verDetalleEstudiante(@PathVariable("numeroControl") String numeroControl,
                                       Authentication authentication, Model model) {
        Estudiante estudiante = adminService.obtenerEstudiante(numeroControl);
        List<CargaAlumno> cargas = adminService.obtenerCargasEstudiantePeriodoActual(numeroControl);
        List<Grupo> gruposDisponibles = adminService.listarGruposPeriodoActual();

        model.addAttribute("usuario", adminService.obtenerUsuario(authentication.getName()));
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("cargas", cargas);
        model.addAttribute("gruposDisponibles", gruposDisponibles);
        return "jefe/estudiante";
    }

    @PostMapping("/estudiante/{numeroControl}/cargar-materia")
    public String cargarMateriaEstudiante(@PathVariable("numeroControl") String numeroControl,
                                         @RequestParam("grupoId") Long grupoId,
                                         Authentication authentication) {
        adminService.inscribirEstudiante(grupoId, numeroControl, authentication.getName());
        return "redirect:/jefe/estudiante/" + numeroControl + "?materiaCargada=true";
    }

    @PostMapping("/grupos/asignar")
    public String asignarProfesorAGrupo(@RequestParam("codigoGrupo") String codigoGrupo,
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

    @PostMapping("/grupos/{grupoId}/quitar-estudiante")
    public String quitarEstudianteDeGrupo(@PathVariable("grupoId") Long grupoId,
                                         @RequestParam("numeroControl") String numeroControl,
                                         @RequestParam(value = "redirectEstudiante", required = false) Boolean redirectEstudiante,
                                         Authentication authentication) {
        adminService.quitarEstudianteDeGrupo(grupoId, numeroControl, authentication.getName());
        if (Boolean.TRUE.equals(redirectEstudiante)) {
            return "redirect:/jefe/estudiante/" + numeroControl + "?estudianteDadoDeBaja=true";
        }
        return "redirect:/jefe?estudianteQuitado=true";
    }

    @PostMapping("/grupos/{grupoId}/cambiar-profesor")
    public String cambiarProfesorGrupo(@PathVariable("grupoId") Long grupoId,
                                       @RequestParam("profesorControl") String profesorControl,
                                       Authentication authentication) {
        adminService.cambiarProfesorGrupo(grupoId, profesorControl, authentication.getName());
        return "redirect:/jefe?docenteActualizado=true";
    }

    @PostMapping("/grupos/{grupoId}/cerrar")
    public String cerrarGrupo(@PathVariable("grupoId") Long grupoId,
                              Authentication authentication) {
        adminService.cerrarGrupo(grupoId, authentication.getName());
        return "redirect:/jefe?grupoCerrado=true";
    }

    @PostMapping("/grupos/inscribir-multiples")
    public String inscribirMultiplesEstudiantes(@RequestParam("grupoId") Long grupoId,
                                               @RequestParam("alumnosControl") String alumnosControlRaw,
                                               Authentication authentication) {
        if (alumnosControlRaw != null && !alumnosControlRaw.isBlank()) {
            List<String> lista = Arrays.stream(alumnosControlRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            int inscritos = adminService.inscribirMultiplesEstudiantes(grupoId, lista, authentication.getName());
            return "redirect:/jefe?multiplesInscritos=" + inscritos;
        }
        return "redirect:/jefe?errorInscripcion=true";
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
                "No se pudo guardar la operación por una restricción de datos del sistema.");
        return "jefe/dashboard";
    }
}
