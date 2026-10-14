-- Esquema para MySQL / TiDB Cloud.
-- Ejecutar en un esquema nuevo o vacio, despues de seleccionar la base de datos destino.
-- No es una migracion para una base existente: no elimina tablas ni modifica estructuras actuales.

CREATE TABLE roles (
    rol_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre_rol VARCHAR(30) NOT NULL,
    PRIMARY KEY (rol_id),
    CONSTRAINT uk_roles_nombre_rol UNIQUE (nombre_rol)
) AUTO_INCREMENT = 4;

CREATE TABLE carreras (
    id_carrera BIGINT NOT NULL AUTO_INCREMENT,
    clave_carrera VARCHAR(20) NOT NULL,
    nombre_carrera VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_carrera),
    CONSTRAINT uk_carreras_clave_carrera UNIQUE (clave_carrera)
) AUTO_INCREMENT = 19;

CREATE TABLE edificios (
    id_edificio BIGINT NOT NULL AUTO_INCREMENT,
    clave_edificio VARCHAR(10) NOT NULL,
    nombre_edificio VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_edificio),
    CONSTRAINT uk_edificios_clave_edificio UNIQUE (clave_edificio)
);

CREATE TABLE aulas (
    id_aula BIGINT NOT NULL AUTO_INCREMENT,
    id_edificio BIGINT NOT NULL,
    nombre_aula VARCHAR(30) NOT NULL,
    capacidad DECIMAL(3,0) NOT NULL DEFAULT 30,
    tipo_aula VARCHAR(30) NOT NULL DEFAULT 'TEORICA',
    PRIMARY KEY (id_aula),
    CONSTRAINT chk_aulas_capacidad CHECK (capacidad > 0),
    CONSTRAINT chk_aulas_tipo CHECK (tipo_aula IN ('TEORICA', 'LABORATORIO', 'TALLER', 'AUDITORIO')),
    CONSTRAINT fk_aulas_edificios FOREIGN KEY (id_edificio) REFERENCES edificios(id_edificio)
);

CREATE TABLE perfiles_base (
    numero_control VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50),
    fecha_nacimiento DATE,
    domicilio VARCHAR(200),
    telefono VARCHAR(15),
    correo_institucional VARCHAR(100) NOT NULL,
    activo TINYINT NOT NULL DEFAULT 1,
    debe_cambiar_pass TINYINT NOT NULL DEFAULT 1,
    fecha_creacion DATE DEFAULT (CURRENT_DATE),
    PRIMARY KEY (numero_control),
    CONSTRAINT uk_perfiles_correo UNIQUE (correo_institucional),
    CONSTRAINT chk_perfiles_activo CHECK (activo IN (0, 1)),
    CONSTRAINT chk_perfiles_cambio_pass CHECK (debe_cambiar_pass IN (0, 1)),
    CONSTRAINT chk_perfiles_numero_control CHECK (numero_control REGEXP '^[0-9]+$'),
    CONSTRAINT fk_perfiles_roles FOREIGN KEY (rol_id) REFERENCES roles(rol_id)
);

CREATE TABLE estudiantes (
    numero_control VARCHAR(20) NOT NULL,
    id_carrera BIGINT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    promedio DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    pago_reinscripcion CHAR(1) NOT NULL DEFAULT 'N',
    tiene_adeudos CHAR(1) NOT NULL DEFAULT 'N',
    PRIMARY KEY (numero_control),
    CONSTRAINT chk_estudiantes_promedio CHECK (promedio BETWEEN 0 AND 100),
    CONSTRAINT chk_estudiantes_pago CHECK (pago_reinscripcion IN ('S', 'N')),
    CONSTRAINT chk_estudiantes_adeudos CHECK (tiene_adeudos IN ('S', 'N')),
    CONSTRAINT fk_estudiantes_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    CONSTRAINT fk_estudiantes_carreras FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
);

