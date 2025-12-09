package com.gestionmedica.gestionmedica.modelo.enums;

public enum EstadoCita {
    REGISTRADA,    // Cita recién creada
    CONFIRMADA,    // Médico/sistema confirmó
    CANCELADA,     // Fue cancelada
    ATENDIDA       // Ya se realizó la consulta
}