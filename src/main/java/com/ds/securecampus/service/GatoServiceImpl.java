package com.ds.securecampus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.securecampus.model.Gato;
import com.ds.securecampus.repository.GatoRepository;

@Service
public class GatoServiceImpl implements GatoService{
    @Autowired 
    private GatoRepository repository;

    @Override
    public void guardar(Gato gato) {
        repository.save(gato);
    }

    @Override
    public List<Gato> obtenerGatos() {
        return repository.findAll();
    }
}
