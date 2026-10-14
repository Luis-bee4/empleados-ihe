package com.ihe.empleados.controller;

import jakarta.validation.Valid;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.service.EmpleadoService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Expone los servicios REST para administrar empleados.
 *
 * Permite que el frontend Angular consulte al backend mediante HTTP.
 */
@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "http://localhost:4200")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    /**
     * Recibe el servicio mediante inyección de dependencias.
     *
     * @param empleadoService Servicio con las reglas de negocio.
     */
    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    /**
     * Consulta un empleado por CURP y devuelve sus datos.
     *
     * @param curp CURP capturada en la pantalla de consulta.
     * @return HTTP 200 si existe, 404 si no existe o 400 si es inválida.
     */
    @GetMapping("/curp/{curp}")
    public ResponseEntity<Map<String, Object>> consultarPorCurp(
            @PathVariable("curp") String curp) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        try {
            Empleado empleado = empleadoService.consultarPorCurp(curp);

            respuesta.put("existe", true);
            respuesta.put("mensaje", "Existe empleado");
            respuesta.put("empleado", empleado);

            return ResponseEntity.ok(respuesta);

        } catch (ResponseStatusException excepcion) {
            // Solamente una búsqueda sin resultados significa que no existe.
            if (excepcion.getStatusCode().value()
                    == HttpStatus.NOT_FOUND.value()) {

                respuesta.put("existe", false);
                respuesta.put("mensaje", "No existe empleado con esa CURP");
                respuesta.put("empleado", null);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(respuesta);
            }

            // Una CURP inválida se informa como error de captura.
            if (excepcion.getStatusCode().value()
                    == HttpStatus.BAD_REQUEST.value()) {

                respuesta.put("mensaje", excepcion.getReason());

                return ResponseEntity.badRequest().body(respuesta);
            }

            throw excepcion;
        }
    }
    /**
 * Consulta un empleado por ID y devuelve sus datos.
 *
 * @param id Identificador interno del empleado.
 * @return HTTP 200 si existe, 404 si no existe o 400 si el ID es inválido.
 */
@GetMapping("/id/{id}")
public ResponseEntity<Map<String, Object>> consultarPorId(
        @PathVariable("id") Long id) {

    Map<String, Object> respuesta = new LinkedHashMap<>();

    try {
        Empleado empleado = empleadoService.consultarPorId(id);

        respuesta.put("existe", true);
        respuesta.put("mensaje", "Existe empleado");
        respuesta.put("empleado", empleado);

        return ResponseEntity.ok(respuesta);

    } catch (ResponseStatusException excepcion) {
        if (excepcion.getStatusCode().value() == 404) {
            respuesta.put("existe", false);
            respuesta.put("empleado", null);
        }

        respuesta.put("mensaje", excepcion.getReason());

        return ResponseEntity.status(excepcion.getStatusCode())
            .body(respuesta);
    }
}
/**
 * Registra un empleado con los datos recibidos.
 */
@PostMapping
public ResponseEntity<Map<String, Object>> registrar(
        @Valid @RequestBody Empleado empleado) {

    Empleado registrado = empleadoService.registrar(empleado);

    Map<String, Object> respuesta = new LinkedHashMap<>();
    respuesta.put("mensaje", "Empleado registrado correctamente");
    respuesta.put("empleado", registrado);

    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
}
}