package com.ds.securecampus.model;

public record HorarioClaseEstudiante(
        Byte diaSemana,
        String horaInicio,
        String horaFin,
        String materia,
        String salon) {
    public String getHora() {
        return horaInicio + " - " + horaFin;
    }

    public String getAula() {
        return salon;
    }
}