CREATE TABLE profesores (
    numero_control VARCHAR(20) NOT NULL,
    id_carrera_adscripcion BIGINT NOT NULL,
    tipo_contratacion VARCHAR(20) NOT NULL,
    cubiculo VARCHAR(20),
    PRIMARY KEY (numero_control),
    CONSTRAINT chk_profesores_tipo_contratacion CHECK (tipo_contratacion IN ('BASE', 'HONORARIOS')),
    CONSTRAINT fk_profesores_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE,
    CONSTRAINT fk_profesores_carreras FOREIGN KEY (id_carrera_adscripcion) REFERENCES carreras(id_carrera)
);

CREATE TABLE administrativos (
    numero_control VARCHAR(20) NOT NULL,
    area_departamento VARCHAR(80) NOT NULL,
    cargo_puesto VARCHAR(80),
    PRIMARY KEY (numero_control),
    CONSTRAINT fk_administrativos_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE
);

CREATE TABLE periodos (
    periodo_id BIGINT NOT NULL AUTO_INCREMENT,
    clave_periodo VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    limite_captura_calificaciones DATE NOT NULL,
    captura_activa CHAR(1) NOT NULL DEFAULT 'N',
    PRIMARY KEY (periodo_id),
    CONSTRAINT uk_periodos_clave UNIQUE (clave_periodo),
    CONSTRAINT chk_periodos_captura CHECK (captura_activa IN ('S', 'N')),
    CONSTRAINT chk_periodos_fechas CHECK (fecha_inicio < fecha_fin AND limite_captura_calificaciones >= fecha_inicio)
);

CREATE TABLE materias (
    materia_id BIGINT NOT NULL AUTO_INCREMENT,
    clave_materia VARCHAR(20) NOT NULL,
    nombre_materia VARCHAR(100) NOT NULL,
    creditos DECIMAL(2,0) NOT NULL,
    id_carrera BIGINT NOT NULL,
    PRIMARY KEY (materia_id),
    CONSTRAINT uk_materia_carrera_clave UNIQUE (id_carrera, clave_materia),
    CONSTRAINT uk_materia_id_carrera UNIQUE (materia_id, id_carrera),
    CONSTRAINT chk_materias_creditos CHECK (creditos > 0),
    CONSTRAINT fk_materias_carreras FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
);

CREATE TABLE prerrequisitos_materia (
    materia_id BIGINT NOT NULL,
    materia_prerrequisito_id BIGINT NOT NULL,
    PRIMARY KEY (materia_id, materia_prerrequisito_id),
    CONSTRAINT chk_prerrequisito_distinto CHECK (materia_id <> materia_prerrequisito_id),
    CONSTRAINT fk_prerrequisitos_materia FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_prerrequisitos_materia_previa FOREIGN KEY (materia_prerrequisito_id) REFERENCES materias(materia_id)
);

CREATE TABLE grupos (
    grupo_id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_grupo VARCHAR(20) NOT NULL,
    materia_id BIGINT NOT NULL,
    profesor_numero_control VARCHAR(20),
    periodo_id BIGINT NOT NULL,
    PRIMARY KEY (grupo_id),
    CONSTRAINT uk_codigo_materia_periodo_grupo UNIQUE (codigo_grupo, materia_id, periodo_id),
    CONSTRAINT fk_grupos_materias FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_grupos_profesores FOREIGN KEY (profesor_numero_control) REFERENCES profesores(numero_control),
    CONSTRAINT fk_grupos_periodos FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id)
);

CREATE TABLE horarios_clase (
    horario_id BIGINT NOT NULL AUTO_INCREMENT,
    grupo_id BIGINT NOT NULL,
    id_aula BIGINT NOT NULL,
    dia_semana TINYINT NOT NULL,
    hora_inicio VARCHAR(5) NOT NULL,
    hora_fin VARCHAR(5) NOT NULL,
    PRIMARY KEY (horario_id),
    CONSTRAINT chk_horarios_dia CHECK (dia_semana BETWEEN 1 AND 6),
    CONSTRAINT chk_horarios_horas CHECK (
        hora_inicio REGEXP '^([01][0-9]|2[0-3]):[0-5][0-9]$'
        AND hora_fin REGEXP '^([01][0-9]|2[0-3]):[0-5][0-9]$'
        AND hora_inicio < hora_fin
    ),
    CONSTRAINT fk_horarios_grupos FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id) ON DELETE CASCADE,
    CONSTRAINT fk_horarios_aulas FOREIGN KEY (id_aula) REFERENCES aulas(id_aula)
);

