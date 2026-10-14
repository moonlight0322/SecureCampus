package com.ds.securecampus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class Mockups {

    @GetMapping({"/", "/login"})
    public String login(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? "login"
                : "redirect:/acceso";
    }

    @GetMapping("/acceso")
    public String acceso(Authentication authentication) {
        // Redirige al inicio del estudiante de forma limpia
        return "redirect:/estudiante/inicio";
    }

    @GetMapping("/estudiante/inicio")
    public String inicioEstudiante(Model model) {
        // Datos simulados (Mock) para la credencial del alumno
        model.addAttribute("estudiante", Map.of(
            "nombre", "Luis Antonio Rivera",
            "numeroControl", "21211045",
            "carrera", "Ingeniería en Sistemas Computacionales"
        ));

        // Clases simuladas para hoy
        model.addAttribute("clasesHoy", List.of(
            Map.of("hora", "07:00 - 09:00", "materia", "Sistemas Operativos", "aula", "Lab 3"),
            Map.of("hora", "09:00 - 11:00", "materia", "Base de Datos Avanzadas", "aula", "Lab 1"),
            Map.of("hora", "11:00 - 13:00", "materia", "Redes de Computadoras", "aula", "Lab Redes")
        ));

        return "estudiante/inicio";
    }

    @GetMapping("/estudiante/horario")
    public String horario(Model model) {
        model.addAttribute("estudiante", Map.of(
            "nombre", "Luis Antonio Rivera",
            "numeroControl", "21211045",
            "carrera", "Ingeniería en Sistemas Computacionales"
        ));

        // Datos simulados (Mock). Las claves coinciden con lo que lee el fragmento
        // fragments/tables :: tablaHorario (materia, clave, grupo, horario, salon,
        // profesor, periodo). Si el fragmento cambia, este Map debe cambiar con él.
        model.addAttribute("materias", List.of(
            Map.of(
                "clave", "SCC-1015",
                "materia", "Sistemas Operativos",
                "grupo", "5SC",
                "horario", "Lun - Vie 07:00 - 09:00",
                "salon", "Lab 3",
                "profesor", "Dr. Juan Pérez",
                "periodo", "Agosto - Diciembre 2026"
            )
        ));

        return "estudiante/horario";
    }

    @GetMapping("/estudiante/cardex")
    public String kardex(Model model) {
        model.addAttribute("estudiante", Map.of(
            "nombre", "Luis Antonio Rivera",
            "numeroControl", "21211045",
            "carrera", "Ingeniería en Sistemas Computacionales"
        ));

        // Datos simulados (Mock). Las claves coinciden con lo que lee el fragmento
        // fragments/tables :: tablaKardex (materia, clave, periodo, calificacion,
        // estatus, oportunidad). 'oportunidad' acepta PRIMERA, SEGUNDA, REPETICION
        // o ESPECIAL (ver fragmento opportunityBadge en fragments/components).
        model.addAttribute("materias", List.of(
            Map.of(
                "clave", "ACF-0901",
                "materia", "Cálculo Diferencial",
                "periodo", "Agosto - Diciembre 2023",
                "calificacion", 95,
                "estatus", "Acreditada",
                "oportunidad", "PRIMERA"
            ),
            Map.of(
                "clave", "ACF-0902",
                "materia", "Cálculo Integral",
                "periodo", "Enero - Junio 2024",
                "calificacion", 88,
                "estatus", "Acreditada",
                "oportunidad", "PRIMERA"
            )
        ));

        return "estudiante/cardex";
    }

    @GetMapping("/administrativo/inicio")
        public String inicioAdministrativo(Model model) {
            // Datos simulados para el perfil del administrativo
            model.addAttribute("administrativo", Map.of(
                "nombre", "Lic. María Elena Gómez",
                "empleadoId", "EMP-9082",
                "departamento", "Dirección de Servicios Escolares"
            ));
    
            // Métrica simulada para el dashboard ejecutivo
            model.addAttribute("totalUsuarios", 1420);
    
            return "administrativo/inicio";
        }

        @GetMapping("/inicio")
            public String inicio(Model model) {
                model.addAttribute("profesor", Map.of(
                    "nombre", "Dr. Juan Pérez",
                    "departamento", "Sistemas y Computación",
                    "numeroEmpleado", "EMP-2026" // <-- Asegúrate de agregar esta línea
                ));
                model.addAttribute("totalGrupos", 4);
                model.addAttribute("totalAlumnos", 128);
                
                model.addAttribute("clasesHoy", List.of(
                    Map.of("hora", "08:00 - 10:00", "materia", "Sistemas Operativos", "grupo", "5SC1", "aula", "Lab 3"),
                    Map.of("hora", "10:00 - 12:00", "materia", "Redes de Computadoras", "grupo", "5SC2", "aula", "Lab 4")
                ));
                
                return "profesor/inicio";
            }
    
        // 2. Simula el listado de grupos
        @GetMapping("/grupos")
            public String grupos(Model model) {
                model.addAttribute("profesor", Map.of(
                    "nombre", "Dr. Juan Pérez",
                    "departamento", "Sistemas y Computación",
                    "numeroEmpleado", "EMP-2026" // <-- Asegúrate de incluir esto aquí también
                ));
                
                model.addAttribute("listaGrupos", List.of(
                    Map.of("id", 1, "clave", "5SC1", "materia", "Sistemas Operativos", "aula", "Lab 3", "horario", "08:00 - 10:00")//,
                 //   Map.of("id", 2, "clave", "5SC2", "materia", "Redes de Computadoras", "aula", "Lab 4", "horario", "10:00 - 12:00"),
                 //   Map.of("id", 3, "clave", "6SC1", "materia", "Arquitectura de Computadoras", "aula", "Lab 2", "horario", "12:00 - 14:00")
                ));
                
                return "profesor/grupos";
            }
    
        // 3. Simula el detalle de un grupo específico
        @GetMapping("/grupo/{id}")
            public String grupoDetalle(@PathVariable("id") Long id, Model model) {
                model.addAttribute("profesor", Map.of(
                    "nombre", "Dr. Juan Pérez",
                    "departamento", "Sistemas y Computación",
                    "numeroEmpleado", "EMP-2026"
                ));
                
                model.addAttribute("grupo", Map.of(
                    "id", id,
                    "clave", "5SC1",
                    "materia", "Sistemas Operativos",
                    "horario", "08:00 - 10:00",
                    "aula", "Lab 3"
                ));
            
                // 5 unidades dinámicas
                List<String> evaluaciones = List.of("Unidad 1", "Unidad 2", "Unidad 3", "Unidad 4", "Unidad 5","Unidad 6","Unidad 7");
                model.addAttribute("listaEvaluaciones", evaluaciones);
            
                // 15 alumnos con sus 5 calificaciones correspondientes
                model.addAttribute("listaAlumnos", List.of(
                    Map.of("numeroControl", "21211001", "nombre", "María Fernanda Gómez", "calificaciones", List.of(90, 85, 92, 88, 95,0,0)),
                    Map.of("numeroControl", "21211002", "nombre", "Carlos Alberto Ruiz", "calificaciones", List.of(80, 88, 85, 90, 82,0,0)),
                    Map.of("numeroControl", "21211003", "nombre", "Ana Sofía Morales", "calificaciones", List.of(95, 92, 98, 96, 94,0,0)),
                    Map.of("numeroControl", "21211004", "nombre", "José Luis Hernández", "calificaciones", List.of(75, 78, 80, 85, 83,0,0)),
                    Map.of("numeroControl", "21211005", "nombre", "Daniela Torres Cruz", "calificaciones", List.of(88, 90, 85, 92, 89,0,0)),
                    Map.of("numeroControl", "21211006", "nombre", "Alejandro Ramírez Soto", "calificaciones", List.of(92, 95, 90, 88, 91,0,0)),
                    Map.of("numeroControl", "21211007", "nombre", "Valeria Mendoza Silva", "calificaciones", List.of(85, 82, 88, 84, 86,0,0)),
                    Map.of("numeroControl", "21211008", "nombre", "Jorge Antonio Castillo", "calificaciones", List.of(78, 80, 75, 82, 79,0,0)),
                    Map.of("numeroControl", "21211009", "nombre", "Brenda Lizbeth Flores", "calificaciones", List.of(96, 94, 95, 97, 98,0,0)),
                    Map.of("numeroControl", "21211010", "nombre", "Fernando Garza Medina", "calificaciones", List.of(83, 85, 87, 80, 84,0,0)),
                    Map.of("numeroControl", "21211011", "nombre", "Sofía Alejandra Vargas", "calificaciones", List.of(91, 89, 93, 90, 92,0,0)),
                    Map.of("numeroControl", "21211012", "nombre", "Ricardo Morales Luna", "calificaciones", List.of(76, 79, 81, 75, 78,0,0)),
                    Map.of("numeroControl", "21211013", "nombre", "Paulina Rangel Rojas", "calificaciones", List.of(89, 91, 88, 93, 90,0,0)),
                    Map.of("numeroControl", "21211014", "nombre", "Eduardo Jaramillo Pardo", "calificaciones", List.of(84, 82, 86, 85, 88,0,0)),
                    Map.of("numeroControl", "21211015", "nombre", "Monserrat Pineda Ortiz", "calificaciones", List.of(94, 96, 92, 95, 97,0,0))
                ));
                
                return "profesor/grupo-detalle";
            }
    }