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

La aplicación necesita conectarse a una base MySQL/TiDB para iniciar sesión y cargar los datos. No basta con ejecutar Spring Boot: si no se configuran variables, intenta conectarse a `localhost`, lo que requiere tener MySQL instalado y en ejecución en esa computadora.

En Windows PowerShell, para una base local:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/securecampus?serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<contraseña-de-MySQL>"
```

Si no tienes MySQL local, usa la URL, el usuario y la contraseña que proporcione la persona que administra la base remota. Para TiDB Cloud, la URL normalmente tiene este formato:

```powershell
$env:DB_URL = "jdbc:mysql://<host>:<puerto>/<base>?sslMode=VERIFY_IDENTITY"
$env:DB_USERNAME = "<usuario-de-la-base>"
$env:DB_PASSWORD = "<contraseña-de-la-base>"
```

Reemplaza los valores entre `< >` con los datos del proveedor. Ejecuta después la aplicación desde esa misma ventana de PowerShell; las variables definidas con `$env:` no se comparten al clonar el repositorio ni se conservan al cerrar la ventana:

```powershell
.\mvnw.cmd spring-boot:run
```

Las credenciales de conexión a MySQL/TiDB son distintas de los números de control y contraseñas con los que cada persona inicia sesión en el portal. Los usuarios de prueba del script sólo existen después de ejecutar `CreacionBD.sql` en la base a la que apunta `DB_URL`. Si el esquema ya fue creado por el equipo, no vuelvas a ejecutar el script sin confirmar con quien administra la base; solicita que verifique que se insertaron las cuentas y que la IP de tu equipo tiene permitido el acceso remoto. No guardes credenciales en el repositorio ni las compartas en commits.

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

El comando anterior presupone que ya configuraste `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en la terminal, como se indica arriba. Si sólo quieres ejecutar las pruebas, no necesitas una base MySQL: usan H2 en memoria con `.\mvnw.cmd test`.

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
