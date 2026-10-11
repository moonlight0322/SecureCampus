package com.ds.securecampus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "aulas")
public class Aula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private Long aulaId;

    @Column(name = "id_edificio", nullable = false)
    private Long edificioId;

    @Column(name = "nombre_aula", nullable = false, length = 30)
    private String nombreAula;

    @Column(nullable = false)
    private Integer capacidad = 30;

    @Column(name = "tipo_aula", nullable = false, length = 30)
    private String tipoAula = "TEORICA";
}
