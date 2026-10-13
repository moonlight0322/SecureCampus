package com.ds.securecampus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Materia;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {
    List<Materia> findByCarreraCarreraId(Long carreraId);
    boolean existsByClave(String clave);
}
