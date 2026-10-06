package com.ds.securecampus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ds.securecampus.model.Gato;
import com.ds.securecampus.service.GatoServiceImpl;

@Controller 
public class IndexController {

    @Autowired 
    private GatoServiceImpl repoGato;

    @GetMapping ({"/", "/index"})
    public String index(Model model){
        Gato gato = new Gato("Flerki " + System.currentTimeMillis());
        repoGato.guardar(gato);
        List<Gato> gatos = repoGato.obtenerGatos();
        model.addAttribute("gatos", gatos);
        return "index"; //index.html
    }
}