CREATE TABLE cargas_academicas (
    carga_id BIGINT NOT NULL AUTO_INCREMENT,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    grupo_id BIGINT NOT NULL,
    calificacion_parcial1 DECIMAL(5,2),
    calificacion_parcial2 DECIMAL(5,2),
    calificacion_final DECIMAL(5,2),
    estatus VARCHAR(20) NOT NULL DEFAULT 'CURSANDO',
    oportunidad VARCHAR(20) NOT NULL DEFAULT 'PRIMERA',
    PRIMARY KEY (carga_id),
    CONSTRAINT uk_estudiante_grupo UNIQUE (numero_control_estudiante, grupo_id),
    CONSTRAINT chk_cargas_calificacion1 CHECK (calificacion_parcial1 IS NULL OR calificacion_parcial1 BETWEEN 0 AND 100),
    CONSTRAINT chk_cargas_calificacion2 CHECK (calificacion_parcial2 IS NULL OR calificacion_parcial2 BETWEEN 0 AND 100),
    CONSTRAINT chk_cargas_calificacion_final CHECK (calificacion_final IS NULL OR calificacion_final BETWEEN 0 AND 100),
    CONSTRAINT chk_cargas_estatus CHECK (estatus IN ('CURSANDO', 'APROBADA', 'REPROBADA', 'BAJA')),
    CONSTRAINT chk_cargas_oportunidad CHECK (oportunidad IN ('PRIMERA', 'SEGUNDA', 'REPETICION', 'ESPECIAL')),
    CONSTRAINT fk_cargas_estudiantes FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_cargas_grupos FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id)
);

CREATE TABLE criterios_evaluacion (
    criterio_id BIGINT NOT NULL AUTO_INCREMENT,
    grupo_id BIGINT NOT NULL,
    nombre_criterio VARCHAR(50) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (criterio_id),
    CONSTRAINT uk_criterio_grupo_nombre UNIQUE (grupo_id, nombre_criterio),
    CONSTRAINT chk_criterios_porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT fk_criterios_grupos FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id) ON DELETE CASCADE
);

CREATE TABLE calificaciones_detalladas (
    calificacion_det_id BIGINT NOT NULL AUTO_INCREMENT,
    carga_id BIGINT NOT NULL,
    criterio_id BIGINT NOT NULL,
    calificacion DECIMAL(5,2),
    PRIMARY KEY (calificacion_det_id),
    CONSTRAINT uk_carga_criterio UNIQUE (carga_id, criterio_id),
    CONSTRAINT chk_calificaciones_detalladas CHECK (calificacion IS NULL OR calificacion BETWEEN 0 AND 100),
    CONSTRAINT fk_calificaciones_cargas FOREIGN KEY (carga_id) REFERENCES cargas_academicas(carga_id) ON DELETE CASCADE,
    CONSTRAINT fk_calificaciones_criterios FOREIGN KEY (criterio_id) REFERENCES criterios_evaluacion(criterio_id)
);

