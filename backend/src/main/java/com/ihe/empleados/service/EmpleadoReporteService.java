package com.ihe.empleados.service;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.repository.EmpleadoRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Genera reportes de empleados por periodo de ingreso.
 */
@Service
public class EmpleadoReporteService {

    private final EmpleadoRepository repositorio;

    /** Recibe el repositorio mediante inyección de dependencias. */
    public EmpleadoReporteService(EmpleadoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Valida el periodo y consulta los empleados por fecha de ingreso.
     *
     * @param fechaInicial Primer día del periodo, incluido.
     * @param fechaFinal Último día del periodo, incluido.
     * @return Lista de empleados que ingresaron dentro del periodo.
     */
    @Transactional(readOnly = true)
    public List<Empleado> generar(
            LocalDate fechaInicial,
            LocalDate fechaFinal) {

        if (fechaInicial == null || fechaFinal == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha inicial y la fecha final son obligatorias"
            );
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        return repositorio
            .findByFechaIngresoBetweenOrderByFechaIngresoAscIdAsc(
                fechaInicial,
                fechaFinal
            );
    }
}