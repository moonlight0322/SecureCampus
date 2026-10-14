# Análisis y decisiones de seguridad
## 1. Autenticación
- **Spring Security** con `BCryptPasswordEncoder`.  
- Contraseñas almacenadas con hash BCrypt (sal incluido).  
## 2. Bloqueo de cuenta
- `LoginAttemptService` guarda intentos en un `ConcurrentHashMap`.  
- Límite: **5** intentos fallidos → bloqueo de **15 min**.  
- `CustomAuthenticationFailureHandler` incrementa los contadores y redirige a `/login?blocked=true`.  
## 3. Auditoría
- Tabla `logs_auditoria` registra: usuario, rol, operación, IP, fecha/hora y detalles.  
- `AuditoriaService` usa **PROPAGATION.REQUIRES_NEW** para que fallos de auditoría **no** reviertan la transacción principal.  
- Campo `rol` truncado a 30 caracteres (columna ampliada a 100 caracteres).  
## 4. RBAC (Control de acceso basado en roles)
- Configurado en `SecurityConfig` mediante `hasRole(...)` o `hasAnyRole(...)`.  
- Rutas:
  - `/estudiante/**` → `ROLE_ESTUDIANTE`
  - `/profesor/**` → `ROLE_PROFESOR`
  - `/admin/**` → `ROLE_ADMINISTRADOR` o `ROLE_ADMINISTRATIVO`
  - `/jefe/**` → `ROLE_JEFE_DEPARTAMENTO`
- Todas las demás rutas requieren autenticación.
## 5. CSRF y cabeceras de seguridad
- CSRF habilitado por defecto; los formularios incluyen token `_csrf`.  
- Cabecera `X-Frame-Options: DENY` y `X-Content-Type-Options: nosniff`.  
## 6. Protección contra inyección
- Uso de **JPA** y **Spring Data**, evita concatenación manual de SQL.  
- Parámetros siempre enviados como **prepared statements**.  
## 7. Accesibilidad y UI
- CSS `portal.css` incluye modos **dark**, **high contrast**, y ajuste de **tamaño de fuente**.  
- Preferencias guardadas en `localStorage`.  
## 8. Pruebas SAST/DAST
- Ejecutar `mvn sonar:sonar` (SAST).  
- Ejecutar `OWASP ZAP` contra `http://localhost:8080` (DAST).  
## 9. Registro y monitoreo
- Logs de Spring (`INFO` en arranque, `ERROR` en fallos).  
- Audilog guardado en DB, revisable desde `/admin/auditoria`.  
---  
## 4. Pasos finales para que el frontend pueda enlazar  
1. **Ejecutar migraciones** (si usa Flyway): `./mvnw flyway:migrate`.  
2. **Arrancar el backend** (`./mvnw spring-boot:run`).  
3. **Comprobar los endpoints** con el script de pruebas (`python scratch/test_all_actors.py`).  
4. **Compartir al equipo frontend**:  
   - URL base: `http://localhost:8080` (o la URL del despliegue).  
   - Endpoints listados en la tabla de la sección *Endpoints principales*.  
   - Los PDFs se solicitan vía GET a `…/pdf` y devuelven `application/pdf`.  
---  


