package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import lombok.*;
import com.gestionmedica.gestionmedica.modelo.enums.*;
import com.gestionmedica.gestionmedica.validadores.*;
import org.openxava.validators.ValidationException;

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
        validarDatos();
        validarCedulaNoExista();
        if (estado == null) {
            estado = EstadoMedico.ACTIVO;
        }
    }
    
   
    
    private void validarCedulaNoExista() {
        Long count = XPersistence.getManager()
            .createQuery("SELECT COUNT(m) FROM Medico m WHERE m.cedula = :cedula", Long.class)
            .setParameter("cedula", cedula)
            .getSingleResult();
        
        if (count > 0) {
            throw new ValidationException("Cédula ya existente en la base de datos");
        }
    }
    
    private void validarDatos() {
        if (!ValidadorCedula.esValida(cedula)) {
            throw new ValidationException("La cédula debe tener exactamente 10 dígitos numéricos");
        }
        if (!ValidadorNombre.esValido(nombre)) {
            throw new ValidationException("El nombre solo puede contener letras");
        }
        if (!ValidadorNombre.esValido(apellido)) {
            throw new ValidationException("El apellido solo puede contener letras");
        }
        if (!ValidadorTelefono.esValido(telefono)) {
            throw new ValidationException("El teléfono debe tener exactamente 10 dígitos");
        }
    }
    
    public String toString() {
        return "Dr. " + nombre + " " + apellido + " (" + especialidad.getNombre() + ")";
    }
}