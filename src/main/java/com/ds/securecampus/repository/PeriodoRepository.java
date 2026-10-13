package com.ds.securecampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Periodo;

@Repository
public interface PeriodoRepository extends JpaRepository<Periodo, Long> {
    Periodo findByCapturaActiva(String capturaActiva);
}
