package com.ds.securecampus.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.service.DocumentoPdfService;
import com.ds.securecampus.service.ProfesorService;
import com.ds.securecampus.service.SolicitudService;

@Controller
@RequestMapping("/profesor")
public class ProfesorController {
    private final ProfesorService profesorService;
    private final DocumentoPdfService documentoPdfService;
    private final SolicitudService solicitudService;

    public ProfesorController(ProfesorService profesorService,
                              DocumentoPdfService documentoPdfService,
                              SolicitudService solicitudService) {
        this.profesorService = profesorService;
        this.documentoPdfService = documentoPdfService;
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public String inicioProfesor(Authentication authentication, Model model) {
        String numControl = authentication.getName();
        List<Grupo> grupos = profesorService.obtenerGruposProfesor(numControl);
        model.addAttribute("profesor", profesorService.obtenerProfesor(numControl));
        model.addAttribute("grupos", grupos);
        model.addAttribute("clasesHoy", List.of());
        return "profesor/inicio";
    }

    @GetMapping("/grupos")
    public String gruposProfesor(Authentication authentication, Model model) {
        String numeroControl = authentication.getName();
        model.addAttribute("profesor", profesorService.obtenerProfesor(numeroControl));
        model.addAttribute("listaGrupos", profesorService.obtenerGruposProfesor(numeroControl));
        return "profesor/grupos";
    }

    @GetMapping("/grupo/{id}")
    public String verGrupo(@PathVariable("id") Long grupoId, Authentication authentication, Model model) {
        Grupo grupo = profesorService.obtenerGrupoProfesor(grupoId, authentication.getName());
        List<CargaAlumno> alumnos = profesorService.obtenerAlumnosGrupo(grupoId);
        boolean puedeEditar = profesorService.puedeCapturar(grupo);

        model.addAttribute("profesor", profesorService.obtenerProfesor(authentication.getName()));
        model.addAttribute("grupo", grupo);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("puedeEditar", puedeEditar);
        return "profesor/grupo";
    }

    @PostMapping("/grupo/{id}/calificar")
    public String calificar(@PathVariable("id") Long grupoId,
                            @RequestParam("cargaId") Long cargaId,
                            @RequestParam(value = "p1", required = false) BigDecimal p1,
                            @RequestParam(value = "p2", required = false) BigDecimal p2,
                            Authentication authentication) {
        profesorService.obtenerGrupoProfesor(grupoId, authentication.getName());
        profesorService.guardarCalificacion(grupoId, cargaId, p1, p2, authentication.getName());
        return "redirect:/profesor/grupo/" + grupoId + "?guardado=true";
    }

    @PostMapping("/grupo/{id}/solicitar-desbloqueo")
    public String solicitarDesbloqueo(@PathVariable("id") Long grupoId,
                                      @RequestParam("motivo") String motivo,
                                      Authentication authentication) {
        solicitudService.crearSolicitud(authentication.getName(), "DESBLOQUEO_CAPTURA", motivo, grupoId);
        return "redirect:/profesor/grupo/" + grupoId + "?solicitudEnviada=true";
    }

    @GetMapping("/grupo/{id}/pdf")
    public ResponseEntity<byte[]> exportarListaPdf(@PathVariable("id") Long grupoId, Authentication authentication) {
        Grupo grupo = profesorService.obtenerGrupo(grupoId);
        List<CargaAlumno> alumnos = profesorService.obtenerAlumnosGrupo(grupoId);

        String profesorNombre = (grupo.getProfesor() != null && grupo.getProfesor().getUsuario() != null)
                ? grupo.getProfesor().getUsuario().getNombre() + " " + grupo.getProfesor().getUsuario().getApellidoPaterno()
                : "Docente";

        byte[] pdf = documentoPdfService.generarListaGrupoPdf(
                grupoId, 
                grupo.getMateria().getNombre(), 
                grupo.getCodigoGrupo(), 
                profesorNombre, 
                alumnos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Lista_" + grupo.getCodigoGrupo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
