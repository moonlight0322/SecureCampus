package com.ds.securecampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ds.securecampus.model.Gato;

@Repository
public interface GatoRepository extends JpaRepository<Gato, Long>{
    
}
