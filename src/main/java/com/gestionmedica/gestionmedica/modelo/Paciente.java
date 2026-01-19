package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import lombok.*;
import java.time.LocalDate;
import com.gestionmedica.gestionmedica.validadores.*;
import org.openxava.validators.ValidationException;

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
    
    @Column(length=50)
    @Required
    private String nombre;
    
    @Column(length=50)
    @Required
    private String apellido;
    
    @Required
    private LocalDate fechaNacimiento;
    
    @Column(length=10)
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
        validarDatos();
        validarCedulaNoExista();
        fechaRegistro = LocalDate.now();
    }
    
   
    
    private void validarCedulaNoExista() {
        Long count = XPersistence.getManager()
            .createQuery("SELECT COUNT(p) FROM Paciente p WHERE p.cedula = :cedula", Long.class)
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
        if (!ValidadorFechaNacimiento.esValida(fechaNacimiento)) {
            throw new ValidationException("La fecha de nacimiento no puede ser una fecha futura");
        }
        if (!ValidadorTelefono.esValido(telefono)) {
            throw new ValidationException("El teléfono debe tener exactamente 10 dígitos");
        }
    }
    
    // Método para mostrar en selectores
    public String toString() {
        return nombre + " " + apellido + " - " + cedula;
    }
}