CREATE TABLE kardex_estudiante (
    kardex_id BIGINT NOT NULL AUTO_INCREMENT,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    materia_id BIGINT NOT NULL,
    periodo_id BIGINT NOT NULL,
    calificacion_final DECIMAL(5,2) NOT NULL,
    tipo_acreditacion VARCHAR(20) NOT NULL DEFAULT 'ORDINARIO',
    estatus VARCHAR(15) NOT NULL,
    creditos_obtenidos DECIMAL(2,0) NOT NULL,
    PRIMARY KEY (kardex_id),
    CONSTRAINT uk_estudiante_mat_per UNIQUE (numero_control_estudiante, materia_id, periodo_id),
    CONSTRAINT chk_kardex_calificacion CHECK (calificacion_final BETWEEN 0 AND 100),
    CONSTRAINT chk_kardex_tipo CHECK (tipo_acreditacion IN ('ORDINARIO', 'EXTRAORDINARIO', 'REPETICION', 'CONVALIDACION')),
    CONSTRAINT chk_kardex_estatus CHECK (estatus IN ('APROBADA', 'REPROBADA')),
    CONSTRAINT fk_kardex_estudiantes FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_kardex_materias FOREIGN KEY (materia_id) REFERENCES materias(materia_id),
    CONSTRAINT fk_kardex_periodos FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id)
);

CREATE TABLE logs_auditoria (
    id_log BIGINT NOT NULL AUTO_INCREMENT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_control VARCHAR(20),
    rol VARCHAR(30),
    operacion VARCHAR(50) NOT NULL,
    detalles VARCHAR(1000),
    ip_origen VARCHAR(45),
    PRIMARY KEY (id_log)
);

CREATE TABLE notificaciones (
    id_notificacion BIGINT NOT NULL AUTO_INCREMENT,
    numero_control VARCHAR(20) NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id_notificacion),
    CONSTRAINT chk_notificaciones_leida CHECK (leida IN (0, 1)),
    CONSTRAINT fk_notificaciones_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control) ON DELETE CASCADE
);

CREATE TABLE solicitudes_tramite (
    id_solicitud BIGINT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(50) NOT NULL,
    numero_control VARCHAR(20) NOT NULL,
    tipo_tramite VARCHAR(50) NOT NULL,
    motivo VARCHAR(500),
    estatus VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    grupo_id BIGINT,
    respuesta_admin VARCHAR(500),
    PRIMARY KEY (id_solicitud),
    CONSTRAINT uk_solicitudes_folio UNIQUE (folio),
    CONSTRAINT fk_solicitudes_perfiles FOREIGN KEY (numero_control) REFERENCES perfiles_base(numero_control),
    CONSTRAINT fk_solicitudes_grupos FOREIGN KEY (grupo_id) REFERENCES grupos(grupo_id)
);

CREATE TABLE turnos_inscripcion (
    turno_id BIGINT NOT NULL AUTO_INCREMENT,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    periodo_id BIGINT NOT NULL,
    fecha_hora_inscripcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estatus VARCHAR(15) NOT NULL DEFAULT 'ACTIVA',
    PRIMARY KEY (turno_id),
    CONSTRAINT uk_estudiante_periodo_turno UNIQUE (numero_control_estudiante, periodo_id),
    CONSTRAINT chk_turnos_estatus CHECK (estatus IN ('ACTIVA', 'CANCELADA')),
    CONSTRAINT fk_turnos_estudiantes FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_turnos_periodos FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id)
);

CREATE TABLE vouchers_servicios (
    voucher_id BIGINT NOT NULL AUTO_INCREMENT,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    concepto VARCHAR(100) NOT NULL,
    monto DECIMAL(8,2) NOT NULL,
    folio VARCHAR(50) NOT NULL,
    fecha_emision DATE DEFAULT (CURRENT_DATE),
    estatus VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (voucher_id),
    CONSTRAINT uk_vouchers_folio UNIQUE (folio),
    CONSTRAINT chk_vouchers_monto CHECK (monto >= 0),
    CONSTRAINT chk_vouchers_estatus CHECK (estatus IN ('PENDIENTE', 'PAGADO', 'CANCELADO')),
    CONSTRAINT fk_vouchers_estudiantes FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control)
);

