-- SecureCampus - esquema para MySQL / TiDB
-- Ejecutar completo en el SQL Editor de TiDB Cloud, DBeaver o MySQL Workbench.

CREATE DATABASE IF NOT EXISTS securecampus
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE securecampus;

CREATE TABLE carreras (
    carrera_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE roles (
    rol_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    usuario_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    activo TINYINT(1) DEFAULT 1,
    debe_cambiar_pass TINYINT(1) DEFAULT 0,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(rol_id),
    CONSTRAINT ck_usuarios_activo CHECK (activo IN (0, 1)),
    CONSTRAINT ck_usuarios_cambiar_pass CHECK (debe_cambiar_pass IN (0, 1))
);

CREATE TABLE estudiantes (
    numero_control VARCHAR(20) PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50),
    correo VARCHAR(100) NOT NULL UNIQUE,
    carrera_id BIGINT NOT NULL,
    promedio DECIMAL(4,2) DEFAULT 0.00,
    CONSTRAINT fk_estudiantes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    CONSTRAINT fk_estudiantes_carrera FOREIGN KEY (carrera_id)
        REFERENCES carreras(carrera_id)
);

CREATE TABLE profesores (
    numero_control VARCHAR(20) PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50),
    correo VARCHAR(100) NOT NULL UNIQUE,
    tipo_contratacion VARCHAR(20) NOT NULL,
    CONSTRAINT fk_profesores_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    CONSTRAINT ck_profesores_tipo CHECK (tipo_contratacion IN ('BASE', 'HONORARIOS'))
);

CREATE TABLE administrativos (
    numero_control VARCHAR(20) PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50),
    correo VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT fk_administrativos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(usuario_id) ON DELETE CASCADE
);

CREATE TABLE periodos (
    periodo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    limite_captura_calificaciones DATE NOT NULL
);

CREATE TABLE materias (
    materia_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    carrera_id BIGINT NOT NULL,
    CONSTRAINT fk_materias_carrera FOREIGN KEY (carrera_id)
        REFERENCES carreras(carrera_id)
);

CREATE TABLE grupos (
    grupo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    materia_id BIGINT NOT NULL,
    profesor_numero_control VARCHAR(20),
    periodo_id BIGINT NOT NULL,
    nombre_grupo VARCHAR(20) NOT NULL,
    salon VARCHAR(20) NOT NULL,
    horario VARCHAR(100) NOT NULL,
    CONSTRAINT fk_grupos_materia FOREIGN KEY (materia_id)
        REFERENCES materias(materia_id),
    CONSTRAINT fk_grupos_profesor FOREIGN KEY (profesor_numero_control)
        REFERENCES profesores(numero_control),
    CONSTRAINT fk_grupos_periodo FOREIGN KEY (periodo_id)
        REFERENCES periodos(periodo_id)
);

CREATE TABLE cargas_alumnos (
    carga_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    grupo_id BIGINT NOT NULL,
    calificacion DECIMAL(5,2),
    estatus VARCHAR(20) DEFAULT 'CURSANDO',
    oportunidad VARCHAR(20) NOT NULL DEFAULT 'PRIMERA',
    CONSTRAINT fk_cargas_estudiante FOREIGN KEY (numero_control_estudiante)
        REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_cargas_grupo FOREIGN KEY (grupo_id)
        REFERENCES grupos(grupo_id),
    CONSTRAINT ck_cargas_estatus CHECK (estatus IN ('CURSANDO', 'APROBADA', 'REPROBADA')),
    CONSTRAINT ck_cargas_oportunidad CHECK (oportunidad IN ('PRIMERA', 'SEGUNDA', 'REPETICION', 'ESPECIAL')),
    CONSTRAINT uk_estudiante_grupo UNIQUE (numero_control_estudiante, grupo_id)
);

CREATE TABLE turnos_inscripcion (
    turno_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    periodo_id BIGINT NOT NULL,
    fecha_hora_inscripcion DATETIME NOT NULL,
    CONSTRAINT fk_turnos_estudiante FOREIGN KEY (numero_control_estudiante)
        REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_turnos_periodo FOREIGN KEY (periodo_id)
        REFERENCES periodos(periodo_id),
    CONSTRAINT uk_estudiante_periodo_turno UNIQUE (numero_control_estudiante, periodo_id)
);

CREATE TABLE vouchers_servicios (
    voucher_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    concepto VARCHAR(100) NOT NULL,
    monto DECIMAL(8,2) NOT NULL,
    folio VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    estatus VARCHAR(20) DEFAULT 'PENDIENTE',
    CONSTRAINT fk_vouchers_estudiante FOREIGN KEY (numero_control_estudiante)
        REFERENCES estudiantes(numero_control),
    CONSTRAINT ck_vouchers_estatus CHECK (estatus IN ('PENDIENTE', 'PAGADO', 'CANCELADO'))
);

