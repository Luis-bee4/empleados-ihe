package com.ihe.empleados.controller;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.service.EmpleadoReporteService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Permite descargar el reporte de empleados como archivo CSV.
 */
@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(
    origins = "http://localhost:4200",
    exposedHeaders = "Content-Disposition"
)
public class EmpleadoCsvController {

    private final EmpleadoReporteService servicio;

    /** Recibe el servicio que consulta los empleados del periodo. */
    public EmpleadoCsvController(EmpleadoReporteService servicio) {
        this.servicio = servicio;
    }

    /**
     * Exporta todos los empleados del periodo de ingreso solicitado.
     *
     * @param fechaInicial Primer día del periodo, incluido.
     * @param fechaFinal Último día del periodo, incluido.
     * @return Archivo CSV codificado en UTF-8.
     */
    @GetMapping("/reporte/csv")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(name = "fechaInicial")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,

            @RequestParam(name = "fechaFinal")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal) {

        var empleados = servicio.generar(fechaInicial, fechaFinal);

        // El BOM ayuda a Excel a reconocer los acentos en UTF-8.
        StringBuilder csv = new StringBuilder("\uFEFF");

        csv.append(
            "Numero de empleado,CURP,Nombre completo,Area de adscripcion,"
            + "Puesto,Fecha de ingreso,Estatus,Fecha de registro\r\n"
        );

        for (Empleado empleado : empleados) {
            String nombreCompleto = Stream.of(
                    empleado.getNombres(),
                    empleado.getPrimerApellido(),
                    empleado.getSegundoApellido()
                )
                .filter(texto -> texto != null && !texto.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));

            csv.append(Stream.of(
                    empleado.getNumeroEmpleado(),
                    empleado.getCurp(),
                    nombreCompleto,
                    empleado.getAreaAdscripcion(),
                    empleado.getPuesto(),
                    empleado.getFechaIngreso(),
                    empleado.getEstatus(),
                    empleado.getFechaRegistro()
                )
                .map(this::celda)
                .collect(Collectors.joining(",")));

            csv.append("\r\n");
        }

        String nombreArchivo =
            "empleados_" + fechaInicial + "_" + fechaFinal + ".csv";

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + nombreArchivo + "\""
            )
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Escapa comillas y protege los valores que podrían interpretarse
     * como fórmulas al abrir el archivo en una hoja de cálculo.
     */
    private String celda(Object valor) {
        String texto = valor == null ? "" : valor.toString();

        if (valor instanceof String) {
            String inicio = texto.stripLeading();

            if (inicio.startsWith("=") || inicio.startsWith("+")
                    || inicio.startsWith("-") || inicio.startsWith("@")
                    || texto.startsWith("\t") || texto.startsWith("\r")
                    || texto.startsWith("\n")) {
                texto = "'" + texto;
            }
        }

        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}