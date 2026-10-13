-- =========================================================================
-- Script de creación de la base de datos (corregido)
-- =========================================================================

-- 1. CATÁLOGOS GENERALES
CREATE TABLE roles (
    rol_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol VARCHAR2(30) NOT NULL UNIQUE
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
    id_edificio NUMBER NOT NULL,
    nombre_aula VARCHAR2(30) NOT NULL,
    capacidad NUMBER(3) DEFAULT 30 NOT NULL,
    tipo_aula VARCHAR2(30) DEFAULT 'TEORICA' NOT NULL
        CHECK (tipo_aula IN ('TEORICA', 'LABORATORIO', 'TALLER', 'AUDITORIO')),
    CONSTRAINT fk_aulas_edificio FOREIGN KEY (id_edificio) REFERENCES edificios(id_edificio)
);

-- 2. PERFILES BASE
CREATE TABLE perfiles_base (
    numero_control VARCHAR2(20) NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    rol_id NUMBER NOT NULL,
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    fecha_nacimiento DATE,
    domicilio VARCHAR2(200),
    telefono VARCHAR2(15),
    correo_institucional VARCHAR2(100) NOT NULL UNIQUE,
    activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
    debe_cambiar_pass NUMBER(1) DEFAULT 1 CHECK (debe_cambiar_pass IN (0, 1)),
    fecha_creacion DATE DEFAULT SYSDATE,
    CONSTRAINT pk_perfiles PRIMARY KEY (numero_control),
    CONSTRAINT fk_perfiles_roles FOREIGN KEY (rol_id) REFERENCES roles(rol_id),
    CONSTRAINT chk_num_control_num CHECK (REGEXP_LIKE(numero_control, '^[0-9]+$'))
);

CREATE TABLE estudiantes (
    numero_control VARCHAR2(20) NOT NULL,
    id_carrera NUMBER NOT NULL,
    promedio NUMBER(4,2) DEFAULT 0.00,
    pago_reinscripcion CHAR(1) DEFAULT 'N' NOT NULL CHECK (pago_reinscripcion IN ('S', 'N')),
    tiene_adeudos CHAR(1) DEFAULT 'N' NOT NULL CHECK (tiene_adeudos IN ('S', 'N')),
    CONSTRAINT pk_estudiantes PRIMARY KEY (numero_control),
    CONSTRAINT fk_estudiantes_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    CONSTRAINT fk_estudiantes_carrera FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
);

CREATE TABLE profesores (
    numero_control VARCHAR2(20) NOT NULL,
    id_carrera_adscripcion NUMBER NOT NULL,
    tipo_contratacion VARCHAR2(20) NOT NULL CHECK (tipo_contratacion IN ('BASE', 'HONORARIOS', 'HORARIO')),
    cubiculo VARCHAR2(20),
    CONSTRAINT pk_profesores PRIMARY KEY (numero_control),
    CONSTRAINT fk_profesores_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    CONSTRAINT fk_profesores_carrera FOREIGN KEY (id_carrera_adscripcion) REFERENCES carreras(id_carrera)
);

CREATE TABLE administrativos (
    numero_control VARCHAR2(20) NOT NULL,
    area_departamento VARCHAR2(80) NOT NULL,
    cargo_puesto VARCHAR2(80),
    CONSTRAINT pk_administrativos PRIMARY KEY (numero_control),
    CONSTRAINT fk_administrativos_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE
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
    id_carrera NUMBER NOT NULL,
    CONSTRAINT fk_materias_carrera FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
);

CREATE TABLE prerrequisitos_materia (
    materia_id NUMBER NOT NULL,
    materia_prerrequisito_id NUMBER NOT NULL,
    CONSTRAINT pk_prerrequisitos PRIMARY KEY (materia_id, materia_prerrequisito_id),
    CONSTRAINT fk_prerrequisito_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_prerrequisito_referencia FOREIGN KEY (materia_prerrequisito_id) REFERENCES materias(materia_id)
);

CREATE TABLE grupos (
    grupo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_grupo VARCHAR2(20) NOT NULL,
    materia_id NUMBER NOT NULL,
    profesor_numero_control VARCHAR2(20),
    periodo_id NUMBER NOT NULL,
    CONSTRAINT fk_grupos_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_grupos_profesor FOREIGN KEY (profesor_numero_control) REFERENCES profesores(numero_control),
    CONSTRAINT fk_grupos_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id),
    CONSTRAINT uk_codigo_periodo_grupo UNIQUE (codigo_grupo, periodo_id)
);

