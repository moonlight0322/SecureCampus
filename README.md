# SecureCampus

Portal académico del Instituto Tecnológico de Toluca con acceso institucional y vistas para estudiantes.

## Requisitos

- Java 21
- TiDB Cloud (compatible con el driver MySQL) o MySQL 8.0.16+
- Maven (o el wrapper `./mvnw`)
- Conexión a la base MySQL/TiDB para iniciar sesión y consultar datos académicos

## Preparar TiDB Cloud o MySQL

Selecciona una base de datos vacía en TiDB Cloud o MySQL y ejecuta `CreacionBD.sql` con el cliente `mysql`. El script crea el esquema académico y los datos iniciales; no elimina tablas ni importa automáticamente filas de una base Oracle existente.

```bash
mysql -h <host> -P <puerto> -u <usuario> -p <base_de_datos> < CreacionBD.sql
```

Configura las variables de conexión que usa Spring Boot. Para MySQL local, por ejemplo:

```bash
export DB_URL='jdbc:mysql://localhost:3306/securecampus?serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='tu-contraseña'
```

Para TiDB Cloud, usa el host, puerto, base de datos y usuario del panel de conexión de TiDB; configura el TLS requerido por tu clúster en `DB_URL`. No guardes contraseñas en el repositorio.

Las cuentas se validan contra `perfiles_base.numero_control`, `perfiles_base.password_hash` y el rol asociado en `roles`. El script contiene registros de prueba; configura y cambia sus contraseñas antes de usar esos datos en cualquier entorno compartido. No publiques contraseñas, tokens ni credenciales de bases de datos.

Si la base TiDB/MySQL ya existe y no tiene `estudiantes.fecha_ingreso`, agrega la columna antes de insertar el estudiante:

```sql
ALTER TABLE estudiantes
ADD COLUMN fecha_ingreso DATE NOT NULL DEFAULT '2026-01-01';
```

El formulario envía `POST /login`; Spring Security procesa la autenticación, valida BCrypt, carga los roles y rechaza las cuentas inactivas. No se requiere un `@PostMapping` adicional: el filtro de formulario de Spring Security es el punto de entrada del login. Las tablas específicas (`estudiantes`, `profesores` y `administrativos`) se relacionan con `perfiles_base` por número de control.

TiDB Cloud no soporta triggers. Por eso, las reglas que requieren comparar varias tablas (por ejemplo, que un prerrequisito sea de la misma carrera, que el criterio pertenezca al grupo de la carga o que una constancia requiera una inscripción vigente) deben validarse desde la lógica de la aplicación.

## Ejecutar

```bash
./mvnw spring-boot:run
```

Abre <http://localhost:8080>. Los módulos disponibles incluyen:

- **Estudiante:** resumen, horario, kárdex, solicitudes y descarga de documentos PDF. La constancia requiere reinscripción vigente (`pago_reinscripcion = 'S'`).
- **Profesor:** grupos asignados, captura de dos parciales, promedio automático, solicitud de desbloqueo, lista oficial PDF y horario.
- **Jefe de departamento:** alta de materias y creación/asignación de grupos a docentes.
- **Administración:** alta de usuarios y su perfil académico, activación de cuentas, asignación de grupos, atención de solicitudes y restablecimiento de contraseñas.

Todos los roles autenticados pueden cambiar su propia contraseña en **Contraseña**. El administrador puede fijar una contraseña temporal; debe compartirla por un canal seguro. La recuperación automática por correo todavía no está habilitada. Las sesiones terminan después de 10 minutos sin actividad. Los PDF se generan localmente y se paginan cuando el documento lo requiere.

Para validar el proyecto:

```powershell
.\mvnw.cmd test
```
