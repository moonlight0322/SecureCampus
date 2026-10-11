package com.ds.securecampus.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kardex_estudiante", uniqueConstraints = @UniqueConstraint(
        name = "uk_estudiante_mat_per",
        columnNames = {"numero_control_estudiante", "materia_id", "periodo_id"}))
public class Kardex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kardex_id")
    private Long kardexId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "numero_control_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private Periodo periodo;

    @Column(name = "calificacion_final", nullable = false, precision = 4, scale = 2)
    private BigDecimal calificacionFinal;

    @Column(name = "tipo_acreditacion", nullable = false, length = 20)
    private String tipoAcreditacion = "ORDINARIO";

    @Column(nullable = false, length = 15)
    private String estatus;

    @Column(name = "creditos_obtenidos", nullable = false, precision = 2)
    private Integer creditosObtenidos;
}
