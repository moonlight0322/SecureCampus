package com.ds.securecampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrativos")
public class Administrativo {

    @Id
    @Column(name = "numero_control", length = 20)
    private String numeroControl;

    @Column(name = "area_departamento", nullable = false, length = 80)
    private String areaDepartamento;

    @Column(name = "cargo_puesto", length = 80)
    private String cargoPuesto;

    // Relación OneToOne para acceder a los datos de perfiles_base (Usuario)
    @OneToOne
    @JoinColumn(name = "numero_control", referencedColumnName = "numero_control", insertable = false, updatable = false)
    private Usuario usuario;

    // Getters y Setters
    public String getNumeroControl() { return numeroControl; }
    public void setNumeroControl(String numeroControl) { this.numeroControl = numeroControl; }

    public String getAreaDepartamento() { return areaDepartamento; }
    public void setAreaDepartamento(String areaDepartamento) { this.areaDepartamento = areaDepartamento; }

    public String getCargoPuesto() { return cargoPuesto; }
    public void setCargoPuesto(String cargoPuesto) { this.cargoPuesto = cargoPuesto; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}