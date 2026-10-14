package com.ds.securecampus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByProfesorNumeroControl(String profesorNumeroControl);
    List<Grupo> findByPeriodoClavePeriodo(String clavePeriodo);
    Optional<Grupo> findByGrupoIdAndProfesorNumeroControl(Long grupoId, String profesorNumeroControl);
    boolean existsByCodigoGrupoAndMateriaMateriaIdAndPeriodoPeriodoId(
            String codigoGrupo, Long materiaId, Long periodoId);
}
