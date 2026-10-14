package com.ds.securecampus.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.MateriaHorario;

@Repository
public interface CargaAlumnoRepository extends JpaRepository<CargaAlumno, Long> {
    @Query("""
            SELECT new com.ds.securecampus.model.MateriaHorario(
                m.nombre, m.clave, g.codigoGrupo,
                CASE WHEN a IS NULL THEN 'Salón por asignar' ELSE a.nombreAula END,
                CASE WHEN h IS NULL THEN 'Horario pendiente'
                     ELSE CONCAT(CONCAT(CASE h.diaSemana
                            WHEN 1 THEN 'Lunes'
                            WHEN 2 THEN 'Martes'
                            WHEN 3 THEN 'Miércoles'
                            WHEN 4 THEN 'Jueves'
                            WHEN 5 THEN 'Viernes'
                            WHEN 6 THEN 'Sábado'
                            ELSE 'Día no definido'
                        END, ' '), CONCAT(CONCAT(h.horaInicio, ' - '), h.horaFin)) END,
                CASE WHEN p IS NULL THEN 'Por asignar'
                     ELSE CONCAT(CONCAT(perfilProfesor.nombre, ' '), perfilProfesor.apellidoPaterno) END,
                pe.nombre)
            FROM CargaAlumno ca
            JOIN ca.grupo g
            JOIN g.materia m
            JOIN g.periodo pe
            LEFT JOIN g.profesor p
            LEFT JOIN p.usuario perfilProfesor
            LEFT JOIN g.horarios h
            LEFT JOIN h.aula a
            WHERE ca.estudiante.numeroControl = :numeroControl
              AND :fechaActual BETWEEN pe.fechaInicio AND pe.fechaFin
            ORDER BY m.nombre
            """)
    List<MateriaHorario> buscarHorarioActual(
            @Param("numeroControl") String numeroControl,
            @Param("fechaActual") LocalDate fechaActual);

    List<CargaAlumno> findByGrupoGrupoId(Long grupoId);
}
