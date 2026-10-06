package com.ds.securecampus.service;

import java.util.List;

import com.ds.securecampus.model.Gato;

public interface GatoService {
    public List<Gato> obtenerGatos();
    
    public void guardar(Gato gato);
}
