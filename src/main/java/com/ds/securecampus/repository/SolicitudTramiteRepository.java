package com.ds.securecampus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.SolicitudTramite;

@Repository
public interface SolicitudTramiteRepository extends JpaRepository<SolicitudTramite, Long> {
    List<SolicitudTramite> findByNumeroControlOrderByFechaSolicitudDesc(String numeroControl);
    List<SolicitudTramite> findAllByOrderByFechaSolicitudDesc();
    Optional<SolicitudTramite> findByIdSolicitudAndNumeroControlAndEstatusIgnoreCase(
            Long idSolicitud, String numeroControl, String estatus);
    boolean existsByGrupoIdAndTipoTramiteIgnoreCaseAndEstatusIgnoreCase(
            Long grupoId, String tipoTramite, String estatus);
}
