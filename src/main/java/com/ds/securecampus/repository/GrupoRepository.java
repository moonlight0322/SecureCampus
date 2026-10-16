package com.ds.securecampus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    @EntityGraph(attributePaths = {"materia", "periodo"})
    List<Grupo> findByProfesorNumeroControl(String profesorNumeroControl);
    @EntityGraph(attributePaths = {"materia", "periodo", "horarios"})
    List<Grupo> findByPeriodoClavePeriodo(String clavePeriodo);
    @EntityGraph(attributePaths = {"materia", "periodo", "horarios"})
    Optional<Grupo> findByCodigoGrupoAndMateriaMateriaIdAndPeriodoPeriodoId(
            String codigoGrupo, Long materiaId, Long periodoId);

    @Query(value = "select periodo_id from grupos where grupo_id = :grupoId for update", nativeQuery = true)
    Optional<Long> findPeriodoIdByGrupoIdForUpdate(@Param("grupoId") Long grupoId);
}
