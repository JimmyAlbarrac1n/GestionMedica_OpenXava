package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalTime;
import com.gestionmedica.gestionmedica.modelo.enums.*;
import com.gestionmedica.gestionmedica.calculadores.DuracionCitaCalculator;
@Entity
@Getter @Setter
@View(members=
    "medico;" +
    "horario[" +
        "diaSemana; horaInicio; horaFin; duracionCita" +
    "];" +
    "estado"
)
public class HorarioDisponible {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idHorario;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties="nombre, apellido, especialidad.nombre")
    @Required
    private Medico medico;
    
    @Enumerated(EnumType.STRING)
    @Required
    private DiaSemana diaSemana;
    
    @Column
    @Required
    @Stereotype("TIME")
    private LocalTime horaInicio;
    
    @Column
    @Required
    @Stereotype("TIME")
    private LocalTime horaFin;
    
    @Column
    @Required
    @DefaultValueCalculator(value=DuracionCitaCalculator.class)
    private Integer duracionCita; // Minutos
    
    @Enumerated(EnumType.STRING)
    @Required
    private EstadoHorario estado;
    
    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = EstadoHorario.ACTIVO;
        }
    }
    
    public String toString() {
        return medico.getNombre() + " - " + diaSemana + " " + 
               horaInicio + "-" + horaFin;
    }
}