CREATE TABLE constancias_estudio (
    constancia_id BIGINT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(50) NOT NULL,
    numero_control_estudiante VARCHAR(20) NOT NULL,
    numero_control_administrativo VARCHAR(20) NOT NULL,
    periodo_id BIGINT NOT NULL,
    semestre_al_emitir DECIMAL(2,0) NOT NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estatus VARCHAR(15) NOT NULL DEFAULT 'EMITIDA',
    motivo_cancelacion VARCHAR(250),
    PRIMARY KEY (constancia_id),
    CONSTRAINT uk_constancias_folio UNIQUE (folio),
    CONSTRAINT chk_constancias_semestre CHECK (semestre_al_emitir BETWEEN 1 AND 12),
    CONSTRAINT chk_constancias_estatus CHECK (estatus IN ('EMITIDA', 'CANCELADA')),
    CONSTRAINT chk_constancias_cancelacion CHECK (
        (estatus = 'CANCELADA' AND motivo_cancelacion IS NOT NULL)
        OR (estatus = 'EMITIDA' AND motivo_cancelacion IS NULL)
    ),
    CONSTRAINT fk_constancias_estudiantes FOREIGN KEY (numero_control_estudiante) REFERENCES estudiantes(numero_control),
    CONSTRAINT fk_constancias_administrativos FOREIGN KEY (numero_control_administrativo) REFERENCES administrativos(numero_control),
    CONSTRAINT fk_constancias_periodos FOREIGN KEY (periodo_id) REFERENCES periodos(periodo_id)
);
-- =========================================================================
-- SCRIPT DE DATOS INICIALES
-- =========================================================================

INSERT INTO roles (rol_id, nombre_rol) VALUES (1, 'ADMINISTRATIVO');
INSERT INTO roles (rol_id, nombre_rol) VALUES (2, 'PROFESOR');
INSERT INTO roles (rol_id, nombre_rol) VALUES (3, 'ESTUDIANTE');
INSERT INTO roles (rol_id, nombre_rol) VALUES (4, 'JEFE_DEPARTAMENTO');

INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (1, 'ISC', 'Ingeniería en Sistemas Computacionales');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (2, 'MEC', 'Ingeniería Mecatrónica');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (3, 'QUI', 'Ingeniería Química');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (4, 'LOG', 'Ingeniería en Logística');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (5, 'IGE', 'Ingeniería en Gestión Empresarial');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (6, 'TICs', 'Ingeniería en Tecnologías de la Información y Comunicaciones');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (7, 'IEM', 'Ingeniería Electromecánica');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (8, 'IELE', 'Ingeniería Electrónica');
INSERT INTO carreras (id_carrera, clave_carrera, nombre_carrera) VALUES (9, 'IND', 'Ingeniería Industrial');

-- Cuentas de prueba
INSERT INTO perfiles_base (
    numero_control, password_hash, rol_id, nombre, apellido_paterno,
    apellido_materno, correo_institucional, activo, debe_cambiar_pass
) VALUES (
    '22280120', '$2b$12$i5aQYn6MmACOme5xsv7LzeAtGL3QdqOrVd08S9NgRiuPMi9FvtsAi',
    3, 'Mario', 'Mora', 'Martinez', 'L22280120@escuela.com', 1, 1
);

INSERT INTO estudiantes (numero_control, id_carrera, fecha_ingreso, pago_reinscripcion, tiene_adeudos
) VALUES ('22280120', 1, DATE '2026-01-01', 'N', 'N');

-- 22280120 no.control
-- PW_22280120 es la contraseña


INSERT INTO perfiles_base (
    numero_control, password_hash, rol_id, nombre, apellido_paterno,
    apellido_materno, correo_institucional, activo, debe_cambiar_pass
) VALUES (
    '10280070', '$2b$12$UFdljSoPFp3aue5Jd4pRSuwD5nGFqj4V5HCvyO5AOK/ljrqk7kCdO',
    1, 'Administrativo', 'Prueba', 'Campus', 'administrativo.prueba@securecampus.test', 1, 1
);
INSERT INTO administrativos (numero_control, area_departamento, cargo_puesto)
VALUES ('10280070', 'Servicios Escolares', 'Personal de prueba');

