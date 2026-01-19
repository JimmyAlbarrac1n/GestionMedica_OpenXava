package com.gestionmedica.gestionmedica.validadores;

/**
 * Validador para teléfono.
 * Verifica que contenga exactamente 10 dígitos numéricos.
 */
public class ValidadorTelefono {
    
    public static boolean esValido(String telefono) {
        if (telefono == null) {
            return false;
        }
        return telefono.matches("^\\d{10}$");
    }
}
