-- Ejecutar conectado como el usuario propietario del esquema de SecureCampus.

CREATE TABLE carreras (
    carrera_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave VARCHAR2(20) UNIQUE NOT NULL,
    nombre VARCHAR2(100) NOT NULL
);

CREATE TABLE roles (
    rol_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol VARCHAR2(30) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    usuario_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    rol_id NUMBER NOT NULL REFERENCES roles(rol_id),
    activo NUMBER(1) DEFAULT 1 CHECK (activo IN (0, 1)),
    debe_cambiar_pass NUMBER(1) DEFAULT 0 CHECK (debe_cambiar_pass IN (0, 1)),
    fecha_creacion DATE DEFAULT SYSDATE
);

CREATE TABLE estudiantes (
    numero_control VARCHAR2(20) PRIMARY KEY,
    usuario_id NUMBER UNIQUE REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    correo VARCHAR2(100) UNIQUE NOT NULL,
    carrera_id NUMBER NOT NULL REFERENCES carreras(carrera_id),
    promedio NUMBER(4,2) DEFAULT 0.00
);

CREATE TABLE profesores (
    numero_control VARCHAR2(20) PRIMARY KEY,
    usuario_id NUMBER UNIQUE REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    correo VARCHAR2(100) UNIQUE NOT NULL,
    tipo_contratacion VARCHAR2(20) NOT NULL CHECK (tipo_contratacion IN ('BASE', 'HONORARIOS'))
);

CREATE TABLE administrativos (
    numero_control VARCHAR2(20) PRIMARY KEY,
    usuario_id NUMBER UNIQUE REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    correo VARCHAR2(100) UNIQUE NOT NULL
);

CREATE TABLE periodos (
    periodo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    limite_captura_calificaciones DATE NOT NULL
);

CREATE TABLE materias (
    materia_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave VARCHAR2(20) UNIQUE NOT NULL,
    nombre VARCHAR2(100) NOT NULL,
    carrera_id NUMBER NOT NULL REFERENCES carreras(carrera_id)
);

CREATE TABLE grupos (
    grupo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    materia_id NUMBER NOT NULL REFERENCES materias(materia_id),
    profesor_numero_control VARCHAR2(20) REFERENCES profesores(numero_control),
    periodo_id NUMBER NOT NULL REFERENCES periodos(periodo_id),
    nombre_grupo VARCHAR2(20) NOT NULL,
    salon VARCHAR2(20) NOT NULL,
    horario VARCHAR2(100) NOT NULL
);

CREATE TABLE cargas_alumnos (
    carga_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_control_estudiante VARCHAR2(20) NOT NULL REFERENCES estudiantes(numero_control),
    grupo_id NUMBER NOT NULL REFERENCES grupos(grupo_id),
    calificacion NUMBER(5,2),
    estatus VARCHAR2(20) DEFAULT 'CURSANDO'
        CHECK (estatus IN ('CURSANDO', 'APROBADA', 'REPROBADA')),
    oportunidad VARCHAR2(20) DEFAULT 'PRIMERA' NOT NULL
        CHECK (oportunidad IN ('PRIMERA', 'SEGUNDA', 'REPETICION', 'ESPECIAL')),
    CONSTRAINT uk_estudiante_grupo UNIQUE (numero_control_estudiante, grupo_id)
);

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

INSERT INTO roles (nombre_rol) VALUES ('ADMINISTRATIVO');
INSERT INTO roles (nombre_rol) VALUES ('MAESTRO');
INSERT INTO roles (nombre_rol) VALUES ('ESTUDIANTE');
INSERT INTO carreras (clave, nombre)
VALUES ('ISC', 'Ingeniería en Sistemas Computacionales');

-- Insertar usuario administrador inicial para pruebas
INSERT INTO usuarios (username, password_hash, rol_id, activo, debe_cambiar_pass)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, 0);

INSERT INTO administrativos (numero_control, usuario_id, nombre, apellido_paterno, apellido_materno, correo)
VALUES ('ADM001', 1, 'Administrador', 'Principal', 'Sistema', 'admin@escuela.edu.mx');

COMMIT;