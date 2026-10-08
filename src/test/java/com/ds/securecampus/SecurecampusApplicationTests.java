package com.ds.securecampus;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.service.EstudianteService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurecampusApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstudianteService estudianteService;

    @Test
    void loginPageLoadsWithPortalStylesheet() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu vida académica, en un solo lugar.")))
                .andExpect(content().string(containsString("/css/portal.css?v=4")));
    }

    @Test
    void portalStylesheetIsServedWithInstitutionalPalette() throws Exception {
        mockMvc.perform(get("/css/portal.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(containsString(".login-card")))
                .andExpect(content().string(containsString(".portal-navbar")))
                .andExpect(content().string(containsString("linear-gradient(110deg, var(--navy-dark)")))
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

        assertStudentPage("/estudiante", "Bienvenido/a");
        assertStudentPage("/estudiante/horario", "Mi horario");
        assertStudentPage("/estudiante/kardex", "Mi kárdex");
    }

    private void assertStudentPage(String path, String heading) throws Exception {
        mockMvc.perform(get(path).with(SecurityMockMvcRequestPostProcessors.user("22280691").roles("ESTUDIANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(heading)))
                .andExpect(content().string(containsString("href=\"/css/portal.css?v=4\"")))
                .andExpect(content().string(containsString("INSTITUTO TECNOLÓGICO DE TOLUCA")))
                .andExpect(content().string(containsString("href=\"/estudiante/horario\"")))
                .andExpect(content().string(containsString("href=\"/estudiante/kardex\"")));
    }

}
