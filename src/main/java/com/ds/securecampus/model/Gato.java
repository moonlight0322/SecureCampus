package com.ds.securecampus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 

@Entity
@Table(name = "GATO")
public class Gato {

    public Gato (String nombre){
        this.nombre = nombre;
    }

    @Id
    @Column(name = "ID_GATO")
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long idGato;

    @Column(name = "NOMBRE", length = 255)
    private String nombre;
}
