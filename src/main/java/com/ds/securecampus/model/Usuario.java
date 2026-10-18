package com.ds.securecampus.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_base")
public class Usuario {
    @Id
    @Column(name = "numero_control", length = 20)
    private String numeroControl;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Byte activo = 1;

    @Column(name = "debe_cambiar_pass", nullable = false)
    @ColumnDefault("1")
    private Byte debeCambiarPass = 1;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 200)
    private String domicilio;

    @Column(length = 15)
    private String telefono;

    @Column(name = "correo_institucional", nullable = false, unique = true, length = 100)
    private String correoInstitucional;

    @Column(name = "fecha_creacion")
    @ColumnDefault("(CURRENT_DATE)")
    private LocalDate fechaCreacion;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Estudiante estudiante;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Profesor profesor;
}
