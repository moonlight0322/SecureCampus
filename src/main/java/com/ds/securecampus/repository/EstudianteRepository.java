package com.ds.securecampus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.PerfilEstudiante;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
    @Query("""
            SELECT new com.ds.securecampus.model.PerfilEstudiante(
                e.numeroControl,
                CONCAT(CONCAT(CONCAT(e.nombre, ' '), e.apellidoPaterno),
                    CASE WHEN e.apellidoMaterno IS NULL THEN '' ELSE CONCAT(' ', e.apellidoMaterno) END),
                e.correo,
                c.nombre,
                e.promedio)
            FROM Estudiante e
            JOIN e.carrera c
            WHERE e.numeroControl = :numeroControl
            """)
    Optional<PerfilEstudiante> buscarPerfil(@Param("numeroControl") String numeroControl);
}
