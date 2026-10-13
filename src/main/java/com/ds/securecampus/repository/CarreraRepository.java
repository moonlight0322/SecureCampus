package com.ds.securecampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Carrera;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Long> {
}
