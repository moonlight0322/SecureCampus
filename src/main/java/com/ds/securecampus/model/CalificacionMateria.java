package com.ds.securecampus.model;

import java.math.BigDecimal;

public record CalificacionMateria(
        String materia,
        String clave,
        String grupo,
        String periodo,
        BigDecimal unidad1,
        BigDecimal unidad2,
        BigDecimal finalCurso) {
}
