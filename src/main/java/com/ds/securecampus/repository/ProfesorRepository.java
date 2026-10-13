package com.ds.securecampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Profesor;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, String> {
}
