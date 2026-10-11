-- =========================================================================
--- Script de creación de la base de datos
-- =========================================================================

-- 1. CATÁLOGOS GENERALES E INFRAESTRUCTURA
CREATE TABLE roles (
    rol_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol VARCHAR2(30) NOT NULL UNIQUE -- Ej: ESTUDIANTE, PROFESOR, ADMINISTRATIVO
);

CREATE TABLE carreras (
    id_carrera NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave_carrera VARCHAR2(20) NOT NULL UNIQUE,
    nombre_carrera VARCHAR2(100) NOT NULL
);

CREATE TABLE edificios (
    id_edificio NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave_edificio VARCHAR2(10) NOT NULL UNIQUE,
    nombre_edificio VARCHAR2(50) NOT NULL
);

CREATE TABLE aulas (
    id_aula NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_edificio NUMBER NOT NULL REFERENCES edificios(id_edificio),
    nombre_aula VARCHAR2(30) NOT NULL,
    capacidad NUMBER(3) DEFAULT 30 NOT NULL,
    tipo_aula VARCHAR2(30) DEFAULT 'TEORICA' NOT NULL
        CHECK (tipo_aula IN ('TEORICA', 'LABORATORIO', 'TALLER', 'AUDITORIO'))
);

-- 2. PERFILES BASE (El número de control es la PK y credencial de acceso)
CREATE TABLE perfiles_base (
    numero_control VARCHAR2(20) PRIMARY KEY,
    password_hash VARCHAR2(255) NOT NULL,
    rol_id NUMBER NOT NULL REFERENCES roles(rol_id),

    -- Datos personales y de contacto
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    fecha_nacimiento DATE,
    domicilio VARCHAR2(200),
    telefono VARCHAR2(15),
    correo_institucional VARCHAR2(100) NOT NULL UNIQUE,

    -- Control de acceso y seguridad semestral
    activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
    debe_cambiar_pass NUMBER(1) DEFAULT 1 CHECK (debe_cambiar_pass IN (0, 1)), -- 1: Debe cambiar contraseña por inicio de semestre
    fecha_creacion DATE DEFAULT SYSDATE,

    CONSTRAINT chk_num_control_num CHECK (REGEXP_LIKE(numero_control, '^[0-9]+$'))
);

CREATE TABLE estudiantes (
    numero_control VARCHAR2(20) PRIMARY KEY REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    id_carrera NUMBER NOT NULL REFERENCES carreras(id_carrera),
    promedio NUMBER(4,2) DEFAULT 0.00,
    pago_reinscripcion CHAR(1) DEFAULT 'N' NOT NULL CHECK (pago_reinscripcion IN ('S', 'N')),
    tiene_adeudos CHAR(1) DEFAULT 'N' NOT NULL CHECK (tiene_adeudos IN ('S', 'N'))
);

CREATE TABLE profesores (
    numero_control VARCHAR2(20) PRIMARY KEY REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    id_carrera_adscripcion NUMBER NOT NULL REFERENCES carreras(id_carrera),
    tipo_contratacion VARCHAR2(20) NOT NULL CHECK (tipo_contratacion IN ('BASE', 'HONORARIOS', 'HORARIO')),
    cubiculo VARCHAR2(20)
);

CREATE TABLE administrativos (
    numero_control VARCHAR2(20) PRIMARY KEY REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    area_departamento VARCHAR2(80) NOT NULL,
    cargo_puesto VARCHAR2(80)
);

-- 3. OFERTA ACADÉMICA Y PERIODOS
CREATE TABLE periodos (
    periodo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave_periodo VARCHAR2(10) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    limite_captura_calificaciones DATE NOT NULL,
    captura_activa CHAR(1) DEFAULT 'N' NOT NULL CHECK (captura_activa IN ('S', 'N'))
);

CREATE TABLE materias (
    materia_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave_materia VARCHAR2(20) NOT NULL UNIQUE,
    nombre_materia VARCHAR2(100) NOT NULL,
    creditos NUMBER(2) NOT NULL,
    id_carrera NUMBER NOT NULL REFERENCES carreras(id_carrera)
);

CREATE TABLE prerrequisitos_materia (
    materia_id NUMBER NOT NULL REFERENCES materias(materia_id),
    materia_prerrequisito_id NUMBER NOT NULL REFERENCES materias(materia_id),
    PRIMARY KEY (materia_id, materia_prerrequisito_id)
);