INSERT INTO roles (nombre_rol) VALUES ('ADMINISTRATIVO');
INSERT INTO roles (nombre_rol) VALUES ('MAESTRO');
INSERT INTO roles (nombre_rol) VALUES ('ESTUDIANTE');
INSERT INTO carreras (clave, nombre)
VALUES ('ISC', 'Ingeniería en Sistemas Computacionales');

-- Datos de prueba para SecureCampus (ejecutar DESPUÉS de CreacionBD_mysql.sql)
-- Login:  usuario  estudiante1   contraseña  Estudiante123

USE securecampus;

INSERT INTO usuarios (username, password_hash, rol_id, activo, debe_cambiar_pass)
VALUES ('estudiante1',
        '$2b$12$qhaYXlyQwwx0grreXaokguFH6Yc80rNRBPJmpv0yU3/FQygn7ejdu',
        (SELECT rol_id FROM roles WHERE nombre_rol = 'ESTUDIANTE'),
        1, 0);

INSERT INTO estudiantes (numero_control, usuario_id, nombre, apellido_paterno, apellido_materno, correo, carrera_id, promedio)
VALUES ('20210001',
        (SELECT usuario_id FROM usuarios WHERE username = 'estudiante1'),
        'Luis', 'Rivera', 'Cruz', 'luis@example.com',
        (SELECT carrera_id FROM carreras WHERE clave = 'ISC'),
        88.50);

INSERT INTO profesores (numero_control, usuario_id, nombre, apellido_paterno, apellido_materno, correo, tipo_contratacion)
VALUES ('P0001', NULL, 'Ana', 'García', 'López', 'ana@example.com', 'BASE');

INSERT INTO periodos (nombre, fecha_inicio, fecha_fin, limite_captura_calificaciones) VALUES
('Enero-Junio 2026',  '2026-01-19', '2026-06-12', '2026-06-19'),
('Agosto-Diciembre 2026', '2026-08-17', '2026-12-11', '2026-12-18');

INSERT INTO materias (clave, nombre, carrera_id)
SELECT 'ISC101', 'Programación Orientada a Objetos', carrera_id FROM carreras WHERE clave = 'ISC';
INSERT INTO materias (clave, nombre, carrera_id)
SELECT 'ISC102', 'Bases de Datos', carrera_id FROM carreras WHERE clave = 'ISC';
INSERT INTO materias (clave, nombre, carrera_id)
SELECT 'ISC103', 'Estructura de Datos', carrera_id FROM carreras WHERE clave = 'ISC';

-- Periodo anterior (aparece en el kárdex)
INSERT INTO grupos (materia_id, profesor_numero_control, periodo_id, nombre_grupo, salon, horario)
SELECT m.materia_id, 'P0001', p.periodo_id, 'A', 'B-101', 'Lun-Mie 08:00-10:00'
FROM materias m, periodos p WHERE m.clave = 'ISC103' AND p.nombre = 'Enero-Junio 2026';

-- Periodo actual (aparece en el horario)
INSERT INTO grupos (materia_id, profesor_numero_control, periodo_id, nombre_grupo, salon, horario)
SELECT m.materia_id, 'P0001', p.periodo_id, 'A', 'B-102', 'Lun-Mie 10:00-12:00'
FROM materias m, periodos p WHERE m.clave = 'ISC101' AND p.nombre = 'Agosto-Diciembre 2026';
INSERT INTO grupos (materia_id, profesor_numero_control, periodo_id, nombre_grupo, salon, horario)
SELECT m.materia_id, 'P0001', p.periodo_id, 'B', 'LAB-2', 'Mar-Jue 08:00-10:00'
FROM materias m, periodos p WHERE m.clave = 'ISC102' AND p.nombre = 'Agosto-Diciembre 2026';

INSERT INTO cargas_alumnos (numero_control_estudiante, grupo_id, calificacion, estatus, oportunidad)
SELECT '20210001', g.grupo_id, 90.00, 'APROBADA', 'PRIMERA'
FROM grupos g JOIN materias m ON m.materia_id = g.materia_id WHERE m.clave = 'ISC103';
INSERT INTO cargas_alumnos (numero_control_estudiante, grupo_id, calificacion, estatus, oportunidad)
SELECT '20210001', g.grupo_id, NULL, 'CURSANDO', 'PRIMERA'
FROM grupos g JOIN materias m ON m.materia_id = g.materia_id WHERE m.clave IN ('ISC101', 'ISC102');

USE securecampus;
UPDATE usuarios SET username = '20210001' WHERE username = 'estudiante1';ˆ
