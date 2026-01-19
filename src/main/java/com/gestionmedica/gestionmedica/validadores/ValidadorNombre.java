package com.gestionmedica.gestionmedica.validadores;

/**
 * Validador para nombres.
 * Verifica que contenga únicamente letras (incluyendo acentos y espacios).
 */
public class ValidadorNombre {
    
    public static boolean esValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return nombre.matches("^[a-záéíóúñA-ZÁÉÍÓÚÑ\\s]+$");
    }
}
