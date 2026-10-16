package com.ds.securecampus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ds.securecampus.model.HorarioClase;

public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
            FROM HorarioClase h
            WHERE h.aula.edificio.clave = :edificio
              AND h.aula.nombreAula = :aula
              AND h.diaSemana = :dia
              AND h.grupo.periodo.periodoId = :periodoId
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
            """)
    boolean existeConflictoAula(@Param("edificio") String edificio,
                                @Param("aula") String aula,
                                @Param("dia") Byte dia,
                                @Param("periodoId") Long periodoId,
                                @Param("horaInicio") String horaInicio,
                                @Param("horaFin") String horaFin);

    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
            FROM HorarioClase h
            WHERE h.grupo.profesor.numeroControl = :profesor
              AND h.diaSemana = :dia
              AND h.grupo.periodo.periodoId = :periodoId
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
            """)
    boolean existeConflictoProfesor(@Param("profesor") String profesor,
                                    @Param("dia") Byte dia,
                                    @Param("periodoId") Long periodoId,
                                    @Param("horaInicio") String horaInicio,
                                    @Param("horaFin") String horaFin);

    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
            FROM CargaAlumno ca
            JOIN ca.grupo g
            JOIN g.horarios h
            WHERE ca.estudiante.numeroControl = :estudiante
              AND g.periodo.periodoId = :periodoId
              AND g.grupoId <> :grupoId
              AND h.diaSemana = :dia
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
            """)
    boolean existeConflictoEstudiante(@Param("estudiante") String estudiante,
                                      @Param("grupoId") Long grupoId,
                                      @Param("periodoId") Long periodoId,
                                      @Param("dia") Byte dia,
                                      @Param("horaInicio") String horaInicio,
                                      @Param("horaFin") String horaFin);

    List<HorarioClase> findByGrupoGrupoId(Long grupoId);
}
