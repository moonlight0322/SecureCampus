package com.ds.securecampus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ds.securecampus.model.Aula;

public interface AulaRepository extends JpaRepository<Aula, Long> {
    Optional<Aula> findByEdificioClaveAndNombreAula(String claveEdificio, String nombreAula);
}
