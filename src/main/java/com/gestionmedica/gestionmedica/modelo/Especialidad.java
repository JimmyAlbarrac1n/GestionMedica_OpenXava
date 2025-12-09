package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@View(members="nombre; descripcion") // Layout vertical
public class Especialidad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idEspecialidad;
    
    @Column(length=100)
    @Required
    private String nombre;
    
    @Column(length=500)
    @Stereotype("MEMO")
    private String descripcion;
}