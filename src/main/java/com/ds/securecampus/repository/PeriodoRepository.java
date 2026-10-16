package com.ds.securecampus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Periodo;

@Repository
public interface PeriodoRepository extends JpaRepository<Periodo, Long> {
    Periodo findByCapturaActiva(String capturaActiva);
    Optional<Periodo> findByClavePeriodo(String clavePeriodo);

    @Query(value = "select * from periodos where periodo_id = :periodoId for update", nativeQuery = true)
    Optional<Periodo> findByIdForUpdate(@Param("periodoId") Long periodoId);

    @Query(value = "select * from periodos where clave_periodo = :clavePeriodo for update", nativeQuery = true)
    Optional<Periodo> findByClavePeriodoForUpdate(@Param("clavePeriodo") String clavePeriodo);
}
