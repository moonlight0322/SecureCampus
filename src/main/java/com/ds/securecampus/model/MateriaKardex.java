package com.ds.securecampus.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MateriaKardex {
    private String materia;
    private String clave;
    private String periodo;
    private BigDecimal calificacion;
    private String estatus;
    private String oportunidad;
}
