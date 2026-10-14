package com.ds.securecampus.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Aula;
import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Edificio;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.HorarioClase;
import com.ds.securecampus.model.Kardex;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Rol;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.service.PortalUserDetailsService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SchemaAlignmentRepositoryTests {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CargaAlumnoRepository cargaAlumnoRepository;

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private PortalUserDetailsService portalUserDetailsService;

    @Test
    void loginPostAuthenticatesBcryptPasswordAndRejectsAnIncorrectPassword() throws Exception {
        Rol rol = new Rol();
        rol.setNombreRol("ADMINISTRATIVO");
        entityManager.persist(rol);

        Usuario usuario = new Usuario();
        usuario.setNumeroControl("00280691");
        String passwordHash = "$2b$12$/RoB1xK3r44glIChR7dwv.WLvZ1.8TLifb.ydehkwYdqsR6k9IuUK";
        assertThat(passwordEncoder.matches("PW_22280691", passwordHash)).isTrue();
        usuario.setPasswordHash(passwordHash);
        usuario.setRol(rol);
        usuario.setActivo((byte) 1);
        usuario.setDebeCambiarPass((byte) 1);
        usuario.setNombre("Carlos");
        usuario.setApellidoPaterno("Pérez");
        usuario.setApellidoMaterno("Gómez");
        usuario.setCorreoInstitucional("00280691@escuela.edu.mx");
        entityManager.persist(usuario);
        entityManager.flush();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("numeroControl", "00280691")
                        .param("password", "PW_22280691"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acceso"))
                .andExpect(authenticated().withUsername("00280691"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("numeroControl", "00280691")
                        .param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void repositoriesReadStudentScheduleAndTranscriptFromTheScriptSchema() {
        Rol rol = new Rol();
        rol.setNombreRol("ESTUDIANTE");
        entityManager.persist(rol);

        Usuario usuario = new Usuario();
        usuario.setNumeroControl("12345678");
        usuario.setPasswordHash("encoded-password");
        usuario.setRol(rol);
        usuario.setActivo((byte) 1);
        usuario.setDebeCambiarPass((byte) 0);
        usuario.setNombre("Alicia");
        usuario.setApellidoPaterno("Núñez");
        usuario.setApellidoMaterno("López");
        usuario.setCorreoInstitucional("alicia@example.edu");
        entityManager.persist(usuario);

        Carrera carrera = new Carrera();
        carrera.setClave("ISC");
        carrera.setNombre("Ingeniería en Sistemas");
        entityManager.persist(carrera);

        Estudiante estudiante = new Estudiante();
        estudiante.setNumeroControl(usuario.getNumeroControl());
        estudiante.setUsuario(usuario);
        estudiante.setCarrera(carrera);
        estudiante.setFechaIngreso(LocalDate.of(2026, 1, 1));
        estudiante.setPromedio(new BigDecimal("90.00"));
        estudiante.setPagoReinscripcion("N");
        estudiante.setTieneAdeudos("N");
        entityManager.persist(estudiante);

        Periodo periodo = new Periodo();
        periodo.setClavePeriodo("2026A");
        periodo.setNombre("Enero-Junio 2026");
        periodo.setFechaInicio(LocalDate.of(2026, 1, 1));
        periodo.setFechaFin(LocalDate.of(2026, 12, 31));
        periodo.setLimiteCapturaCalificaciones(LocalDate.of(2026, 12, 31));
        periodo.setCapturaActiva("S");
        entityManager.persist(periodo);

        Materia materia = new Materia();
        materia.setClave("ISC-101");
        materia.setNombre("Programación");
        materia.setCreditos(new BigDecimal("5"));
        materia.setCarrera(carrera);
        entityManager.persist(materia);

        Grupo grupo = new Grupo();
        grupo.setMateria(materia);
        grupo.setPeriodo(periodo);
        grupo.setCodigoGrupo("A1");
        entityManager.persist(grupo);

        Aula aula = new Aula();
        Edificio edificio = new Edificio();
        edificio.setClave("ED01");
        edificio.setNombre("Edificio principal");
        entityManager.persist(edificio);
        aula.setEdificio(edificio);
        aula.setNombreAula("A-101");
        aula.setCapacidad(new BigDecimal("30"));
        aula.setTipoAula("TEORICA");
        entityManager.persist(aula);

        HorarioClase horario = new HorarioClase();
        horario.setGrupo(grupo);
        horario.setAula(aula);
        horario.setDiaSemana((byte) 1);
        horario.setHoraInicio("08:00");
        horario.setHoraFin("10:00");
        entityManager.persist(horario);

        CargaAlumno carga = new CargaAlumno();
        carga.setEstudiante(estudiante);
        carga.setGrupo(grupo);
        carga.setEstatus("CURSANDO");
        carga.setOportunidad("PRIMERA");
        entityManager.persist(carga);

        Kardex kardex = new Kardex();
        kardex.setEstudiante(estudiante);
        kardex.setMateria(materia);
        kardex.setPeriodo(periodo);
        kardex.setCalificacionFinal(new BigDecimal("95.00"));
        kardex.setTipoAcreditacion("ORDINARIO");
        kardex.setEstatus("APROBADA");
        kardex.setCreditosObtenidos(new BigDecimal("5"));
        entityManager.persist(kardex);

        entityManager.flush();
        entityManager.clear();

        var perfil = estudianteRepository.buscarPerfil("12345678").orElseThrow();
        assertThat(perfil.getNombreCompleto()).isEqualTo("Alicia Núñez López");
        assertThat(perfil.getCorreo()).isEqualTo("alicia@example.edu");
        assertThat(portalUserDetailsService.loadUserByUsername("12345678").getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ESTUDIANTE");

        var materias = cargaAlumnoRepository.buscarHorarioActual(
                "12345678", LocalDate.of(2026, 10, 11));
        assertThat(materias).singleElement().satisfies(materiaHorario -> {
            assertThat(materiaHorario.getGrupo()).isEqualTo("A1");
            assertThat(materiaHorario.getHorario()).isEqualTo("Lunes 08:00 - 10:00");
            assertThat(materiaHorario.getSalon()).isEqualTo("A-101");
        });

        var historial = kardexRepository.buscarKardex("12345678");
        assertThat(historial).singleElement().satisfies(materiaKardex -> {
            assertThat(materiaKardex.getCalificacion()).isEqualByComparingTo("95.00");
            assertThat(materiaKardex.getEstatus()).isEqualTo("APROBADA");
            assertThat(materiaKardex.getOportunidad()).isEqualTo("ORDINARIO");
        });
    }
}
