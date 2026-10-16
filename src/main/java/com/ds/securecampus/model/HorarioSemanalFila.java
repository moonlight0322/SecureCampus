package com.ds.securecampus.model;

public class HorarioSemanalFila {
    private final String horario;
    private String lunes = "";
    private String martes = "";
    private String miercoles = "";
    private String jueves = "";
    private String viernes = "";

    public HorarioSemanalFila(String horario) {
        this.horario = horario;
    }

    public String getHorario() { return horario; }
    public String getLunes() { return lunes; }
    public String getMartes() { return martes; }
    public String getMiercoles() { return miercoles; }
    public String getJueves() { return jueves; }
    public String getViernes() { return viernes; }

    public void agregarClase(Byte dia, String detalle) {
        switch (dia) {
            case 1 -> lunes = combinar(lunes, detalle);
            case 2 -> martes = combinar(martes, detalle);
            case 3 -> miercoles = combinar(miercoles, detalle);
            case 4 -> jueves = combinar(jueves, detalle);
            case 5 -> viernes = combinar(viernes, detalle);
            default -> { }
        }
    }

    private String combinar(String actual, String nuevo) {
        return actual.isEmpty() ? nuevo : actual + "\n" + nuevo;
    }
}
