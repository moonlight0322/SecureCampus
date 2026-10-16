package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Aula;
import com.ds.securecampus.model.Edificio;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.model.Rol;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.CarreraRepository;
import com.ds.securecampus.repository.AulaRepository;
import com.ds.securecampus.repository.CargaAlumnoRepository;
import com.ds.securecampus.repository.EdificioRepository;
import com.ds.securecampus.repository.EstudianteRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.HorarioClaseRepository;
import com.ds.securecampus.repository.LogAuditoriaRepository;
import com.ds.securecampus.repository.MateriaRepository;
import com.ds.securecampus.repository.PeriodoRepository;
import com.ds.securecampus.repository.ProfesorRepository;
import com.ds.securecampus.repository.RolRepository;
import com.ds.securecampus.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private MateriaRepository materiaRepository;
    @Mock private CarreraRepository carreraRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private ProfesorRepository profesorRepository;
    @Mock private PeriodoRepository periodoRepository;
    @Mock private LogAuditoriaRepository logAuditoriaRepository;
    @Mock private AuditoriaService auditoriaService;
    @Mock private AulaRepository aulaRepository;
    @Mock private EdificioRepository edificioRepository;
    @Mock private HorarioClaseRepository horarioClaseRepository;
    @Mock private CargaAlumnoRepository cargaAlumnoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(usuarioRepository, rolRepository, materiaRepository,
                carreraRepository, estudianteRepository, grupoRepository, profesorRepository,
                periodoRepository, logAuditoriaRepository, auditoriaService, passwordEncoder,
                aulaRepository, edificioRepository, horarioClaseRepository, cargaAlumnoRepository);
    }

    @Test
    void creatingStudentAccountAlsoCreatesRequiredAcademicProfile() {
        Rol estudianteRol = new Rol(3L, "ESTUDIANTE");
        Carrera carrera = new Carrera(1L, "ISC", "Ingeniería en Sistemas");
        when(usuarioRepository.findByNumeroControl("12345678")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreoInstitucional("alumno@example.edu")).thenReturn(false);
        when(rolRepository.findById(3L)).thenReturn(Optional.of(estudianteRol));
        when(carreraRepository.findById(1L)).thenReturn(Optional.of(carrera));

        adminService.crearUsuario("12345678", "ContrasenaInicial123!", "Ana", "Pérez",
                "López", "alumno@example.edu", 3L, 1L, null, null, "admin");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(usuarioCaptor.capture());
        assertThat(passwordEncoder.matches("ContrasenaInicial123!",
                usuarioCaptor.getValue().getPasswordHash())).isTrue();

        ArgumentCaptor<Estudiante> estudianteCaptor = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepository).saveAndFlush(estudianteCaptor.capture());
        assertThat(estudianteCaptor.getValue().getUsuario().getNumeroControl()).isEqualTo("12345678");
        assertThat(estudianteCaptor.getValue().getUsuario()).isSameAs(usuarioCaptor.getValue());
        assertThat(estudianteCaptor.getValue().getCarrera()).isSameAs(carrera);
        verify(auditoriaService).registrar(org.mockito.ArgumentMatchers.eq("admin"),
                org.mockito.ArgumentMatchers.eq("ADMINISTRADOR"),
                org.mockito.ArgumentMatchers.eq("ALTA_USUARIO"),
                org.mockito.ArgumentMatchers.contains("12345678"),
                org.mockito.ArgumentMatchers.eq("127.0.0.1"));
    }

    @Test
    void togglingUserStatusSavesAndAuditsTheNewStatus() {
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("12345678");
        usuario.setActivo((byte) 1);
        when(usuarioRepository.findByNumeroControl("12345678")).thenReturn(Optional.of(usuario));

        adminService.alternarEstatusUsuario("12345678", "admin");

        assertThat(usuario.getActivo()).isZero();
        verify(usuarioRepository).save(usuario);
        verify(auditoriaService).registrar("admin", "ADMINISTRADOR",
                "MODIFICAR_ESTATUS_USUARIO", "Usuario 12345678 estatus cambiado a INACTIVO", "127.0.0.1");
    }

    @Test
    void togglingUnknownUserReturnsAnExplicitError() {
        when(usuarioRepository.findByNumeroControl("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.alternarEstatusUsuario("missing", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado.");
    }

    @Test
    void resettingPasswordStoresAHashAndRequiresChangeAtNextLogin() {
        Usuario usuario = new Usuario();
        usuario.setNumeroControl("12345678");
        when(usuarioRepository.findByNumeroControl("12345678")).thenReturn(Optional.of(usuario));

        adminService.restablecerPassword("12345678", "ContrasenaTemporal123!", "admin");

        assertThat(passwordEncoder.matches("ContrasenaTemporal123!", usuario.getPasswordHash())).isTrue();
        assertThat(usuario.getDebeCambiarPass()).isEqualTo((byte) 1);
        verify(usuarioRepository).save(usuario);
        verify(auditoriaService).registrar("admin", "ADMINISTRADOR", "RESTABLECER_PASSWORD",
                "Contraseña restablecida para usuario 12345678", "127.0.0.1");
    }

    @Test
    void creatingGroupConnectsItsMatterPeriodAndOptionalProfessor() {
        Carrera carrera = new Carrera(1L, "ISC", "Ingeniería en Sistemas");
        Materia materia = new Materia(15L, "ISC-201", "Programación", BigDecimal.valueOf(5), carrera);
        Periodo periodo = new Periodo(2L, "AD2026", "Agosto - Diciembre 2026",
                java.time.LocalDate.of(2026, 8, 10), java.time.LocalDate.of(2026, 12, 14),
                java.time.LocalDate.of(2026, 12, 14), "N");
        Usuario perfilProfesor = new Usuario();
        perfilProfesor.setNombre("Luis");
        Profesor profesor = new Profesor();
        profesor.setNumeroControl("profesor-1");
        profesor.setUsuario(perfilProfesor);
        Edificio edificio = new Edificio();
        edificio.setClave("B2");
        Aula aula = new Aula();
        aula.setEdificio(edificio);
        aula.setNombreAula("B2-1");
        when(periodoRepository.findByClavePeriodoForUpdate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(periodo));
        when(materiaRepository.findById(15L)).thenReturn(Optional.of(materia));
        when(profesorRepository.findById("profesor-1")).thenReturn(Optional.of(profesor));
        when(aulaRepository.findByEdificioClaveAndNombreAula("B2", "B2-1"))
                .thenReturn(Optional.of(aula));
        when(grupoRepository.findByCodigoGrupoAndMateriaMateriaIdAndPeriodoPeriodoId(
                "SC-801", 15L, 2L)).thenReturn(Optional.empty());
        when(grupoRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Grupo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adminService.crearGrupoAsignacion(
                "SC-801", 15L, "profesor-1", "TR", "09:00", null, "B2-1", "admin");

        ArgumentCaptor<Grupo> grupoCaptor = ArgumentCaptor.forClass(Grupo.class);
        verify(grupoRepository).saveAndFlush(grupoCaptor.capture());
        verify(horarioClaseRepository, org.mockito.Mockito.times(2))
                .save(org.mockito.ArgumentMatchers.any());
        assertThat(grupoCaptor.getValue().getCodigoGrupo()).isEqualTo("SC-801");
        assertThat(grupoCaptor.getValue().getMateria()).isSameAs(materia);
        assertThat(grupoCaptor.getValue().getPeriodo()).isSameAs(periodo);
        assertThat(grupoCaptor.getValue().getProfesor()).isSameAs(profesor);
        verify(auditoriaService).registrar(org.mockito.ArgumentMatchers.eq("admin"),
                org.mockito.ArgumentMatchers.eq("ACADEMICO"),
                org.mockito.ArgumentMatchers.eq("ASIGNACION_GRUPO"),
                org.mockito.ArgumentMatchers.contains("SC-801"), org.mockito.ArgumentMatchers.eq("127.0.0.1"));
    }
}