CREATE TABLE grupos (
    grupo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_grupo VARCHAR2(20) NOT NULL,
    materia_id NUMBER NOT NULL REFERENCES materias(materia_id),
    profesor_numero_control VARCHAR2(20) REFERENCES profesores(numero_control),
    periodo_id NUMBER NOT NULL REFERENCES periodos(periodo_id),
    CONSTRAINT uk_codigo_periodo_grupo UNIQUE (codigo_grupo, periodo_id)
);

CREATE TABLE horarios_clase (
    horario_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grupo_id NUMBER NOT NULL REFERENCES grupos(grupo_id) ON DELETE CASCADE,
    id_aula NUMBER NOT NULL REFERENCES aulas(id_aula),
    dia_semana NUMBER(1) NOT NULL CHECK (dia_semana BETWEEN 1 AND 6),
    hora_inicio VARCHAR2(5) NOT NULL,
    hora_fin VARCHAR2(5) NOT NULL
);

-- 4. INSCRIPCIONES, EVALUACIONES Y KÁRDEX
CREATE TABLE cargas_academicas (
    carga_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL REFERENCES estudiantes(numero_control),
    grupo_id NUMBER NOT NULL REFERENCES grupos(grupo_id),
    calificacion_parcial1 NUMBER(4,2),
    calificacion_parcial2 NUMBER(4,2),
    calificacion_final NUMBER(5,2),
    estatus VARCHAR2(20) DEFAULT 'CURSANDO' NOT NULL
        CHECK (estatus IN ('CURSANDO', 'APROBADA', 'REPROBADA', 'BAJA')),
    oportunidad VARCHAR2(20) DEFAULT 'PRIMERA' NOT NULL
        CHECK (oportunidad IN ('PRIMERA', 'SEGUNDA', 'REPETICION', 'ESPECIAL')),
    CONSTRAINT uk_estudiante_grupo UNIQUE (numero_control_estudiante, grupo_id)
);

CREATE TABLE criterios_evaluacion (
    criterio_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grupo_id NUMBER NOT NULL REFERENCES grupos(grupo_id) ON DELETE CASCADE,
    nombre_criterio VARCHAR2(50) NOT NULL,
    porcentaje NUMBER(5,2) NOT NULL
);

CREATE TABLE calificaciones_detalladas (
    calificacion_det_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    carga_id NUMBER NOT NULL REFERENCES cargas_academicas(carga_id) ON DELETE CASCADE,
    criterio_id NUMBER NOT NULL REFERENCES criterios_evaluacion(criterio_id),
    calificacion NUMBER(4,2),
    CONSTRAINT uk_carga_criterio UNIQUE (carga_id, criterio_id)
);

CREATE TABLE kardex_estudiante (
    kardex_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL REFERENCES estudiantes(numero_control),
    materia_id NUMBER NOT NULL REFERENCES materias(materia_id),
    periodo_id NUMBER NOT NULL REFERENCES periodos(periodo_id),
    calificacion_final NUMBER(4,2) NOT NULL,
    tipo_acreditacion VARCHAR2(20) DEFAULT 'ORDINARIO' NOT NULL
        CHECK (tipo_acreditacion IN ('ORDINARIO', 'EXTRAORDINARIO', 'REPETICION', 'CONVALIDACION')),
    estatus VARCHAR2(15) NOT NULL CHECK (estatus IN ('APROBADA', 'REPROBADA')),
    creditos_obtenidos NUMBER(2) NOT NULL,
    CONSTRAINT uk_estudiante_mat_per UNIQUE (numero_control_estudiante, materia_id, periodo_id)
);

-- 5. SERVICIOS Y FINANZAS
CREATE TABLE turnos_inscripcion (
    turno_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL REFERENCES estudiantes(numero_control),
    periodo_id NUMBER NOT NULL REFERENCES periodos(periodo_id),
    fecha_hora_inscripcion TIMESTAMP NOT NULL,
    CONSTRAINT uk_estudiante_periodo_turno UNIQUE (numero_control_estudiante, periodo_id)
);

CREATE TABLE vouchers_servicios (
    voucher_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL REFERENCES estudiantes(numero_control),
    concepto VARCHAR2(100) NOT NULL,
    monto NUMBER(8,2) NOT NULL,
    folio VARCHAR2(50) UNIQUE NOT NULL,
    fecha_emision DATE DEFAULT SYSDATE,
    estatus VARCHAR2(20) DEFAULT 'PENDIENTE'
        CHECK (estatus IN ('PENDIENTE', 'PAGADO', 'CANCELADO'))
);

