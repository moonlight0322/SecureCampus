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

Las cuentas se validan contra `perfiles_base.numero_control`, `perfiles_base.password_hash` y el rol asociado en `roles`. El script incluye estas cuentas de prueba, con contraseñas almacenadas como hashes BCrypt y habilitadas para cambiarla (`debe_cambiar_pass = 1`):

| Tipo | Número de control | Contraseña inicial |
|---|---|---|
| Estudiante | `22280120` | `PW_22280120` |
| Administrativo | `10280070` | `PW_10280070` |
| Profesor | `19280703` | `PW_19280703` |

Úsalas solo en entornos de prueba. Si la base TiDB/MySQL ya existe y no tiene `estudiantes.fecha_ingreso`, agrega la columna antes de insertar el estudiante:

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

Abre <http://localhost:8080>. El portal implementa las páginas del estudiante: resumen académico, horario del periodo vigente y kárdex. Las cuentas administrativas y de profesor pueden autenticarse, pero muestran una pantalla informativa porque esos módulos no están dentro del alcance actual.
