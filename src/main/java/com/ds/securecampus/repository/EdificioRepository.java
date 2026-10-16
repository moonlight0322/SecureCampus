package com.ds.securecampus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ds.securecampus.model.Edificio;

public interface EdificioRepository extends JpaRepository<Edificio, Long> {
    Optional<Edificio> findByClave(String clave);
}
