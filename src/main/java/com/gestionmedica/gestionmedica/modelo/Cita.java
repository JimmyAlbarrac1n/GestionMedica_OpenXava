package com.gestionmedica.gestionmedica.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import com.gestionmedica.gestionmedica.modelo.enums.*;

@Entity
@Getter
@Setter
@View(members = "paciente;" +
        "agendamiento[" +
        "medico; fechaCita; numeroSlot; horaDelSlotTexto" +
        "];" +
        "detalles[" +
        "motivoConsulta; estado" +
        "];" +
        "auditoria[" +
        "fechaRegistro; fechaCancelacion" +
        "]")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Integer idCita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "cedula, nombre, apellido")
    @Required
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre, apellido, especialidad.nombre")
    @Required
    private Medico medico;

    @Column
    @Required
    private LocalDate fechaCita;

    @Column
    @Required
    private Integer numeroSlot;

    @Enumerated(EnumType.STRING)
    @Required
    private EstadoCita estado;

    @Column(length = 500)
    @Stereotype("MEMO")
    private String motivoConsulta;

    @Column
    @ReadOnly
    private LocalDate fechaRegistro;

    @Column
    @ReadOnly
    private LocalDate fechaCancelacion;

    // Duración fija de cada slot en minutos
    private static final int DURACION_MINUTOS = 30;
    // Máximo de slots por turno (8 horas = 480 min / 30 min = 16 slots)
    private static final int MAX_SLOTS = 16;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDate.now();
        if (estado == null) {
            estado = EstadoCita.REGISTRADA;
        }

        validarFechaNoPasada();
        validarSlotValido();
        Integer medicoId = medico.getIdMedico();
        validarHorarioMedico(medicoId);
        validarDisponibilidad(medicoId);
    }

    @PreUpdate
    protected void onUpdate() {
        // Solo validar si las entidades están cargadas (evita
        // LazyInitializationException)
        if (medico != null && fechaCita != null && numeroSlot != null) {
            validarFechaNoPasada();
            validarSlotValido();

            // Solo validar si podemos acceder al ID del médico
            try {
                Integer medicoId = medico.getIdMedico();
                if (medicoId != null) {
                    validarHorarioMedico(medicoId);
                    validarDisponibilidad(medicoId);
                }
            } catch (ValidationException e) {
                // Re-lanzar las excepciones de validación para que el usuario las vea
                throw e;
            } catch (Exception e) {
                // Solo capturar excepciones de lazy loading u otros errores técnicos
                // Las ValidationException se propagan arriba
            }
        }
    }

    // ===== VALIDACIONES =====

    private void validarFechaNoPasada() {
        if (fechaCita.isBefore(LocalDate.now())) {
            throw new ValidationException(
                    "No puede agendar citas en fechas pasadas");
        }
    }

    /**
     * Obtiene el día de la semana derivado de la fecha
     */
    private DiaSemana obtenerDiaDesdeFecha() {
        DayOfWeek diaSemanaFecha = fechaCita.getDayOfWeek();
        String diaSemanaStr = convertirDiaSemana(diaSemanaFecha);
        return DiaSemana.valueOf(diaSemanaStr);
    }

    /**
     * Valida que el slot esté en rango válido (1-16)
     */
    private void validarSlotValido() {
        if (numeroSlot == null || numeroSlot < 1 || numeroSlot > MAX_SLOTS) {
            throw new ValidationException(
                    "El slot debe estar entre 1 y " + MAX_SLOTS);
        }
    }

    /**
     * Valida que el médico tenga horario activo para ese día
     */
    private void validarHorarioMedico(Integer medicoId) {
        DiaSemana diaDerido = obtenerDiaDesdeFecha();

        if (medicoId == null) {
            throw new ValidationException("No se pudo obtener el ID del médico");
        }

        HorarioDisponible horarioMedico = XPersistence.getManager()
                .createQuery("SELECT h FROM HorarioDisponible h WHERE " +
                        "h.medico.id = :medicoId AND " +
                        "h.diaSemana = :diaSemana AND " +
                        "h.estado = 'ACTIVO'", HorarioDisponible.class)
                .setParameter("medicoId", medicoId)
                .setParameter("diaSemana", diaDerido)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        if (horarioMedico == null) {
            throw new ValidationException(
                    "El médico no tiene horario activo para ese día");
        }
    }

    /**
     * Valida que no exista otra cita en el mismo slot
     */
    private void validarDisponibilidad(Integer medicoId) {
        if (medicoId == null) {
            throw new ValidationException("No se pudo obtener el ID del médico");
        }

        Long count = XPersistence.getManager()
                .createQuery("SELECT COUNT(c) FROM Cita c WHERE " +
                        "c.medico.id = :medicoId AND " +
                        "c.fechaCita = :fecha AND " +
                        "c.numeroSlot = :slot AND " +
                        "c.estado != 'CANCELADA' AND " +
                        "c.idCita != :citaId", Long.class)
                .setParameter("medicoId", medicoId)
                .setParameter("fecha", fechaCita)
                .setParameter("slot", numeroSlot)
                .setParameter("citaId", idCita == null ? 0 : idCita)
                .getSingleResult();

        if (count > 0) {
            throw new ValidationException(
                    "El médico ya tiene una cita en ese slot");
        }
    }

    private String convertirDiaSemana(DayOfWeek dia) {
        switch (dia) {
            case MONDAY:
                return "LUNES";
            case TUESDAY:
                return "MARTES";
            case WEDNESDAY:
                return "MIERCOLES";
            case THURSDAY:
                return "JUEVES";
            case FRIDAY:
                return "VIERNES";
            case SATURDAY:
                return "SABADO";
            case SUNDAY:
                return "DOMINGO";
            default:
                return "";
        }
    }

    /**
     * Calcula la hora de inicio del slot
     */
    public LocalTime getHoraDelSlot() {
        // Si no hay médico o fecha aún, no intentar conversión
        if (medico == null || fechaCita == null || numeroSlot == null)
            return null;

        HorarioDisponible horario = null;
        try {
            horario = XPersistence.getManager()
                    .createQuery("SELECT h FROM HorarioDisponible h WHERE " +
                            "h.medico.id = :medicoId AND " +
                            "h.diaSemana = :diaSemana AND " +
                            "h.estado = 'ACTIVO'", HorarioDisponible.class)
                    .setParameter("medicoId", medico.getIdMedico())
                    .setParameter("diaSemana", obtenerDiaDesdeFecha())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }

        if (horario == null)
            return null;

        LocalTime inicio = horario.getHoraInicioTurno();
        if (inicio == null)
            return null;
        try {
            return inicio.plusMinutes((numeroSlot - 1) * DURACION_MINUTOS);
        } catch (Exception e) {
            return null;
        }
    }

    @Transient
    @ReadOnly
    @Hidden
    public String getHoraDelSlotTexto() {
        LocalTime hora = getHoraDelSlot();
        if (hora == null)
            return "";
        try {
            return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "";
        }
    }

    @Transient
    @ReadOnly
    public String getInformacionSlots() {
        HorarioDisponible horario = null;
        try {
            horario = (HorarioDisponible) XPersistence.getManager()
                    .createQuery("SELECT h FROM HorarioDisponible h WHERE " +
                            "h.medico.id = :medicoId AND " +
                            "h.diaSemana = :diaSemana AND " +
                            "h.estado = 'ACTIVO'")
                    .setParameter("medicoId", medico.getIdMedico())
                    .setParameter("diaSemana", obtenerDiaDesdeFecha())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return "Error al obtener horario del médico";
        }

        if (horario == null) {
            return "No hay horario activo para el médico en la fecha seleccionada.";
        }

        LocalTime inicio = horario.getHoraInicioTurno();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder sb = new StringBuilder();
        for (int s = 1; s <= MAX_SLOTS; s++) {
            LocalTime slotStart = inicio.plusMinutes((s - 1) * DURACION_MINUTOS);
            LocalTime slotEnd = slotStart.plusMinutes(DURACION_MINUTOS);
            sb.append("Slot ").append(s).append(": ")
                    .append(slotStart.format(fmt)).append(" - ")
                    .append(slotEnd.format(fmt));
            if (s < MAX_SLOTS)
                sb.append("\n");
        }
        return sb.toString();
    }

    public String toString() {
        String hora = getHoraDelSlotTexto();
        if (hora.isEmpty())
            hora = "SIN HORA";
        return fechaCita + " " + hora + " (Slot " + numeroSlot + ") - " +
                (medico != null ? medico.getNombre() : "-") + " - " +
                (paciente != null ? paciente.getNombre() : "-");
    }
}