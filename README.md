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

Si ya existe el esquema de las tablas proporcionadas, ejecuta también los dos `ALTER TABLE` comentados al final del script para añadir `cargas_alumnos.oportunidad`, requerida para conservar la diferencia entre segunda oportunidad, repetición y especial.

Las cuentas se validan contra `usuarios.username`, `usuarios.password_hash` y su rol asociado. `password_hash` debe contener un hash BCrypt válido de la contraseña institucional; los hashes de ejemplo de la solicitud son marcadores y no son credenciales utilizables. Asocia el número de control del usuario con su fila correspondiente en `estudiantes`, `profesores` o `administrativos`. Las cuentas inactivas no pueden iniciar sesión.

Los modelos persistentes usan entidades JPA con Lombok; los repositorios extienden `JpaRepository` y las clases `MateriaHorario`, `MateriaKardex` y `PerfilEstudiante` son proyecciones para las vistas. El formulario y los recursos visuales pueden cargarse aunque Oracle esté apagado, pero la autenticación y las consultas requieren que la base esté disponible.

## Ejecutar

```bash
./mvnw spring-boot:run
```

Abre <http://localhost:8080>. El portal implementa las páginas del estudiante: resumen académico, horario del periodo vigente y kárdex. Las cuentas administrativas y de maestro se autentican según su rol, pero muestran una pantalla informativa porque esos módulos no están dentro del alcance actual.
