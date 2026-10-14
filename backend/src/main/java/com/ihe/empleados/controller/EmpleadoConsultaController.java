package com.ihe.empleados.controller;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.service.EmpleadoConsultaService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * Expone la consulta general de empleados para la tabla del frontend.
 */
@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "http://localhost:4200")
public class EmpleadoConsultaController {

    private final EmpleadoConsultaService servicio;

    public EmpleadoConsultaController(EmpleadoConsultaService servicio) {
        this.servicio = servicio;
    }

    /**
     * Consulta empleados con filtros opcionales y devuelve información
     * de paginación junto con las filas que se mostrarán en la tabla.
     *
     * Las fechas se reciben con formato yyyy-MM-dd.
     */
    @GetMapping
    public Map<String, Object> consultar(
            @RequestParam Map<String, String> parametros,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio,
            @RequestParam(name = "ordenarPor", defaultValue = "fechaRegistro")
                String ordenarPor,
            @RequestParam(name = "direccion", defaultValue = "DESC")
                String direccion,
            @RequestParam(name = "desde", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        var resultado = servicio.consultar(
            parametros, desde, hasta, pagina, tamanio, ordenarPor, direccion
        );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(
            "empleados",
            resultado.getContent().stream().map(this::convertirFila).toList()
        );
        respuesta.put("pagina", resultado.getNumber());
        respuesta.put("tamanio", resultado.getSize());
        respuesta.put("totalElementos", resultado.getTotalElements());
        respuesta.put("totalPaginas", resultado.getTotalPages());
        respuesta.put("primera", resultado.isFirst());
        respuesta.put("ultima", resultado.isLast());

        return respuesta;
    }

    /**
     * Selecciona las columnas y construye el nombre completo,
     * considerando que el segundo apellido es opcional.
     */
    private Map<String, Object> convertirFila(Empleado empleado) {
        String nombreCompleto = Stream.of(
                empleado.getNombres(),
                empleado.getPrimerApellido(),
                empleado.getSegundoApellido()
            )
            .filter(texto -> texto != null && !texto.isBlank())
            .map(String::trim)
            .collect(Collectors.joining(" "));

        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("numeroEmpleado", empleado.getNumeroEmpleado());
        fila.put("curp", empleado.getCurp());
        fila.put("nombreCompleto", nombreCompleto);
        fila.put("areaAdscripcion", empleado.getAreaAdscripcion());
        fila.put("puesto", empleado.getPuesto());
        fila.put("fechaIngreso", empleado.getFechaIngreso());
        fila.put("estatus", empleado.getEstatus());
        fila.put("fechaRegistro", empleado.getFechaRegistro());

        return fila;
    }
}