package com.ds.securecampus.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;

@Repository
public interface CargaAlumnoRepository extends JpaRepository<CargaAlumno, Long> {
    @Query("""
            SELECT new com.ds.securecampus.model.MateriaHorario(
                m.nombre, m.clave, g.nombreGrupo, g.salon, g.horario,
                CASE WHEN p IS NULL THEN 'Por asignar'
                     ELSE CONCAT(CONCAT(p.nombre, ' '), p.apellidoPaterno) END,
                pe.nombre)
            FROM CargaAlumno ca
            JOIN ca.grupo g
            JOIN g.materia m
            JOIN g.periodo pe
            LEFT JOIN g.profesor p
            WHERE ca.estudiante.numeroControl = :numeroControl
              AND :fechaActual BETWEEN pe.fechaInicio AND pe.fechaFin
            ORDER BY m.nombre
            """)
    List<MateriaHorario> buscarHorarioActual(
            @Param("numeroControl") String numeroControl,
            @Param("fechaActual") LocalDate fechaActual);

    @Query("""
            SELECT new com.ds.securecampus.model.MateriaKardex(
                m.nombre, m.clave, pe.nombre, ca.calificacion, ca.estatus, ca.oportunidad)
            FROM CargaAlumno ca
            JOIN ca.grupo g
            JOIN g.materia m
            JOIN g.periodo pe
            WHERE ca.estudiante.numeroControl = :numeroControl
            ORDER BY pe.fechaInicio DESC, m.nombre
            """)
    List<MateriaKardex> buscarKardex(@Param("numeroControl") String numeroControl);
}
