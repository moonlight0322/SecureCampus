package com.ds.securecampus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.SolicitudTramite;

@Repository
public interface SolicitudTramiteRepository extends JpaRepository<SolicitudTramite, Long> {
    List<SolicitudTramite> findByNumeroControlOrderByFechaSolicitudDesc(String numeroControl);
    List<SolicitudTramite> findAllByOrderByFechaSolicitudDesc();
}