-- 10280070 no.control
-- PW_10280070 es la contraseña

INSERT INTO perfiles_base (
    numero_control, password_hash, rol_id, nombre, apellido_paterno,
    apellido_materno, correo_institucional, activo, debe_cambiar_pass
) VALUES (
    '19280703', '$2b$12$6jTclHMl7PPmMCWI63byIuFuzOR6DTfMYUu2d/qOC3EuGkT5yvlyO',
    2, 'Profesor', 'Prueba', 'Electromecánica', 'profesor.prueba@securecampus.test', 1, 1
);
INSERT INTO profesores (
    numero_control, id_carrera_adscripcion, tipo_contratacion, cubiculo
) VALUES ('19280703', 7, 'BASE', 'P-001');

-- 19280703 no.control
-- PW_19280703 es la contraseña

INSERT INTO perfiles_base (
    numero_control, password_hash, rol_id, nombre, apellido_paterno,
    apellido_materno, correo_institucional, activo, debe_cambiar_pass
) VALUES (
    '10280071', '$2a$10$2ARVzjkr33Od.ybe7v5MkOAJIF2Dzlv9yEcFYlEJP2seXP5AAT8Py',
    4, 'Jefe', 'Departamento', 'Prueba', 'jefe.departamento@securecampus.test', 1, 1
);

-- 10280071 no.control
-- Jefe_2026! es la contraseña; se recomienda cambiarla después de probar.


-- =========================================================================
-- SCRIPT AMPLIADO: MATERIAS Y SERIACIONES COMPLETAS (MYSQL / TIDB)
-- =========================================================================

-- =========================================================================
-- 1. INGENIERÍA EN SISTEMAS COMPUTACIONALES (id_carrera = 1)[cite: 11]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-101', 'Cálculo Diferencial', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-102', 'Cálculo Integral', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-103', 'Cálculo Vectorial', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-104', 'Ecuaciones Diferenciales', 4, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-201', 'Fundamentos de Programación', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-202', 'Programación Orientada a Objetos', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-203', 'Estructura de Datos', 5, 1);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ISC-204', 'Tópicos Avanzados de Programación', 5, 1);


-- =========================================================================
-- 2. INGENIERÍA MECATRÓNICA (id_carrera = 2)[cite: 8]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-101', 'Cálculo Diferencial', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-102', 'Cálculo Integral', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-103', 'Cálculo Vectorial', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-104', 'Ecuaciones Diferenciales', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-105', 'Estática', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-106', 'Dinámica', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-107', 'Mecanismos', 5, 2);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('MEC-108', 'Diseño de Elementos Mecánicos', 5, 2);


-- =========================================================================
-- 3. INGENIERÍA QUÍMICA (id_carrera = 3)[cite: 9]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-101', 'Cálculo Diferencial', 5, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-102', 'Cálculo Integral', 5, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-103', 'Cálculo Vectorial', 5, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-104', 'Química Inorgánica', 4, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-105', 'Química Orgánica I', 5, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-106', 'Química Orgánica II', 5, 3);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('QUI-107', 'Mecanismos de Transferencia', 5, 3);


-- =========================================================================
-- 4. INGENIERÍA EN LOGÍSTICA (id_carrera = 4)[cite: 6]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-101', 'Introd a la Ing. en Logística', 4, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-102', 'Cadena de Suministro', 4, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-103', 'Cálculo Diferencial', 5, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-104', 'Cálculo Integral', 5, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-105', 'Probabilidad y Estadística', 4, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-106', 'Estadística Inferencial I', 5, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-107', 'Estadística Inferencial II', 5, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-108', 'Inventarios', 4, 4);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('LOG-109', 'Almacenes', 5, 4);


