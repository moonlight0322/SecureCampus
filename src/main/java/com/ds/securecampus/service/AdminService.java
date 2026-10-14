package com.ds.securecampus.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.model.Rol;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.CarreraRepository;
import com.ds.securecampus.repository.EstudianteRepository;
import com.ds.securecampus.repository.GrupoRepository;
import com.ds.securecampus.repository.LogAuditoriaRepository;
import com.ds.securecampus.repository.MateriaRepository;
import com.ds.securecampus.repository.PeriodoRepository;
import com.ds.securecampus.repository.ProfesorRepository;
import com.ds.securecampus.repository.RolRepository;
import com.ds.securecampus.repository.UsuarioRepository;

@Service
public class AdminService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final MateriaRepository materiaRepository;
    private final CarreraRepository carreraRepository;
    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    private final ProfesorRepository profesorRepository;
    private final PeriodoRepository periodoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        MateriaRepository materiaRepository,
                        CarreraRepository carreraRepository,
                        EstudianteRepository estudianteRepository,
                        GrupoRepository grupoRepository,
                        ProfesorRepository profesorRepository,
                        PeriodoRepository periodoRepository,
                        LogAuditoriaRepository logAuditoriaRepository,
                        AuditoriaService auditoriaService,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.materiaRepository = materiaRepository;
        this.carreraRepository = carreraRepository;
        this.estudianteRepository = estudianteRepository;
        this.grupoRepository = grupoRepository;
        this.profesorRepository = profesorRepository;
        this.periodoRepository = periodoRepository;
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.auditoriaService = auditoriaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LogAuditoria> listarAuditoria() {
        return logAuditoriaRepository.findTop100ByOrderByFechaHoraDesc();
    }

    @Transactional
    public void alternarEstatusUsuario(String numeroControl, String adminUser) {
        Usuario usuario = usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        byte nuevo = (usuario.getActivo() != null && usuario.getActivo() == 1) ? (byte) 0 : (byte) 1;
        usuario.setActivo(nuevo);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "MODIFICAR_ESTATUS_USUARIO",
                "Usuario " + numeroControl + " estatus cambiado a " + (nuevo == 1 ? "ACTIVO" : "INACTIVO"), "127.0.0.1");
    }

    @Transactional
    public void crearUsuario(String numeroControl, String password, String nombre, String apePat, 
                             String apeMat, String correo, Long rolId, Long carreraId,
                             String tipoContratacion, String cubiculo, String adminUser) {
        if (numeroControl == null || numeroControl.isBlank()
                || !numeroControl.matches("\\d+")
                || numeroControl.length() > 20
                || password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || nombre == null || nombre.isBlank() || nombre.length() > 50
                || apePat == null || apePat.isBlank() || apePat.length() > 50
                || apeMat != null && apeMat.length() > 50
                || correo == null || correo.isBlank() || correo.length() > 100) {
            throw new IllegalArgumentException("Revisa los campos obligatorios, sus longitudes y la contraseña (12 a 72 bytes).");
        }
        if (usuarioRepository.findByNumeroControl(numeroControl).isPresent()) {
            throw new IllegalArgumentException("El número de control ya se encuentra registrado.");
        }
        if (usuarioRepository.existsByCorreoInstitucional(correo)) {
            throw new IllegalArgumentException("El correo institucional ya se encuentra registrado.");
        }
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado."));

        Usuario u = new Usuario();
        u.setNumeroControl(numeroControl);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setNombre(nombre);
        u.setApellidoPaterno(apePat);
        u.setApellidoMaterno(apeMat);
        u.setCorreoInstitucional(correo);
        u.setActivo((byte) 1);
        u.setDebeCambiarPass((byte) 1);
        u.setFechaCreacion(LocalDate.now());
        usuarioRepository.saveAndFlush(u);

        String nombreRol = rol.getNombreRol().toUpperCase();
        if ("ESTUDIANTE".equals(nombreRol)) {
            Carrera carrera = carreraParaPerfil(carreraId);
            Estudiante estudiante = new Estudiante();
            estudiante.setNumeroControl(numeroControl);
            estudiante.setUsuario(u);
            estudiante.setCarrera(carrera);
            estudiante.setFechaIngreso(LocalDate.now());
            estudiante.setPromedio(BigDecimal.ZERO);
            estudiante.setPagoReinscripcion("N");
            estudiante.setTieneAdeudos("N");
            estudianteRepository.save(estudiante);
        } else if ("PROFESOR".equals(nombreRol)) {
            Carrera carrera = carreraParaPerfil(carreraId);
            if (tipoContratacion == null || tipoContratacion.isBlank() || tipoContratacion.length() > 20
                    || cubiculo != null && cubiculo.length() > 20) {
                throw new IllegalArgumentException("Revisa el tipo de contratación y el cubículo (máximo 20 caracteres).");
            }
            Profesor profesor = new Profesor();
            profesor.setNumeroControl(numeroControl);
            profesor.setUsuario(u);
            profesor.setCarreraAdscripcion(carrera);
            profesor.setTipoContratacion(tipoContratacion);
            profesor.setCubiculo(cubiculo);
            profesorRepository.save(profesor);
        }

        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "ALTA_USUARIO", 
                "Nuevo usuario registrado: " + numeroControl + " (" + rol.getNombreRol() + ")", "127.0.0.1");
    }

    private Carrera carreraParaPerfil(Long carreraId) {
        if (carreraId == null) {
            throw new IllegalArgumentException("Selecciona la carrera de adscripción del usuario.");
        }
        return carreraRepository.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada."));
    }

    @Transactional
    public void restablecerPassword(String numeroControl, String password, String adminUser) {
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña temporal debe tener de 12 a 72 bytes.");
        }
        Usuario usuario = usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setDebeCambiarPass((byte) 1);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "RESTABLECER_PASSWORD",
                "Contraseña restablecida para usuario " + numeroControl, "127.0.0.1");
    }

    // --- Funcionalidades para Jefe de Departamento y Asignaciones ---
    @Transactional(readOnly = true)
    public List<Carrera> listarCarreras() {
        return carreraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Materia> listarMaterias() {
        return materiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Profesor> listarProfesores() {
        return profesorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Periodo> listarPeriodos() {
        return periodoRepository.findAll();
    }

    @Transactional
    public void altaMateria(String clave, String nombre, Integer creditos, Long carreraId, String jefeUser) {
        if (clave == null || clave.isBlank() || clave.length() > 20
                || nombre == null || nombre.isBlank() || nombre.length() > 100
                || creditos == null || creditos < 1 || creditos > 10) {
            throw new IllegalArgumentException("Indica clave, nombre y entre 1 y 10 créditos para la materia.");
        }
        if (materiaRepository.existsByClave(clave)) {
            throw new IllegalArgumentException("La clave de materia ya existe.");
        }
        Carrera carrera = carreraRepository.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada."));

        Materia m = new Materia();
        m.setClave(clave);
        m.setNombre(nombre);
        m.setCreditos(BigDecimal.valueOf(creditos.longValue()));
        m.setCarrera(carrera);
        materiaRepository.save(m);

        auditoriaService.registrar(jefeUser, "JEFE_DEPARTAMENTO", "ALTA_MATERIA", 
                "Nueva materia: " + clave + " - " + nombre, "127.0.0.1");
    }

    @Transactional
    public void crearGrupoAsignacion(String codigoGrupo, Long materiaId, String profesorControl, Long periodoId, String operadorUser) {
        if (codigoGrupo == null || codigoGrupo.isBlank() || codigoGrupo.length() > 20) {
            throw new IllegalArgumentException("El código de grupo es obligatorio.");
        }
        if (grupoRepository.existsByCodigoGrupoAndMateriaMateriaIdAndPeriodoPeriodoId(
                codigoGrupo, materiaId, periodoId)) {
            throw new IllegalArgumentException("Ya existe ese grupo para la materia y el periodo seleccionados.");
        }
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada."));
        Periodo periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado."));
        Profesor profesor = null;
        if (profesorControl != null && !profesorControl.isBlank()) {
            profesor = profesorRepository.findById(profesorControl)
                    .orElseThrow(() -> new IllegalArgumentException("El profesor seleccionado no existe."));
        }

        Grupo g = new Grupo();
        g.setCodigoGrupo(codigoGrupo);
        g.setMateria(materia);
        g.setProfesor(profesor);
        g.setPeriodo(periodo);
        grupoRepository.save(g);

        auditoriaService.registrar(operadorUser, "ACADEMICO", "ASIGNACION_GRUPO", 
                "Grupo " + codigoGrupo + " asignado a materia " + materia.getNombre() + 
                (profesor != null ? " y docente " + profesor.getUsuario().getNombre() : " sin docente"), "127.0.0.1");
    }
}