CREATE TABLE horarios_clase (
    horario_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grupo_id NUMBER NOT NULL,
    id_aula NUMBER NOT NULL,
    dia_semana NUMBER(1) NOT NULL CHECK (dia_semana BETWEEN 1 AND 6),
    hora_inicio VARCHAR2(5) NOT NULL,
    hora_fin VARCHAR2(5) NOT NULL,
    CONSTRAINT fk_horarios_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id) ON DELETE CASCADE,
    CONSTRAINT fk_horarios_aula FOREIGN KEY (id_aula) REFERENCES aulas(id_aula)
);

-- 4. INSCRIPCIONES, EVALUACIONES Y KÁRDEX
CREATE TABLE cargas_academicas (
    carga_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL,
    grupo_id NUMBER NOT NULL,
    calificacion_parcial1 NUMBER(4,2),
    calificacion_parcial2 NUMBER(4,2),
    calificacion_final NUMBER(5,2),
    estatus VARCHAR2(20) DEFAULT 'CURSANDO' NOT NULL CHECK (estatus IN ('CURSANDO', 'APROBADA', 'REPROBADA', 'BAJA')),
    oportunidad VARCHAR2(20) DEFAULT 'PRIMERA' NOT NULL CHECK (oportunidad IN ('PRIMERA', 'SEGUNDA', 'REPETICION', 'ESPECIAL')),
    CONSTRAINT fk_cargas_estudiante FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_cargas_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id),
    CONSTRAINT uk_estudiante_grupo UNIQUE (numero_control_estudiante, grupo_id)
);

CREATE TABLE criterios_evaluacion (
    criterio_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grupo_id NUMBER NOT NULL,
    nombre_criterio VARCHAR2(50) NOT NULL,
    porcentaje NUMBER(5,2) NOT NULL,
    CONSTRAINT fk_criterios_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id) ON DELETE CASCADE
);

CREATE TABLE calificaciones_detalladas (
    calificacion_det_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    carga_id NUMBER NOT NULL,
    criterio_id NUMBER NOT NULL,
    calificacion NUMBER(4,2),
    CONSTRAINT fk_calif_carga FOREIGN KEY (carga_id) REFERENCES cargas_academicas(carga_id) ON DELETE CASCADE,
    CONSTRAINT fk_calif_criterio FOREIGN KEY (criterio_id) REFERENCES criterios_evaluacion(criterio_id),
    CONSTRAINT uk_carga_criterio UNIQUE (carga_id, criterio_id)
);

CREATE TABLE kardex_estudiante (
    kardex_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL,
    materia_id NUMBER NOT NULL,
    periodo_id NUMBER NOT NULL,
    calificacion_final NUMBER(4,2) NOT NULL,
    tipo_acreditacion VARCHAR2(20) DEFAULT 'ORDINARIO' NOT NULL
        CHECK (tipo_acreditacion IN ('ORDINARIO', 'EXTRAORDINARIO', 'REPETICION', 'CONVALIDACION')),
    estatus VARCHAR2(15) NOT NULL CHECK (estatus IN ('APROBADA', 'REPROBADA')),
    creditos_obtenidos NUMBER(2) NOT NULL,
    CONSTRAINT fk_kardex_estudiante FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_kardex_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_kardex_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id),
    CONSTRAINT uk_estudiante_mat_per UNIQUE (numero_control_estudiante, materia_id, periodo_id)
);

-- 5. SERVICIOS Y FINANZAS

CREATE TABLE turnos_inscripcion (
    turno_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL,
    periodo_id NUMBER NOT NULL,
    fecha_hora_inscripcion TIMESTAMP NOT NULL,
    CONSTRAINT fk_turnos_estudiante FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_turnos_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id),
    CONSTRAINT uk_estudiante_periodo_turno UNIQUE (numero_control_estudiante, periodo_id)
);

CREATE TABLE vouchers_servicios (
    voucher_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL,
    concepto VARCHAR2(100) NOT NULL,
    monto NUMBER(8,2) NOT NULL,
    folio VARCHAR2(50) UNIQUE NOT NULL,
    fecha_emision DATE DEFAULT SYSDATE,
    estatus VARCHAR2(20) DEFAULT 'PENDIENTE'
        CHECK (estatus IN ('PENDIENTE', 'PAGADO', 'CANCELADO')),
    CONSTRAINT fk_vouchers_estudiante FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control)
);