-- =========================================================================
-- 5. INGENIERÍA EN GESTIÓN EMPRESARIAL (id_carrera = 5)[cite: 5]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-101', 'Fundamentos de Investigación', 4, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-102', 'Cálculo Diferencial', 5, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-103', 'Cálculo Integral', 5, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-104', 'Probabilidad y Estadística Descriptiva', 5, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-105', 'Estadística Inferencial I', 6, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-106', 'Estadística Inferencial II', 6, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-107', 'Ingeniería Económica', 5, 5);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IGE-108', 'Finanzas en las Organizaciones', 5, 5);


-- =========================================================================
-- 6. INGENIERÍA EN TICs (id_carrera = 6)[cite: 11]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-101', 'Cálculo Diferencial', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-102', 'Cálculo Integral', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-103', 'Fundamentos de Programación', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-104', 'Programación Orientada a Objetos', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-105', 'Matemáticas Aplicadas', 4, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-106', 'Análisis de Señales', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-107', 'Telecomunicaciones', 5, 6);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('TIC-108', 'Redes Emergentes', 5, 6);


-- =========================================================================
-- 7. INGENIERÍA ELECTROMECÁNICA (id_carrera = 7)
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-101', 'Cálculo Diferencial', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-102', 'Cálculo Integral', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-103', 'Cálculo Vectorial', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-104', 'Ecuaciones Diferenciales', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-105', 'Estática', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-106', 'Dinámica', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-107', 'Mecánica de Materiales', 4, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-108', 'Circuitos Eléctricos I', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-109', 'Circuitos Eléctricos II', 5, 7);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IEM-110', 'Máquinas Eléctricas', 5, 7);


-- =========================================================================
-- 8. INGENIERÍA ELECTRÓNICA (id_carrera = 8)[cite: 4]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-101', 'Cálculo Diferencial', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-102', 'Cálculo Integral', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-103', 'Álgebra Lineal', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-104', 'Cálculo Vectorial', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-105', 'Ecuaciones Diferenciales', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-106', 'Introducción a las Telecomunicaciones', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-107', 'Control I', 5, 8);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('ELE-108', 'Control II', 5, 8);


-- =========================================================================
-- 9. INGENIERÍA INDUSTRIAL (id_carrera = 9)[cite: 7]
-- =========================================================================
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-101', 'Cálculo Diferencial', 5, 9);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-102', 'Cálculo Integral', 5, 9);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-103', 'Probabilidad y Estadística', 4, 9);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-104', 'Estadística Inferencial I', 5, 9);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-105', 'Estadística Inferencial II', 5, 9);
INSERT INTO materias (clave_materia, nombre_materia, creditos, id_carrera) VALUES ('IND-106', 'Control Estadístico de la Calidad', 5, 9);


-- =========================================================================
-- ASIGNACIÓN DE PRERREQUISITOS (SERIACIÓN ESTRICTA)
-- =========================================================================

-- Sistemas
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ISC-102' AND m2.clave_materia = 'ISC-101' AND m1.id_carrera=1 AND m2.id_carrera=1;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ISC-103' AND m2.clave_materia = 'ISC-102' AND m1.id_carrera=1 AND m2.id_carrera=1;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ISC-202' AND m2.clave_materia = 'ISC-201' AND m1.id_carrera=1 AND m2.id_carrera=1;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ISC-203' AND m2.clave_materia = 'ISC-202' AND m1.id_carrera=1 AND m2.id_carrera=1;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ISC-204' AND m2.clave_materia = 'ISC-203' AND m1.id_carrera=1 AND m2.id_carrera=1;

-- Mecatrónica
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'MEC-102' AND m2.clave_materia = 'MEC-101' AND m1.id_carrera=2 AND m2.id_carrera=2;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'MEC-103' AND m2.clave_materia = 'MEC-102' AND m1.id_carrera=2 AND m2.id_carrera=2;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'MEC-106' AND m2.clave_materia = 'MEC-105' AND m1.id_carrera=2 AND m2.id_carrera=2;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'MEC-107' AND m2.clave_materia = 'MEC-106' AND m1.id_carrera=2 AND m2.id_carrera=2;

