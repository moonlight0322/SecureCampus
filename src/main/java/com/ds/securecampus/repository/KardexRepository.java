package com.ds.securecampus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Kardex;
import com.ds.securecampus.model.MateriaKardex;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {
    @Query("""
            SELECT new com.ds.securecampus.model.MateriaKardex(
                m.nombre, m.clave, pe.nombre, k.calificacionFinal, k.estatus, k.tipoAcreditacion)
            FROM Kardex k
            JOIN k.materia m
            JOIN k.periodo pe
            WHERE k.estudiante.numeroControl = :numeroControl
            ORDER BY pe.fechaInicio DESC, m.nombre
            """)
    List<MateriaKardex> buscarKardex(@Param("numeroControl") String numeroControl);
}
