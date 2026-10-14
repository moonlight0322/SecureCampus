package com.ds.securecampus;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.model.Rol;
import com.ds.securecampus.model.SolicitudTramite;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.service.AdminService;
import com.ds.securecampus.service.CuentaService;
import com.ds.securecampus.service.EstudianteService;
import com.ds.securecampus.service.SolicitudService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurecampusApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstudianteService estudianteService;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private SolicitudService solicitudService;

    @MockitoBean
    private CuentaService cuentaService;

    @Value("${server.servlet.session.timeout}")
    private java.time.Duration sessionTimeout;

    @Test
    void loginPageLoadsWithPortalStylesheet() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu información académica, en un solo lugar.")))
                .andExpect(content().string(containsString("/css/portal.css?v=6")));
    }

    @Test
    void sessionsExpireAfterTenMinutesOfInactivity() throws Exception {
        org.assertj.core.api.Assertions.assertThat(sessionTimeout).isEqualTo(java.time.Duration.ofMinutes(10));
        mockMvc.perform(get("/login?expired"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("10 minutos sin actividad")));
    }

    @Test
    void portalStylesheetIsServedWithInstitutionalPalette() throws Exception {
        mockMvc.perform(get("/css/portal.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(containsString(".login-card")))
                .andExpect(content().string(containsString(".portal-navbar")))
                .andExpect(content().string(containsString(".student-id-badge")))
                .andExpect(content().string(containsString(".main-sheet")))
                .andExpect(content().string(containsString("linear-gradient(135deg, var(--navy-dark)")))
                .andExpect(content().string(containsString(".profile-banner")));
    }

    @Test
    void everyStudentPageRendersSharedNavigationAndStyles() throws Exception {
        when(estudianteService.obtenerPerfil("22280691"))
                .thenReturn(new PerfilEstudiante("22280691", "Montserrat Hernández Fabián",
                        "L22280691@toluca.tecnm.mx", "Ingeniería en Sistemas Computacionales",
                        new BigDecimal("86.00")));
        when(estudianteService.obtenerHorario("22280691"))
                .thenReturn(List.of(new MateriaHorario("Programación", "ISC-101", "A1",
                        "Aula 12", "Lunes 08:00", "Docente de prueba", "Ago-Dic 2026")));
        when(estudianteService.obtenerKardex("22280691"))
                .thenReturn(List.of(new MateriaKardex("Programación", "ISC-101",
                        "Ago-Dic 2026", new BigDecimal("95.00"), "APROBADA", "PRIMERA")));

        assertStudentPage("/estudiante", "Resumen académico");
        assertStudentPage("/estudiante/horario", "Mi horario");
        assertStudentPage("/estudiante/kardex", "Mi kárdex");
    }

    private void assertStudentPage(String path, String heading) throws Exception {
        mockMvc.perform(get(path).with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(heading)))
                .andExpect(content().string(containsString("href=\"/css/portal.css?v=6\"")))
                .andExpect(content().string(containsString("class=\"student-id-badge\"")))
                .andExpect(content().string(containsString("INSTITUTO TECNOLÓGICO DE TOLUCA")))
                .andExpect(content().string(containsString("href=\"/estudiante/horario\"")))
                .andExpect(content().string(containsString("href=\"/estudiante/kardex\"")));
    }

    @Test
    void professorSchedulePageIsAvailableToProfessors() throws Exception {
        mockMvc.perform(get("/profesor/horario")
                        .with(SecurityMockMvcRequestPostProcessors.user("00112233").roles("PROFESOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mi horario")))
                .andExpect(content().string(containsString("No hay horarios capturados")));
    }

    @Test
    void authenticatedUsersCanOpenPasswordChangeForm() throws Exception {
        mockMvc.perform(get("/cuenta/contrasena")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cambiar contraseña")))
                .andExpect(content().string(containsString("22280691")))
                .andExpect(content().string(containsString("passwordActual")))
                .andExpect(content().string(containsString("Cancelar y volver al inicio")))
                .andExpect(content().string(containsString("/acceso")));
    }

    @Test
    void passwordChangeUsesTheAuthenticatedAccountAndNeverARequestSuppliedIdentity() throws Exception {
        mockMvc.perform(post("/cuenta/contrasena")
                        .with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("passwordActual", "ContrasenaActual123")
                        .param("passwordNueva", "ContrasenaNueva456")
                        .param("confirmarPassword", "ContrasenaNueva456")
                        .param("numeroControl", "99999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/cuenta/contrasena?actualizada"));

        verify(cuentaService).cambiarPassword("22280691", "ContrasenaActual123",
                "ContrasenaNueva456", "ContrasenaNueva456");
    }

    @Test
    void administrationActionsAreConnectedToTheirServices() throws Exception {
        var admin = SecurityMockMvcRequestPostProcessors.user("10280070").roles("ADMINISTRADOR");
        var csrf = SecurityMockMvcRequestPostProcessors.csrf();

        mockMvc.perform(post("/admin/usuarios/crear").with(admin).with(csrf)
                        .param("numeroControl", "22289999")
                        .param("password", "ContrasenaInicial123")
                        .param("nombre", "Prueba")
                        .param("apellidoPaterno", "Estudiante")
                        .param("correoInstitucional", "prueba@toluca.tecnm.mx")
                        .param("rolId", "3")
                        .param("carreraId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/admin?creado=true"));
        verify(adminService).crearUsuario("22289999", "ContrasenaInicial123", "Prueba",
                "Estudiante", null, "prueba@toluca.tecnm.mx", 3L, 1L, null, null, "10280070");

        mockMvc.perform(post("/admin/grupos/asignar").with(admin).with(csrf)
                        .param("codigoGrupo", "SC-801")
                        .param("materiaId", "15")
                        .param("profesorControl", "10280071")
                        .param("periodoId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/admin?grupoAsignado=true"));
        verify(adminService).crearGrupoAsignacion("SC-801", 15L, "10280071", 2L, "10280070");

        mockMvc.perform(post("/admin/usuarios/restablecer-password").with(admin).with(csrf)
                        .param("numeroControl", "22280691")
                        .param("password", "ContrasenaTemporal123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/admin?passwordRestablecida=true"));
        verify(adminService).restablecerPassword("22280691", "ContrasenaTemporal123", "10280070");

        mockMvc.perform(post("/admin/solicitudes/resolver").with(admin).with(csrf)
                        .param("idSolicitud", "42")
                        .param("estatus", "APROBADA")
                        .param("respuesta", "Solicitud autorizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/admin?solicitudProcesada=true&tab=solicitudes"));
        verify(solicitudService).resolverSolicitud(42L, "APROBADA",
                "Solicitud autorizada", "10280070");
    }

    @Test
    void logoutClearsTheSessionCookie() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/logout")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .sessionAttr("sessionData", "active"))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().maxAge("JSESSIONID", 0));
    }

    @Test
    void administrationPanelsRenderTheirActionForms() throws Exception {
        Rol rol = new Rol();
        rol.setRolId(1L);
        rol.setNombreRol("ADMINISTRADOR");
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("10000001");
        usuario.setNombre("Usuario");
        usuario.setApellidoPaterno("De Prueba");
        usuario.setCorreoInstitucional("usuario@toluca.tecnm.mx");
        usuario.setRol(rol);
        usuario.setActivo((byte) 1);
        when(adminService.listarUsuarios()).thenReturn(List.of(usuario));
        when(adminService.listarRoles()).thenReturn(List.of(rol));

        Carrera carrera = new Carrera(1L, "ISC", "Ingeniería en Sistemas Computacionales");
        when(adminService.listarCarreras()).thenReturn(List.of(carrera));

        Materia materia = new Materia();
        materia.setMateriaId(15L);
        materia.setClave("ISC-201");
        materia.setNombre("Programación");
        materia.setCarrera(carrera);
        when(adminService.listarMaterias()).thenReturn(List.of(materia));

        Profesor profesor = new Profesor();
        profesor.setNumeroControl("10000002");
        Usuario profesorUsuario = new Usuario();
        profesorUsuario.setNombre("Docente");
        profesorUsuario.setApellidoPaterno("De Prueba");
        profesor.setUsuario(profesorUsuario);
        when(adminService.listarProfesores()).thenReturn(List.of(profesor));

        Periodo periodo = new Periodo();
        periodo.setPeriodoId(2L);
        periodo.setNombre("Enero-Junio 2026");
        when(adminService.listarPeriodos()).thenReturn(List.of(periodo));

        LogAuditoria log = new LogAuditoria();
        log.setNumeroControl("10000001");
        log.setRol("ADMINISTRADOR");
        log.setOperacion("INICIO_SESION");
        log.setDetalles("Acceso de prueba");
        log.setIpOrigen("127.0.0.1");
        when(adminService.listarAuditoria()).thenReturn(List.of(log));

        SolicitudTramite solicitud = new SolicitudTramite();
        solicitud.setIdSolicitud(42L);
        solicitud.setFolio("REQ-42");
        solicitud.setNumeroControl("22280691");
        solicitud.setTipoTramite("CONSTANCIA");
        solicitud.setMotivo("Prueba de solicitud");
        when(solicitudService.obtenerTodas()).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/admin")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("modalNuevoUsuario")))
                .andExpect(content().string(containsString("/admin/usuarios/crear")))
                .andExpect(content().string(containsString("/admin/grupos/asignar")))
                .andExpect(content().string(containsString("/admin/usuarios/restablecer-password")))
                .andExpect(content().string(containsString("ISC-201 - Programación")))
                .andExpect(content().string(containsString("10000002 - Docente De Prueba")))
                .andExpect(content().string(containsString("Enero-Junio 2026")))
                .andExpect(content().string(containsString("10000001")))
                .andExpect(content().string(containsString("INICIO_SESION")))
                .andExpect(content().string(containsString("Prueba de solicitud")))
                .andExpect(content().string(containsString("data-bs-target=\"#modalAprobar42\"")))
                .andExpect(content().string(containsString("id=\"modalAprobar42\"")))
                .andExpect(content().string(containsString("Resolver REQ-42")));

        mockMvc.perform(get("/admin?tab=solicitudes")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"solicitudes-tab\"")))
                .andExpect(content().string(containsString("aria-selected=\"true\"")));

        mockMvc.perform(get("/jefe")
                        .with(SecurityMockMvcRequestPostProcessors.user("jefe").roles("JEFE_DEPARTAMENTO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/jefe/materias/alta")))
                .andExpect(content().string(containsString("/jefe/grupos/asignar")));
    }

}
