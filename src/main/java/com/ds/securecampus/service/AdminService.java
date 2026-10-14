package com.ds.securecampus.service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.LogAuditoria;
import com.ds.securecampus.model.Materia;
import com.ds.securecampus.model.Periodo;
import com.ds.securecampus.model.Profesor;
import com.ds.securecampus.model.Rol;
import com.ds.securecampus.model.Usuario;
import com.ds.securecampus.repository.CarreraRepository;
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
        usuarioRepository.findByNumeroControl(numeroControl).ifPresent(u -> {
            byte nuevo = (u.getActivo() != null && u.getActivo() == 1) ? (byte) 0 : (byte) 1;
            u.setActivo(nuevo);
            usuarioRepository.save(u);
            auditoriaService.registrar(adminUser, "ADMINISTRADOR", "MODIFICAR_ESTATUS_USUARIO",
                    "Usuario " + numeroControl + " estatus cambiado a " + (nuevo == 1 ? "ACTIVO" : "INACTIVO"),
                    "127.0.0.1");
        });
    }

    @Transactional
    public void crearUsuario(String numeroControl, String password, String nombre, String apePat,
            String apeMat, String correo, Long rolId, String adminUser) {
        if (usuarioRepository.findByNumeroControl(numeroControl).isPresent()) {
            throw new IllegalArgumentException("El número de control ya se encuentra registrado.");
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
        usuarioRepository.save(u);

        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "ALTA_USUARIO",
                "Nuevo usuario registrado: " + numeroControl + " (" + rol.getNombreRol() + ")", "127.0.0.1");
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
        if (materiaRepository.existsByClave(clave)) {
            throw new IllegalArgumentException("La clave de materia ya existe.");
        }
        Carrera carrera = carreraRepository.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada."));

        Materia m = new Materia();
        m.setClave(clave);
        m.setNombre(nombre);
        m.setCreditos(creditos != null ? BigDecimal.valueOf(creditos) : null);
        m.setCarrera(carrera);
        materiaRepository.save(m);

        auditoriaService.registrar(jefeUser, "JEFE_DEPARTAMENTO", "ALTA_MATERIA",
                "Nueva materia: " + clave + " - " + nombre, "127.0.0.1");
    }

    @Transactional
    public void crearGrupoAsignacion(String codigoGrupo, Long materiaId, String profesorControl, Long periodoId,
            String operadorUser) {
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada."));
        Periodo periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado."));
        Profesor profesor = null;
        if (profesorControl != null && !profesorControl.isBlank()) {
            profesor = profesorRepository.findById(profesorControl).orElse(null);
        }

        Grupo g = new Grupo();
        g.setCodigoGrupo(codigoGrupo);
        g.setMateria(materia);
        g.setProfesor(profesor);
        g.setPeriodo(periodo);
        grupoRepository.save(g);

        auditoriaService.registrar(operadorUser, "ACADEMICO", "ASIGNACION_GRUPO",
                "Grupo " + codigoGrupo + " asignado a materia " + materia.getNombre() +
                        (profesor != null ? " y docente " + profesor.getUsuario().getNombre() : " sin docente"),
                "127.0.0.1");
    }
}