-- Química
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'QUI-102' AND m2.clave_materia = 'QUI-101' AND m1.id_carrera=3 AND m2.id_carrera=3;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'QUI-105' AND m2.clave_materia = 'QUI-104' AND m1.id_carrera=3 AND m2.id_carrera=3;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'QUI-106' AND m2.clave_materia = 'QUI-105' AND m1.id_carrera=3 AND m2.id_carrera=3;

-- Logística
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'LOG-104' AND m2.clave_materia = 'LOG-103' AND m1.id_carrera=4 AND m2.id_carrera=4;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'LOG-106' AND m2.clave_materia = 'LOG-105' AND m1.id_carrera=4 AND m2.id_carrera=4;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'LOG-107' AND m2.clave_materia = 'LOG-106' AND m1.id_carrera=4 AND m2.id_carrera=4;

-- Gestión Empresarial
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IGE-103' AND m2.clave_materia = 'IGE-102' AND m1.id_carrera=5 AND m2.id_carrera=5;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IGE-105' AND m2.clave_materia = 'IGE-104' AND m1.id_carrera=5 AND m2.id_carrera=5;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IGE-106' AND m2.clave_materia = 'IGE-105' AND m1.id_carrera=5 AND m2.id_carrera=5;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IGE-108' AND m2.clave_materia = 'IGE-107' AND m1.id_carrera=5 AND m2.id_carrera=5;

-- TICs
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'TIC-102' AND m2.clave_materia = 'TIC-101' AND m1.id_carrera=6 AND m2.id_carrera=6;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'TIC-104' AND m2.clave_materia = 'TIC-103' AND m1.id_carrera=6 AND m2.id_carrera=6;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'TIC-106' AND m2.clave_materia = 'TIC-105' AND m1.id_carrera=6 AND m2.id_carrera=6;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'TIC-108' AND m2.clave_materia = 'TIC-107' AND m1.id_carrera=6 AND m2.id_carrera=6;

-- Electromecánica
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-102' AND m2.clave_materia = 'IEM-101' AND m1.id_carrera=7 AND m2.id_carrera=7;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-103' AND m2.clave_materia = 'IEM-102' AND m1.id_carrera=7 AND m2.id_carrera=7;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-104' AND m2.clave_materia = 'IEM-103' AND m1.id_carrera=7 AND m2.id_carrera=7;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-106' AND m2.clave_materia = 'IEM-105' AND m1.id_carrera=7 AND m2.id_carrera=7;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-109' AND m2.clave_materia = 'IEM-108' AND m1.id_carrera=7 AND m2.id_carrera=7;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IEM-110' AND m2.clave_materia = 'IEM-109' AND m1.id_carrera=7 AND m2.id_carrera=7;

-- Electrónica
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ELE-102' AND m2.clave_materia = 'ELE-101' AND m1.id_carrera=8 AND m2.id_carrera=8;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ELE-104' AND m2.clave_materia = 'ELE-103' AND m1.id_carrera=8 AND m2.id_carrera=8;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ELE-105' AND m2.clave_materia = 'ELE-104' AND m1.id_carrera=8 AND m2.id_carrera=8;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'ELE-108' AND m2.clave_materia = 'ELE-107' AND m1.id_carrera=8 AND m2.id_carrera=8;

-- Industrial
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IND-102' AND m2.clave_materia = 'IND-101' AND m1.id_carrera=9 AND m2.id_carrera=9;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IND-104' AND m2.clave_materia = 'IND-103' AND m1.id_carrera=9 AND m2.id_carrera=9;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IND-105' AND m2.clave_materia = 'IND-104' AND m1.id_carrera=9 AND m2.id_carrera=9;
INSERT INTO prerrequisitos_materia (materia_id, materia_prerrequisito_id)
SELECT m1.materia_id, m2.materia_id FROM materias m1, materias m2 WHERE m1.clave_materia = 'IND-106' AND m2.clave_materia = 'IND-105' AND m1.id_carrera=9 AND m2.id_carrera=9;

COMMIT;
