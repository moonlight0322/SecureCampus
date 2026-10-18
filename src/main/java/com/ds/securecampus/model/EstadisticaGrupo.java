package com.ds.securecampus.model;

public record EstadisticaGrupo(
    Long grupoId,
    String codigoGrupo,
    String materiaNombre,
    String profesorNombre,
    int totalAlumnos,
    int cupoMaximo,
    int aprobados,
    int reprobados,
    int cursando,
    double promedioGeneral
) {}
