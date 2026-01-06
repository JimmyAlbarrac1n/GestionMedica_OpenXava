package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.openxava.annotations.ReadOnly;
import javax.persistence.Transient;
import com.gestionmedica.gestionmedica.modelo.enums.*;

@Entity
@Getter @Setter
@View(members=
    "medico;" +
    "configuracion[" +
        "diaSemana; turno; rangoHorario" +
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
    
    @Enumerated(EnumType.STRING)
    @Required
    private TurnoTrabajo turno;
    
    @Enumerated(EnumType.STRING)
    @Required
    private EstadoHorario estado;
    
    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = EstadoHorario.ACTIVO;
        }
    }
    
    /**
     * Retorna la hora de inicio del turno asociado
     */
    public LocalTime getHoraInicioTurno() {
        switch(turno) {
            case MATUTINO:
                return LocalTime.of(8, 0);
            case VESPERTINO:
                return LocalTime.of(14, 0);
            case NOCTURNO:
                return LocalTime.of(20, 0);
            case COMPLETO:
                return LocalTime.of(8, 0);
            default:
                return LocalTime.of(8, 0);
        }
    }
    
    /**
     * Retorna la hora de fin del turno asociado
     */
    public LocalTime getHoraFinTurno() {
        switch(turno) {
            case MATUTINO:
                return LocalTime.of(14, 0);
            case VESPERTINO:
                return LocalTime.of(20, 0);
            case NOCTURNO:
                return LocalTime.of(2, 0);
            case COMPLETO:
                return LocalTime.of(20, 0);
            default:
                return LocalTime.of(20, 0);
        }
    }
    
    /**
     * Retorna el rango horario como texto (para mostrar en la vista)
     */
    @Transient
    @ReadOnly
    public String getRangoHorario() {
        if (turno == null) return "";
        LocalTime inicio = null;
        LocalTime fin = null;
        try {
            inicio = getHoraInicioTurno();
            fin = getHoraFinTurno();
        } catch (Exception e) {
            return "";
        }
        if (inicio == null || fin == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return inicio.format(fmt) + " - " + fin.format(fmt);
    }
    
    public String toString() {
        return medico.getNombre() + " - " + diaSemana + " " + turno + " (" + getRangoHorario() + ")";
    }
}