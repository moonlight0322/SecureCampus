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
                CONCAT(CONCAT(CONCAT(e.usuario.nombre, ' '), e.usuario.apellidoPaterno),
                    CASE WHEN e.usuario.apellidoMaterno IS NULL THEN '' ELSE CONCAT(' ', e.usuario.apellidoMaterno) END),
                e.usuario.correoInstitucional,
                c.nombre,
                e.promedio)
            FROM Estudiante e
            JOIN e.carrera c
            JOIN e.usuario u
            WHERE e.numeroControl = :numeroControl
            """)
    Optional<PerfilEstudiante> buscarPerfil(@Param("numeroControl") String numeroControl);
}
