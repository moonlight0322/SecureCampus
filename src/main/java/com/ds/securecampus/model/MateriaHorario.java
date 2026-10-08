package com.ds.securecampus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MateriaHorario {
    private String materia;
    private String clave;
    private String grupo;
    private String salon;
    private String horario;
    private String profesor;
    private String periodo;
}
