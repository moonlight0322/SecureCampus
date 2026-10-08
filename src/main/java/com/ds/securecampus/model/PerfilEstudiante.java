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
public class PerfilEstudiante {
    private String numeroControl;
    private String nombreCompleto;
    private String correo;
    private String carrera;
    private BigDecimal promedio;
}
