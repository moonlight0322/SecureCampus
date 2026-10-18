package com.ds.securecampus.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.securecampus.model.Carrera;
import com.ds.securecampus.model.Aula;
import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.Edificio;
import com.ds.securecampus.model.Grupo;
import com.ds.securecampus.model.HorarioClase;
import com.ds.securecampus.model.LogAuditoria;
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
    private final AulaRepository aulaRepository;
    private final EdificioRepository edificioRepository;
    private final HorarioClaseRepository horarioClaseRepository;
    private final CargaAlumnoRepository cargaAlumnoRepository;
    private final LoginAttemptService loginAttemptService;

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
            PasswordEncoder passwordEncoder,
            AulaRepository aulaRepository,
            EdificioRepository edificioRepository,
            HorarioClaseRepository horarioClaseRepository,
            CargaAlumnoRepository cargaAlumnoRepository,
            LoginAttemptService loginAttemptService) {
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
        this.aulaRepository = aulaRepository;
        this.edificioRepository = edificioRepository;
        this.horarioClaseRepository = horarioClaseRepository;
        this.cargaAlumnoRepository = cargaAlumnoRepository;
        this.loginAttemptService = loginAttemptService;
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
                "Usuario " + numeroControl + " estatus cambiado a " + (nuevo == 1 ? "ACTIVO" : "INACTIVO"),
                "127.0.0.1");
    }

    @Transactional
    public void crearUsuario(String numeroControl, String password, String nombre, String apePat,
            String apeMat, String correo, Long rolId, Long carreraId,
            String tipoContratacion, String cubiculo, String adminUser) {
        if (numeroControl == null || numeroControl.isBlank()
                || !numeroControl.matches("\\d+")
                || numeroControl.length() > 20
                || !PoliticaContrasena.esValida(password)
                || nombre == null || nombre.isBlank() || nombre.length() > 50
                || apePat == null || apePat.isBlank() || apePat.length() > 50
                || apeMat != null && apeMat.length() > 50
                || correo == null || correo.isBlank() || correo.length() > 100) {
            throw new IllegalArgumentException(
                    "Revisa los campos obligatorios, sus longitudes y los requisitos de contraseña: "
                            + PoliticaContrasena.requisitos());
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

        String nombreRol = rol.getNombreRol() != null ? rol.getNombreRol().trim().toUpperCase() : "";
        if ("ESTUDIANTE".equals(nombreRol)) {
            if (carreraId == null) {
                throw new IllegalArgumentException("Debe seleccionar una carrera para el estudiante.");
            }
            Carrera carrera = carreraParaPerfil(carreraId);
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(u);
            estudiante.setCarrera(carrera);
            estudiante.setFechaIngreso(LocalDate.now());
            estudiante.setPromedio(BigDecimal.ZERO);
            estudiante.setPagoReinscripcion("N");
            estudiante.setTieneAdeudos("N");
            estudianteRepository.saveAndFlush(estudiante);
        } else if ("PROFESOR".equals(nombreRol)) {
            if (carreraId == null) {
                throw new IllegalArgumentException("Debe seleccionar una adscripción para el profesor.");
            }
            Carrera carrera = carreraParaPerfil(carreraId);
            if (tipoContratacion == null || tipoContratacion.isBlank() || tipoContratacion.length() > 20
                    || cubiculo != null && cubiculo.length() > 20) {
                throw new IllegalArgumentException(
                        "Revisa el tipo de contratación y el cubículo (máximo 20 caracteres).");
            }
            Profesor profesor = new Profesor();
            profesor.setUsuario(u);
            profesor.setCarreraAdscripcion(carrera);
            profesor.setTipoContratacion(tipoContratacion);
            profesor.setCubiculo(cubiculo);
            profesorRepository.saveAndFlush(profesor);
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
        if (!PoliticaContrasena.esValida(password)) {
            throw new IllegalArgumentException(PoliticaContrasena.requisitos());
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
    public Usuario obtenerUsuario(String numeroControl) {
        return usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el perfil de la cuenta autenticada."));
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarEstudiantes() {
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Grupo> listarGruposPeriodoActual() {
        Periodo periodo = periodoCalculado(LocalDate.now());
        return grupoRepository.findByPeriodoClavePeriodo(periodo.getClavePeriodo()).stream()
                .filter(grupo -> grupo.getHorarios() != null && !grupo.getHorarios().isEmpty())
                .toList();
    }

    public List<String> listarNombresAulas() {
        List<String> aulas = new java.util.ArrayList<>();
        mapaAulas().forEach((edificio, cantidad) -> {
            for (int numero = 1; numero <= cantidad; numero++) {
                aulas.add(edificio + "-" + numero);
            }
        });
        return List.copyOf(aulas);
    }

    @Transactional(readOnly = true)
    public List<Periodo> listarPeriodos() {
        return periodoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public String periodoAsignacionActual(LocalDate fecha) {
        return periodoCalculado(fecha).getNombre();
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void crearGrupoAsignacion(String codigoGrupo, Long materiaId, String profesorControl,
            String patronHorario, String horaInicio, String horaCorta, String aulaNombre,
            String operadorUser) {
        if (codigoGrupo == null || codigoGrupo.isBlank() || codigoGrupo.length() > 20) {
            throw new IllegalArgumentException("El código de grupo es obligatorio.");
        }
        Periodo calculado = periodoCalculado(LocalDate.now());
        Periodo periodo = periodoRepository.findByClavePeriodoForUpdate(calculado.getClavePeriodo())
                .orElseGet(() -> periodoRepository.saveAndFlush(calculado));
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada."));
        if (profesorControl == null || profesorControl.isBlank()) {
            throw new IllegalArgumentException("Selecciona un docente para el grupo.");
        }
        Profesor profesor = profesorRepository.findById(profesorControl)
                .orElseThrow(() -> new IllegalArgumentException("El profesor seleccionado no existe."));

        List<SesionClase> sesiones = HorarioAsignacion.crear(patronHorario, horaInicio, horaCorta);
        Aula aula = obtenerOCrearAula(aulaNombre);
        validarDisponibilidad(sesiones, aula, profesor, periodo.getPeriodoId());

        Grupo grupo = grupoRepository.findByCodigoGrupoAndMateriaMateriaIdAndPeriodoPeriodoId(
                codigoGrupo, materiaId, periodo.getPeriodoId()).orElseGet(() -> {
                    Grupo nuevo = new Grupo();
                    nuevo.setCodigoGrupo(codigoGrupo);
                    nuevo.setMateria(materia);
                    nuevo.setPeriodo(periodo);
                    return nuevo;
                });
        boolean grupoExistente = grupo.getGrupoId() != null;
        if (grupoExistente && !grupo.getHorarios().isEmpty()) {
            throw new IllegalArgumentException("Ese grupo ya tiene horario. Usa otro código para crear una sección distinta.");
        }
        if (grupoExistente) {
            for (CargaAlumno carga : cargaAlumnoRepository.findByGrupoGrupoId(grupo.getGrupoId())) {
                for (SesionClase sesion : sesiones) {
                    if (horarioClaseRepository.existeConflictoEstudiante(
                            carga.getEstudiante().getNumeroControl(), grupo.getGrupoId(),
                            periodo.getPeriodoId(), sesion.dia(),
                            sesion.horaInicio(), sesion.horaFin())) {
                        throw new IllegalArgumentException("El horario propuesto se cruza con otra materia de un estudiante ya inscrito.");
                    }
                }
            }
        }
        grupo.setProfesor(profesor);
        grupo = grupoRepository.saveAndFlush(grupo);
        for (SesionClase sesion : sesiones) {
            HorarioClase horario = new HorarioClase();
            horario.setGrupo(grupo);
            horario.setAula(aula);
            horario.setDiaSemana(sesion.dia());
            horario.setHoraInicio(sesion.horaInicio());
            horario.setHoraFin(sesion.horaFin());
            horarioClaseRepository.save(horario);
        }

        auditoriaService.registrar(operadorUser, "ACADEMICO",
                grupoExistente ? "HORARIO_GRUPO_COMPLETADO" : "ASIGNACION_GRUPO",
                "Grupo " + codigoGrupo + " asignado a materia " + materia.getNombre() +
                        " y docente " + profesor.getUsuario().getNombre(),
                "127.0.0.1");
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void inscribirEstudiante(Long grupoId, String numeroControl, String operadorUser) {
        Long periodoId = grupoRepository.findPeriodoIdByGrupoIdForUpdate(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("El grupo seleccionado no existe."));
        periodoRepository.findByIdForUpdate(periodoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el periodo del grupo."));
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("El grupo seleccionado no existe."));
        Estudiante estudiante = estudianteRepository.findById(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("El número de control no corresponde a un estudiante."));
        if (cargaAlumnoRepository.existsByEstudianteNumeroControlAndGrupoGrupoId(numeroControl, grupoId)) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en este grupo.");
        }

        List<HorarioClase> horarios = horarioClaseRepository.findByGrupoGrupoId(grupoId);
        if (horarios.isEmpty()) {
            throw new IllegalArgumentException("No se puede inscribir en un grupo que no tenga horario y aula.");
        }
        for (HorarioClase horario : horarios) {
            boolean conflicto = horarioClaseRepository.existeConflictoEstudiante(numeroControl, grupoId,
                    grupo.getPeriodo().getPeriodoId(), horario.getDiaSemana(),
                    horario.getHoraInicio(), horario.getHoraFin());
            if (conflicto) {
                throw new IllegalArgumentException("El estudiante ya tiene una clase que coincide con el horario solicitado.");
            }
        }
        CargaAlumno carga = new CargaAlumno();
        carga.setEstudiante(estudiante);
        carga.setGrupo(grupo);
        cargaAlumnoRepository.save(carga);
        auditoriaService.registrar(operadorUser, "ADMINISTRADOR", "INSCRIPCION_ESTUDIANTE",
                "Estudiante " + numeroControl + " inscrito en grupo " + grupo.getCodigoGrupo(), "127.0.0.1");
    }

    private void validarDisponibilidad(List<SesionClase> sesiones, Aula aula, Profesor profesor, Long periodoId) {
        for (SesionClase sesion : sesiones) {
            if (horarioClaseRepository.existeConflictoAula(aula.getEdificio().getClave(),
                    aula.getNombreAula(), sesion.dia(), periodoId,
                    sesion.horaInicio(), sesion.horaFin())) {
                throw new IllegalArgumentException("El aula ya está ocupada en uno de los bloques seleccionados.");
            }
            if (profesor != null && horarioClaseRepository.existeConflictoProfesor(
                    profesor.getNumeroControl(), sesion.dia(), periodoId,
                    sesion.horaInicio(), sesion.horaFin())) {
                throw new IllegalArgumentException("El docente ya tiene una clase que coincide con ese horario.");
            }
        }
    }

    private Aula obtenerOCrearAula(String nombreAula) {
        if (nombreAula == null || !nombreAula.matches("^(B2|T|U|E|M|P|Q|CC|Y|Z)-([1-9]|1[0-6])$")) {
            throw new IllegalArgumentException("Selecciona un aula válida del catálogo institucional.");
        }
        String edificioClave = nombreAula.substring(0, nombreAula.lastIndexOf('-'));
        int numero = Integer.parseInt(nombreAula.substring(nombreAula.lastIndexOf('-') + 1));
        Integer maximo = mapaAulas().get(edificioClave);
        if (maximo == null || numero > maximo) {
            throw new IllegalArgumentException("El número de salón no existe en el edificio seleccionado.");
        }
        return aulaRepository.findByEdificioClaveAndNombreAula(edificioClave, nombreAula)
                .orElseGet(() -> {
                    Edificio edificio = edificioRepository.findByClave(edificioClave).orElseGet(() -> {
                        Edificio nuevo = new Edificio();
                        nuevo.setClave(edificioClave);
                        nuevo.setNombre("Edificio " + edificioClave);
                        return edificioRepository.saveAndFlush(nuevo);
                    });
                    Aula nueva = new Aula();
                    nueva.setEdificio(edificio);
                    nueva.setNombreAula(nombreAula);
                    return aulaRepository.saveAndFlush(nueva);
                });
    }

    private java.util.Map<String, Integer> mapaAulas() {
        return java.util.Map.of("B2", 8, "T", 16, "U", 6, "E", 6, "M", 8,
                "P", 8, "Q", 8, "CC", 4, "Y", 3, "Z", 2);
    }

    private Periodo periodoCalculado(LocalDate fecha) {
        int anio = fecha.getYear();
        LocalDate inicioPrimavera = LocalDate.of(anio, Month.JANUARY, 18);
        LocalDate finPrimavera = inicioPrimavera.plusWeeks(18).minusDays(1);
        LocalDate inicioVerano = inicioPrimavera.plusWeeks(20);
        LocalDate finVerano = inicioVerano.plusWeeks(7).minusDays(1);
        LocalDate inicioAsignacionVerano = inicioVerano.minusWeeks(1);
        LocalDate inicioOtono = LocalDate.of(anio, Month.AUGUST, 10);
        LocalDate finOtono = inicioOtono.plusWeeks(18).minusDays(1);

        if (!fecha.isBefore(inicioAsignacionVerano) && !fecha.isAfter(finVerano)) {
            return nuevoPeriodo("VER" + anio, "Verano " + anio, inicioVerano, finVerano);
        }
        if (!fecha.isBefore(inicioPrimavera) && !fecha.isAfter(inicioAsignacionVerano.minusDays(1))) {
            return nuevoPeriodo("EJ" + anio, "Enero - Junio " + anio,
                    inicioPrimavera, finPrimavera);
        }
        if (!fecha.isBefore(inicioOtono) && !fecha.isAfter(finOtono)) {
            return nuevoPeriodo("AD" + anio, "Agosto - Diciembre " + anio, inicioOtono, finOtono);
        }
        if (fecha.isAfter(finVerano) && fecha.isBefore(inicioOtono)) {
            return nuevoPeriodo("AD" + anio, "Agosto - Diciembre " + anio, inicioOtono, finOtono);
        }

        int anioPrimavera = fecha.isAfter(finOtono) || fecha.getMonthValue() == Month.DECEMBER.getValue()
                ? anio + 1
                : anio;
        LocalDate inicioProximaPrimavera = LocalDate.of(anioPrimavera, Month.JANUARY, 18);
        LocalDate finProximaPrimavera = inicioProximaPrimavera.plusWeeks(18).minusDays(1);
        return nuevoPeriodo("EJ" + anioPrimavera, "Enero - Junio " + anioPrimavera,
                inicioProximaPrimavera, finProximaPrimavera);
    }

    private Periodo nuevoPeriodo(String clave, String nombre, LocalDate inicio, LocalDate fin) {
        Periodo periodo = new Periodo();
        periodo.setClavePeriodo(clave);
        periodo.setNombre(nombre);
        periodo.setFechaInicio(inicio);
        periodo.setFechaFin(fin);
        periodo.setLimiteCapturaCalificaciones(fin);
        periodo.setCapturaActiva("N");
        return periodo;
    }

    @Transactional
    public void alternarPagoReinscripcion(String numeroControl, String adminUser) {
        Estudiante estudiante = estudianteRepository.findById(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
        String actual = estudiante.getPagoReinscripcion();
        String nuevo = "S".equalsIgnoreCase(actual) ? "N" : "S";
        estudiante.setPagoReinscripcion(nuevo);
        estudianteRepository.save(estudiante);
        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "MODIFICAR_PAGO_REINSCRIPCION",
                "Estatus de inscripción del alumno " + numeroControl + " cambiado a " + ("S".equals(nuevo) ? "INSCRITO (HABILITADO)" : "PENDIENTE (NO INSCRITO)"),
                "127.0.0.1");
    }

    @Transactional
    public void desbloquearCuenta(String numeroControl, String adminUser) {
        Usuario usuario = usuarioRepository.findByNumeroControl(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setActivo((byte) 1);
        usuarioRepository.save(usuario);
        loginAttemptService.desbloquear(numeroControl);
        auditoriaService.registrar(adminUser, "ADMINISTRADOR", "DESBLOQUEO_CUENTA",
                "Cuenta desbloqueada y reactivada para usuario " + numeroControl, "127.0.0.1");
    }

    @Transactional
    public void quitarEstudianteDeGrupo(Long grupoId, String numeroControl, String operadorUser) {
        CargaAlumno carga = cargaAlumnoRepository.findByGrupoGrupoId(grupoId).stream()
                .filter(c -> c.getEstudiante().getNumeroControl().equals(numeroControl))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El estudiante no está inscrito en este grupo."));
        cargaAlumnoRepository.delete(carga);
        auditoriaService.registrar(operadorUser, "ACADEMICO", "BAJA_ESTUDIANTE_GRUPO",
                "Estudiante " + numeroControl + " desinscrito del grupo ID " + grupoId, "127.0.0.1");
    }

    @Transactional
    public void cambiarProfesorGrupo(Long grupoId, String nuevoProfesorControl, String operadorUser) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
        Profesor nuevoProfesor = null;
        if (nuevoProfesorControl != null && !nuevoProfesorControl.isBlank() && !"0".equals(nuevoProfesorControl)) {
            nuevoProfesor = profesorRepository.findById(nuevoProfesorControl)
                    .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado."));
            List<HorarioClase> horarios = horarioClaseRepository.findByGrupoGrupoId(grupoId);
            for (HorarioClase h : horarios) {
                if (horarioClaseRepository.existeConflictoProfesor(nuevoProfesorControl, h.getDiaSemana(),
                        grupo.getPeriodo().getPeriodoId(), h.getHoraInicio(), h.getHoraFin())) {
                    throw new IllegalArgumentException("El docente seleccionado ya tiene una clase asignada en ese mismo horario.");
                }
            }
        }
        grupo.setProfesor(nuevoProfesor);
        grupoRepository.save(grupo);
        auditoriaService.registrar(operadorUser, "ACADEMICO", "CAMBIO_DOCENTE_GRUPO",
                "Grupo " + grupo.getCodigoGrupo() + ": docente actualizado a " + (nuevoProfesor != null ? nuevoProfesorControl : "Sin asignar"),
                "127.0.0.1");
    }

    @Transactional
    public void cerrarGrupo(Long grupoId, String operadorUser) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
        List<CargaAlumno> alumnos = cargaAlumnoRepository.findByGrupoGrupoId(grupoId);
        cargaAlumnoRepository.deleteAll(alumnos);
        List<HorarioClase> horarios = horarioClaseRepository.findByGrupoGrupoId(grupoId);
        horarioClaseRepository.deleteAll(horarios);
        grupoRepository.delete(grupo);
        auditoriaService.registrar(operadorUser, "ACADEMICO", "CIERRE_GRUPO",
                "Grupo cerrado: " + grupo.getCodigoGrupo() + " (" + alumnos.size() + " estudiantes desinscritos)",
                "127.0.0.1");
    }

    @Transactional
    public int inscribirMultiplesEstudiantes(Long grupoId, List<String> numerosControl, String operadorUser) {
        if (numerosControl == null || numerosControl.isEmpty()) return 0;
        int count = 0;
        for (String nc : numerosControl) {
            if (nc != null && !nc.isBlank()) {
                try {
                    inscribirEstudiante(grupoId, nc.trim(), operadorUser);
                    count++;
                } catch (Exception ignored) {
                    // Ignora choque o duplicado individual para procesar el lote
                }
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiante(String numeroControl) {
        return estudianteRepository.findById(numeroControl)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<CargaAlumno> obtenerCargasEstudiantePeriodoActual(String numeroControl) {
        Periodo periodo = periodoCalculado(LocalDate.now());
        return cargaAlumnoRepository.findAll().stream()
                .filter(c -> c.getEstudiante().getNumeroControl().equals(numeroControl)
                        && c.getGrupo().getPeriodo().getClavePeriodo().equals(periodo.getClavePeriodo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.ds.securecampus.model.EstadisticaGrupo> obtenerEstadisticasTodosGrupos() {
        Periodo periodo = periodoCalculado(LocalDate.now());
        List<Grupo> grupos = grupoRepository.findByPeriodoClavePeriodo(periodo.getClavePeriodo());
        List<com.ds.securecampus.model.EstadisticaGrupo> stats = new java.util.ArrayList<>();
        for (Grupo g : grupos) {
            List<CargaAlumno> cargas = cargaAlumnoRepository.findByGrupoGrupoId(g.getGrupoId());
            int total = cargas.size();
            int aprobados = 0;
            int reprobados = 0;
            int cursando = 0;
            double suma = 0;
            int conNota = 0;
            for (CargaAlumno c : cargas) {
                if ("APROBADA".equalsIgnoreCase(c.getEstatus())) aprobados++;
                else if ("REPROBADA".equalsIgnoreCase(c.getEstatus())) reprobados++;
                else cursando++;

                if (c.getCalificacionFinal() != null) {
                    suma += c.getCalificacionFinal().doubleValue();
                    conNota++;
                }
            }
            double prom = conNota > 0 ? Math.round((suma / conNota) * 100.0) / 100.0 : 0.0;
            String profName = g.getProfesor() != null && g.getProfesor().getUsuario() != null
                    ? g.getProfesor().getUsuario().getNombre() + " " + g.getProfesor().getUsuario().getApellidoPaterno()
                    : "Sin docente asignado";
            stats.add(new com.ds.securecampus.model.EstadisticaGrupo(
                    g.getGrupoId(),
                    g.getCodigoGrupo(),
                    g.getMateria().getNombre(),
                    profName,
                    total,
                    35, // Cupo estimado estándar por salón
                    aprobados,
                    reprobados,
                    cursando,
                    prom
            ));
        }
        return stats;
    }
}