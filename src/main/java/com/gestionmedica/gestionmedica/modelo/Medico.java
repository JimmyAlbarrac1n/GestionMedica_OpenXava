package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import com.gestionmedica.gestionmedica.modelo.enums.*;

@Entity
@Getter @Setter
@View(members=
    "datosPersonales[" +
        "cedula; nombre; apellido;" +
    "];" +
    "datosContacto[" +
        "telefono; correo;" +
    "];" +
    "datosProfesionales[" +
        "especialidad; estado" +
    "]"
)
public class Medico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idMedico;
    
    @Column(length=10, unique=true)
    @Required
    private String cedula;
    
    @Column(length=100)
    @Required
    private String nombre;
    
    @Column(length=100)
    @Required
    private String apellido;
    
    @Column(length=15)
    @Required
    private String telefono;
    
    @Column(length=100)
    @Required
    @Stereotype("EMAIL")
    private String correo;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties="nombre")
    @Required
    private Especialidad especialidad;
    
    @Enumerated(EnumType.STRING)
    @Required
    private EstadoMedico estado;
    
    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = EstadoMedico.ACTIVO;
        }
    }
    
    public String toString() {
        return "Dr. " + nombre + " " + apellido + " (" + especialidad.getNombre() + ")";
    }
}