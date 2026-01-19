package com.gestionmedica.gestionmedica.validadores;

import com.gestionmedica.gestionmedica.modelo.enums.EstadoCita;
import java.time.LocalDate;

/**
 * Validador para transiciones de estado en Cita.
 * Controla que los cambios de estado sean válidos y no permita cambios múltiples.
 */
public class ValidadorEstadoCita {
    
    /**
     * Verifica si la transición de estado es válida.
     * Estados permitidos: REGISTRADA -> ATENDIDA o CANCELADA (una sola vez)
     */
    public static boolean esTransicionValida(EstadoCita estadoAnterior, EstadoCita estadoNuevo) {
        if (estadoAnterior == null) {
            return estadoNuevo == EstadoCita.REGISTRADA;
        }
        
        if (estadoAnterior == EstadoCita.REGISTRADA) {
            return estadoNuevo == EstadoCita.ATENDIDA || estadoNuevo == EstadoCita.CANCELADA;
        }
        
        // No se permite cambiar de ATENDIDA o CANCELADA a otro estado
        return false;
    }
    
    /**
     * Verifica si se puede editar el agendamiento (máximo 24 horas antes).
     */
    public static boolean puedeeditarAgendamiento(LocalDate fechaCita) {
        if (fechaCita == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !fechaCita.isBefore(hoy.plusDays(1));
    }
}
