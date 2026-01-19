package com.gestionmedica.gestionmedica.validadores;

import java.time.LocalDate;

/**
 * Validador para fechas de nacimiento.
 * Verifica que no sea una fecha futura.
 */
public class ValidadorFechaNacimiento {
    
    public static boolean esValida(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }
        return !fechaNacimiento.isAfter(LocalDate.now());
    }
}
