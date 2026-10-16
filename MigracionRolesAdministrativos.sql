-- Ejecutar una sola vez en una base existente de MySQL/TiDB.
-- INSERT IGNORE evita duplicar los roles si alguno ya existe.
INSERT IGNORE INTO roles (nombre_rol) VALUES ('ADMINISTRADOR');
INSERT IGNORE INTO roles (nombre_rol) VALUES ('JEFE_DEPARTAMENTO');
