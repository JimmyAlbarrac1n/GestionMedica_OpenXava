package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter
@View(members=
    "datosPersonales[" +
        "cedula; nombre; apellido; fechaNacimiento;" +
    "];" +
    "datosContacto[" +
        "telefono; correo; direccion" +
    "]"
)
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idPaciente;
    
    @Column(length=10, unique=true)
    @Required
    private String cedula;
    
    @Column(length=100)
    @Required
    private String nombre;
    
    @Column(length=100)
    @Required
    private String apellido;
    
    @Required
    private LocalDate fechaNacimiento;
    
    @Column(length=15)
    @Required
    private String telefono;
    
    @Column(length=100)
    @Required
    @Stereotype("EMAIL")
    private String correo;
    
    @Column(length=200)
    @Stereotype("MEMO")
    private String direccion;
    
    @Column
    @Hidden
    private LocalDate fechaRegistro;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDate.now();
    }
    
    // Método para mostrar en selectores
    public String toString() {
        return cedula + " - " + nombre + " " + apellido;
    }
}