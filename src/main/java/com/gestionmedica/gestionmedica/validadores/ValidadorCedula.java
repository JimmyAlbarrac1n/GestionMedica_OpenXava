package com.gestionmedica.gestionmedica.validadores;

/**
 * Validador para cédulas.
 * Verifica que tenga exactamente 10 caracteres numéricos.
 */
public class ValidadorCedula {
    
    public static boolean esValida(String cedula) {
        if (cedula == null) {
            return false;
        }
        return cedula.matches("^\\d{10}$");
    }
}
