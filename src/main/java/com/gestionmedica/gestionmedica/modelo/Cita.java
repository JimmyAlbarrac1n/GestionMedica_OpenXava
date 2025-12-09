package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import com.gestionmedica.gestionmedica.modelo.enums.*;

@Entity
@Getter @Setter
@View(members=
    "paciente;" +
    "medico;" +
    "horario;" +
    "agendamiento[" +
        "fechaCita; horaInicio; horaFin" +
    "];" +
    "detalles[" +
        "motivoConsulta; estado" +
    "];" +
    "auditoria[" +
        "fechaRegistro; fechaCancelacion" +
    "]"
)
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idCita;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties="cedula, nombre, apellido")
    @Required
    private Paciente paciente;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(
        descriptionProperties="nombre, apellido, especialidad.nombre",
        condition="${estado} = 'ACTIVO'"
    )
    @Required
    private Medico medico;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(
        descriptionProperties="medico.nombre, diaSemana, horaInicio, horaFin",
        condition="${estado} = 'ACTIVO'"
    )
    @Required
    private HorarioDisponible horario;
    
    @Column
    @Required
    private LocalDate fechaCita;
    
    @Column
    @ReadOnly // Se calcula automáticamente del horario
    @Stereotype("TIME")
    private LocalTime horaInicio;
    
    @Column
    @ReadOnly
    @Stereotype("TIME")
    private LocalTime horaFin;
    
    @Enumerated(EnumType.STRING)
    @Required
    private EstadoCita estado;
    
    @Column(length=500)
    @Stereotype("MEMO")
    private String motivoConsulta;
    
    @Column
    @ReadOnly
    private LocalDate fechaRegistro;
    
    @Column
    @ReadOnly
    private LocalDate fechaCancelacion;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDate.now();
        if (estado == null) {
            estado = EstadoCita.REGISTRADA;
        }
        if (horario != null) {
            horaInicio = horario.getHoraInicio();
            horaFin = horario.getHoraFin();
        }
        validarDisponibilidad();
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (estado == EstadoCita.CANCELADA && fechaCancelacion == null) {
            validarAnticipacion24Horas();
            fechaCancelacion = LocalDate.now();
        }
    }
    
    // VALIDACIÓN CRÍTICA: Evita doble reserva
    private void validarDisponibilidad() {
        // Busca si ya existe otra cita en el mismo horario y fecha
        Long count = (Long) XPersistence.getManager()
            .createQuery("SELECT COUNT(c) FROM Cita c WHERE " +
                        "c.horario.id = :horarioId AND " +
                        "c.fechaCita = :fecha AND " +
                        "c.estado != 'CANCELADA' AND " +
                        "c.id != :citaId")
            .setParameter("horarioId", horario.getIdHorario())
            .setParameter("fecha", fechaCita)
            .setParameter("citaId", idCita == null ? 0 : idCita)
            .getSingleResult();
            
        if (count > 0) {
            throw new ValidationException(
                "Este horario ya está ocupado para la fecha seleccionada"
            );
        }
    }
    
    // VALIDACIÓN: 24 horas de anticipación
    private void validarAnticipacion24Horas() {
        LocalDate ahora = LocalDate.now();
        long diasRestantes = ChronoUnit.DAYS.between(ahora, fechaCita);
        
        if (diasRestantes < 1) {
            throw new ValidationException(
                "Debe cancelar con al menos 24 horas de anticipación"
            );
        }
    }
}