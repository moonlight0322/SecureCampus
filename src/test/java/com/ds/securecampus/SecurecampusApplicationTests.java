package com.ds.securecampus;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ds.securecampus.model.CargaAcademicaMateria;
import com.ds.securecampus.model.CalificacionMateria;
import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.HorarioSemanalFila;
import com.ds.securecampus.model.HorarioClaseEstudiante;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.service.EstudianteService;
import com.ds.securecampus.service.ProfesorService;
import com.ds.securecampus.repository.UsuarioRepository;
import com.ds.securecampus.service.AdminService;
import com.ds.securecampus.service.SolicitudService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurecampusApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EstudianteService estudianteService;

    @MockitoBean
    private ProfesorService profesorService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private SolicitudService solicitudService;

    @Test
    void loginPageLoadsWithPortalStylesheet() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu información académica, en un solo lugar.")))
                .andExpect(content().string(containsString("/css/portal.css?v=5")));
    }

    @Test
    void loginOnlyShowsBlockedAccountNoticeForBlockedParameter() throws Exception {
        mockMvc.perform(get("/login?logout"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu sesión se cerró correctamente.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("Cuenta bloqueada temporalmente:"))));

        mockMvc.perform(get("/login?blocked=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cuenta bloqueada temporalmente:")))
                .andExpect(content().string(containsString("límite de 5 intentos fallidos")));
    }

    @Test
    void authenticatedRootRoutesThroughRoleAwareAccess() throws Exception {
        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000101").roles("ESTUDIANTE")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acceso"));
    }

    @Test
    void portalStylesheetIsServedWithInstitutionalPalette() throws Exception {
        mockMvc.perform(get("/css/portal.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(containsString(".login-card")))
                .andExpect(content().string(containsString(".portal-navbar")))
                .andExpect(content().string(containsString("linear-gradient(135deg, var(--navy-dark)")))
                .andExpect(content().string(containsString(".profile-banner")));
    }

    @Test
    void everyStudentPageRendersSharedNavigationAndStyles() throws Exception {
        when(estudianteService.obtenerPerfil("22280691"))
                .thenReturn(new PerfilEstudiante("22280691", "Montserrat Hernández Fabián",
                        "L22280691@toluca.tecnm.mx", "Ingeniería en Sistemas Computacionales",
                        new BigDecimal("86.00")));
        when(estudianteService.obtenerCargaActual("22280691"))
                .thenReturn(List.of(new CargaAcademicaMateria("Programación", "ISC-101", "A1",
                        "Docente de prueba", "Ago-Dic 2026")));
        when(estudianteService.obtenerClasesHoy("22280691"))
                .thenReturn(List.of(
                        new HorarioClaseEstudiante((byte) 4, "11:00", "13:00", "Estructura de Datos", "E-3"),
                        new HorarioClaseEstudiante((byte) 4, "13:00", "15:00", "Fundamentos de Programación", "E-2"),
                        new HorarioClaseEstudiante((byte) 4, "15:00", "16:00", "Ecuaciones Diferenciales", "U-3")));
        HorarioSemanalFila filaHorario = new HorarioSemanalFila("08:00 - 10:00");
        filaHorario.agregarClase((byte) 1, "Programación · Aula 12");
        when(estudianteService.obtenerHorarioSemanal("22280691")).thenReturn(List.of(filaHorario));
        when(estudianteService.obtenerCalificacionesActuales("22280691"))
                .thenReturn(List.of(new CalificacionMateria("Programación", "ISC-101", "A1",
                        "Ago-Dic 2026", null, new BigDecimal("90.00"), null)));
        when(estudianteService.obtenerKardex("22280691"))
                .thenReturn(List.of(new MateriaKardex("Programación", "ISC-101",
                        "Ago-Dic 2026", new BigDecimal("95.00"), "APROBADA", "PRIMERA")));

        assertStudentPage("/estudiante", "BIENVENIDO/A HALCÓN");
        assertStudentPage("/estudiante/horario", "Horario escolar");
        assertStudentPage("/estudiante/carga", "Carga académica");
        assertStudentPage("/estudiante/calificaciones", "Calificaciones por materia y unidad");
        assertStudentPage("/estudiante/kardex", "Cárdex");
        mockMvc.perform(get("/estudiante")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(content().string(containsString("Estructura de Datos")))
                .andExpect(content().string(containsString("Fundamentos de Programación")))
                .andExpect(content().string(containsString("Ecuaciones Diferenciales")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("No hay clases programadas para el día de hoy."))));
        mockMvc.perform(get("/estudiante/horario")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(content().string(containsString("Lunes")))
                .andExpect(content().string(containsString("Programación · Aula 12")));
        mockMvc.perform(get("/estudiante/calificaciones")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(content().string(containsString("<td>90.00</td>")))
                .andExpect(content().string(containsString("<td></td>")));
    }

    @Test
    void authenticatedUsersCanOpenPasswordChangePage() throws Exception {
        mockMvc.perform(get("/cuenta/contrasena")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contraseña actual")))
                .andExpect(content().string(containsString("Volver al panel")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Cancelar"))));
    }

    @Test
    void administrativeAndDepartmentHeadDashboardRoutesRenderForTheirRoles() throws Exception {
        Usuario administrativo = new Usuario();
        administrativo.setNumeroControl("99000002");
        administrativo.setNombre("Admin");
        administrativo.setApellidoPaterno("Prueba");
        com.ds.securecampus.model.Rol rolAdmin = new com.ds.securecampus.model.Rol();
        rolAdmin.setNombreRol("ADMINISTRATIVO");
        administrativo.setRol(rolAdmin);
        Usuario usuarioActivo = new Usuario();
        usuarioActivo.setNumeroControl("99000005");
        usuarioActivo.setNombre("Usuario");
        usuarioActivo.setApellidoPaterno("Activo");
        usuarioActivo.setCorreoInstitucional("activo@example.edu");
        usuarioActivo.setActivo((byte) 1);
        usuarioActivo.setRol(rolAdmin);
        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setNumeroControl("99000006");
        usuarioInactivo.setNombre("Usuario");
        usuarioInactivo.setApellidoPaterno("Inactivo");
        usuarioInactivo.setCorreoInstitucional("inactivo@example.edu");
        usuarioInactivo.setActivo((byte) 0);
        usuarioInactivo.setRol(rolAdmin);
        SolicitudTramite solicitudPendiente = new SolicitudTramite();
        solicitudPendiente.setIdSolicitud(21L);
        solicitudPendiente.setFolio("FOL-PRUEBA-21");
        solicitudPendiente.setNumeroControl("22280691");
        solicitudPendiente.setTipoTramite("CONSTANCIA");
        solicitudPendiente.setMotivo("Solicitud de prueba");
        SolicitudTramite solicitudResuelta = new SolicitudTramite();
        solicitudResuelta.setIdSolicitud(22L);
        solicitudResuelta.setFolio("FOL-PRUEBA-22");
        solicitudResuelta.setNumeroControl("22280692");
        solicitudResuelta.setTipoTramite("BAJA");
        solicitudResuelta.setEstatus("APROBADA");
        solicitudResuelta.setRespuestaAdmin("Resuelta");
        LogAuditoria log = new LogAuditoria();
        log.setNumeroControl("99000002");
        log.setRol("ADMINISTRATIVO");
        log.setOperacion("PRUEBA");
        log.setDetalles("Registro de auditoría de prueba");
        log.setIpOrigen("127.0.0.1");
        Carrera carrera = new Carrera();
        carrera.setCarreraId(3L);
        carrera.setNombre("Ingeniería de prueba");
        Materia materia = new Materia();
        materia.setClave("ISC-TEST");
        materia.setNombre("Materia de prueba");
        materia.setCreditos(new BigDecimal("6"));
        materia.setCarrera(carrera);
        Usuario jefe = new Usuario();
        jefe.setNumeroControl("99000003");
        jefe.setNombre("Jefe");
        jefe.setApellidoPaterno("Prueba");
        when(adminService.obtenerUsuario("99000002")).thenReturn(administrativo);
        when(adminService.obtenerUsuario("99000003")).thenReturn(jefe);
        when(adminService.listarUsuarios()).thenReturn(List.of(usuarioActivo, usuarioInactivo));
        when(adminService.listarAuditoria()).thenReturn(List.of(log));
        when(solicitudService.obtenerTodas()).thenReturn(List.of(solicitudPendiente, solicitudResuelta));
        when(adminService.listarRoles()).thenReturn(List.of());
        when(adminService.listarMaterias()).thenReturn(List.of(materia));
        Usuario perfilDocente = new Usuario();
        perfilDocente.setNombre("Ana");
        perfilDocente.setApellidoPaterno("Docente");
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("99000004");
        profesor.setUsuario(perfilDocente);
        when(adminService.listarProfesores()).thenReturn(List.of(profesor));
        when(adminService.listarCarreras()).thenReturn(List.of(carrera));
        when(adminService.listarGruposPeriodoActual()).thenReturn(List.of());
        when(adminService.listarEstudiantes()).thenReturn(List.of());
        when(adminService.listarNombresAulas()).thenReturn(List.of("B2-1"));
        when(adminService.periodoAsignacionActual(org.mockito.ArgumentMatchers.any()))
                .thenReturn("Enero - Junio 2026");

        mockMvc.perform(get("/admin")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000002").roles("ADMINISTRATIVO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel de Administración y Seguridad")));
        mockMvc.perform(get("/admin/dashboard")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000002").roles("ADMINISTRATIVO")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        mockMvc.perform(get("/admin")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000002").roles("ADMINISTRATIVO")))
                .andExpect(content().string(containsString(">Ana Docente</option>")))
                .andExpect(content().string(containsString(">Desactivar</span>")))
                .andExpect(content().string(containsString(">Activar</span>")))
                .andExpect(content().string(containsString("FOL-PRUEBA-21")))
                .andExpect(content().string(containsString("id=\"modalAprobar21\"")))
                .andExpect(content().string(containsString("Resuelta")))
                .andExpect(content().string(containsString("Registro de auditoría de prueba")))
                .andExpect(content().string(containsString("value=\"3\">Ingeniería de prueba</option>")))
                .andExpect(content().string(containsString("data-bs-target=\"#modalNuevoUsuario\"")))
                .andExpect(content().string(containsString("data-bs-target=\"#tab-auditoria\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("99000004 - Ana Docente"))));
        mockMvc.perform(get("/jefe")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000003").roles("JEFE_DEPARTAMENTO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de la oferta académica")))
                .andExpect(content().string(containsString(">Ana Docente</option>")));
        mockMvc.perform(get("/jefe/dashboard")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000003").roles("JEFE_DEPARTAMENTO")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000003").roles("JEFE_DEPARTAMENTO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void groupAssignmentEndpointsCreateTheScheduledOfferInOneOperation() throws Exception {
        mockMvc.perform(post("/admin/grupos/asignar")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000002").roles("ADMINISTRATIVO"))
                        .with(csrf())
                        .param("codigoGrupo", "ISC-201-A")
                        .param("materiaId", "12")
                        .param("profesorControl", "99000004")
                        .param("patronHorario", "TR")
                        .param("horaInicio", "09:00")
                        .param("aula", "B2-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?grupoAsignado=true"));
        verify(adminService).crearGrupoAsignacion(
                "ISC-201-A", 12L, "99000004", "TR", "09:00", null, "B2-1", "99000002");

        mockMvc.perform(post("/jefe/grupos/asignar")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000003").roles("JEFE_DEPARTAMENTO"))
                        .with(csrf())
                        .param("codigoGrupo", "ISC-201-B")
                        .param("materiaId", "12")
                        .param("profesorControl", "99000004")
                        .param("patronHorario", "MW")
                        .param("horaInicio", "11:00")
                        .param("aula", "B2-2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jefe?asignado=true"));
        verify(adminService).crearGrupoAsignacion(
                "ISC-201-B", 12L, "99000004", "MW", "11:00", null, "B2-2", "99000003");
    }

    @Test
    void passwordChangeChecksCurrentPasswordAndStoresOnlyTheHashForTheSignedInAccount() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("22280691");
        usuario.setPasswordHash(passwordEncoder.encode("ClaveActualSegura2026!"));
        when(usuarioRepository.findById("22280691")).thenReturn(java.util.Optional.of(usuario));

        mockMvc.perform(post("/cuenta/contrasena")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE"))
                        .with(csrf())
                        .param("contrasenaActual", "ClaveActualSegura2026!")
                        .param("nuevaContrasena", "NuevaClaveSegura2026!")
                        .param("confirmarContrasena", "NuevaClaveSegura2026!"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("se actualizó correctamente")));

        verify(usuarioRepository).save(usuario);
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(
                "NuevaClaveSegura2026!", usuario.getPasswordHash())).isTrue();
        org.assertj.core.api.Assertions.assertThat(usuario.getDebeCambiarPass()).isZero();
    }

    @Test
    void teacherHomeRendersUsingThePersistedTeacherProfileShape() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Docente");
        usuario.setApellidoPaterno("De Prueba");
        Carrera carrera = new Carrera();
        carrera.setNombre("Ingeniería en Sistemas Computacionales");
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("99000001");
        profesor.setUsuario(usuario);
        profesor.setCarreraAdscripcion(carrera);

        when(profesorService.obtenerProfesor("99000001")).thenReturn(profesor);
        when(profesorService.obtenerGruposProfesor("99000001")).thenReturn(List.of());

        mockMvc.perform(get("/profesor")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000001").roles("PROFESOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Docente De Prueba")))
                .andExpect(content().string(containsString("99000001")))
                .andExpect(content().string(containsString("Ingeniería en Sistemas Computacionales")))
                .andExpect(content().string(containsString("No hay clases programadas para hoy.")));
    }

    @Test
    void teacherGroupsRouteRendersAssignedGroupsView() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Docente");
        usuario.setApellidoPaterno("De Prueba");
        Carrera carrera = new Carrera();
        carrera.setNombre("Ingeniería en Sistemas Computacionales");
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("99000001");
        profesor.setUsuario(usuario);
        profesor.setCarreraAdscripcion(carrera);

        when(profesorService.obtenerProfesor("99000001")).thenReturn(profesor);
        when(profesorService.obtenerGruposProfesor("99000001")).thenReturn(List.of());

        mockMvc.perform(get("/profesor/grupos")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000001").roles("PROFESOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Grupos")))
                .andExpect(content().string(containsString("No tienes grupos asignados actualmente.")));
    }

    @Test
    void approvedCaptureUnlockDisplaysWorkingGradeFormForAssignedProfessor() throws Exception {
        Carrera carrera = new Carrera();
        carrera.setNombre("Sistemas");
        Usuario profesorUsuario = new Usuario();
        profesorUsuario.setNombre("Docente");
        profesorUsuario.setApellidoPaterno("Prueba");
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("99000001");
        profesor.setUsuario(profesorUsuario);
        profesor.setCarreraAdscripcion(carrera);
        Materia materia = new Materia();
        materia.setNombre("Programación");
        Grupo grupo = new Grupo();
        grupo.setGrupoId(41L);
        grupo.setCodigoGrupo("A1");
        grupo.setMateria(materia);
        grupo.setProfesor(profesor);
        Periodo periodo = new Periodo();
        periodo.setNombre("Enero-Junio");
        grupo.setPeriodo(periodo);

        Usuario estudianteUsuario = new Usuario();
        estudianteUsuario.setNombre("Estudiante");
        estudianteUsuario.setApellidoPaterno("Prueba");
        Estudiante estudiante = new Estudiante();
        estudiante.setNumeroControl("22280691");
        estudiante.setUsuario(estudianteUsuario);
        CargaAlumno carga = new CargaAlumno();
        carga.setCargaId(88L);
        carga.setEstudiante(estudiante);
        carga.setGrupo(grupo);
        carga.setCalificacionParcial1(new BigDecimal("85"));
        when(profesorService.obtenerGrupoProfesor(41L, "99000001")).thenReturn(grupo);
        when(profesorService.obtenerAlumnosGrupo(41L)).thenReturn(List.of(carga));
        when(profesorService.obtenerProfesor("99000001")).thenReturn(profesor);
        when(profesorService.puedeCapturar(grupo)).thenReturn(true);

        mockMvc.perform(get("/profesor/grupo/41")
                        .with(SecurityMockMvcRequestPostProcessors.user("99000001").roles("PROFESOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"p1\"")))
                .andExpect(content().string(containsString("name=\"p2\"")))
                .andExpect(content().string(containsString("form=\"calificar-88\"")))
                .andExpect(content().string(containsString("action=\"/profesor/grupo/41/calificar\"")))
                .andExpect(content().string(containsString("Guardar")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("disabled=\"disabled\""))));
    }

    private void assertStudentPage(String path, String heading) throws Exception {
        mockMvc.perform(get(path).with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(heading)))
                .andExpect(content().string(containsString("href=\"/css/portal.css?v=7\"")))
                .andExpect(content().string(containsString("Instituto Tecnológico de Toluca")));
    }

}
