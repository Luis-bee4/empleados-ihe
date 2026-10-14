package com.ihe.empleados.controller;

import com.ihe.empleados.service.EmpleadoReporteService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Expone el reporte de empleados por fecha de ingreso.
 */
@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "http://localhost:4200")
public class EmpleadoReporteController {

    private final EmpleadoReporteService servicio;

    /** Recibe el servicio de reportes mediante inyección de dependencias. */
    public EmpleadoReporteController(EmpleadoReporteService servicio) {
        this.servicio = servicio;
    }

    /**
     * Genera un reporte con los empleados del periodo solicitado.
     *
     * @param fechaInicial Inicio del periodo en formato yyyy-MM-dd.
     * @param fechaFinal Fin del periodo en formato yyyy-MM-dd.
     * @return Periodo, total de empleados y sus datos.
     */
    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Object>> generar(
            @RequestParam(name = "fechaInicial")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,

            @RequestParam(name = "fechaFinal")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        try {
            var empleados = servicio.generar(fechaInicial, fechaFinal);

            respuesta.put("fechaInicial", fechaInicial);
            respuesta.put("fechaFinal", fechaFinal);
            respuesta.put("totalEmpleados", empleados.size());
            respuesta.put("empleados", empleados);

            return ResponseEntity.ok(respuesta);

        } catch (ResponseStatusException excepcion) {
            respuesta.put("mensaje", excepcion.getReason());

            return ResponseEntity.status(excepcion.getStatusCode())
                .body(respuesta);
        }
    }
}