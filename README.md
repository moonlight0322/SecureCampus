# SecureCampus

Portal académico del Instituto Tecnológico de Toluca con acceso institucional y vistas para estudiantes.

## Requisitos

- Java 21
- Oracle Database con el esquema de `CreacionBD.sql`
- Maven (o el wrapper `./mvnw`)
- Conexión a Oracle para iniciar sesión y consultar datos académicos

## Preparar Oracle

Ejecuta `CreacionBD.sql` conectado con el usuario propietario de la base de datos. Configura la conexión sin guardar contraseñas en el repositorio:

```bash
export DB_URL='jdbc:oracle:thin:@localhost:1521/freepdb1'
export DB_USERNAME='PRUEBA'
export DB_PASSWORD='contraseña-de-la-base'
```

Las cuentas se validan contra `perfiles_base.numero_control`, `perfiles_base.password_hash` y el rol asociado en `roles`. El script incluye estas cuentas de prueba, con contraseñas almacenadas como hashes BCrypt y habilitadas para cambiarla (`debe_cambiar_pass = 1`):

| Tipo | Número de control | Contraseña inicial |
|---|---|---|
| Estudiante | `22280120` | `PW_22280120` |
| Administrativo | `10280070` | `PW_10280070` |
| Profesor | `19280703` | `PW_19280703` |

Úsalas solo en entornos de prueba. La migración incluida añade `estudiantes.fecha_ingreso` si falta; en una base ya existente, ejecuta ese bloque de migración por separado antes de insertar el estudiante (no vuelvas a ejecutar todos los `CREATE TABLE`).

El formulario envía `POST /login`; Spring Security procesa la autenticación, valida BCrypt, carga los roles y rechaza las cuentas inactivas. No se requiere un `@PostMapping` adicional: el filtro de formulario de Spring Security es el punto de entrada del login. Las tablas específicas (`estudiantes`, `profesores` y `administrativos`) se relacionan con `perfiles_base` por número de control.

## Ejecutar

```bash
./mvnw spring-boot:run
```

Abre <http://localhost:8080>. El portal implementa las páginas del estudiante: resumen académico, horario del periodo vigente y kárdex. Las cuentas administrativas y de profesor pueden autenticarse, pero muestran una pantalla informativa porque esos módulos no están dentro del alcance actual.
