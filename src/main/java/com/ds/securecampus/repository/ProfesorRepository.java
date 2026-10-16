package com.ds.securecampus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Profesor;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, String> {
    @Override
    @EntityGraph(attributePaths = {"usuario", "carreraAdscripcion"})
    java.util.List<Profesor> findAll();

    @EntityGraph(attributePaths = {"usuario", "carreraAdscripcion"})
    Optional<Profesor> findByNumeroControl(String numeroControl);
}
