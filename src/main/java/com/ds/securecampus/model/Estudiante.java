package com.ds.securecampus.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estudiantes")
public class Estudiante {
    @Id
    @Column(name = "numero_control", length = 20)
    private String numeroControl;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_control")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(precision = 5, scale = 2)
    private BigDecimal promedio;

    @Column(name = "pago_reinscripcion", nullable = false, length = 1)
    private String pagoReinscripcion = "N";

    @Column(name = "tiene_adeudos", nullable = false, length = 1)
    private String tieneAdeudos = "N";
}
