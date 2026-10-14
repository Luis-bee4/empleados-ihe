package com.ihe.empleados.service;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.repository.EmpleadoRepository;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Contiene las reglas de negocio para administrar empleados.
 *
 * El controlador recibirá las solicitudes HTTP y utilizará este servicio.
 * El servicio consultará el repositorio para acceder a PostgreSQL.
 */
@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    /**
     * Recibe el repositorio mediante inyección de dependencias.
     *
     * @param empleadoRepository Repositorio de empleados.
     */
    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

   /**
 * Registra un empleado y genera automáticamente su número consecutivo.
 *
 * @param empleado Datos del empleado.
 * @return Empleado guardado con número, ID y fecha de registro.
 */
@Transactional
public Empleado registrar(Empleado empleado) {
    String curp = normalizar(empleado.getCurp());
    empleado.setCurp(curp);

    if (empleadoRepository.existsByCurp(curp)) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "La CURP ya está registrada"
        );
    }

    Long consecutivo = empleadoRepository.obtenerSiguienteNumero();

    // %03d utiliza al menos tres dígitos: 001, 002, ..., 1000.
    String numeroEmpleado = String.format(
        Locale.ROOT,
        "IHE%03d",
        consecutivo
    );

    empleado.setNumeroEmpleado(numeroEmpleado);

    return empleadoRepository.saveAndFlush(empleado);
}

    /**
     * Consulta un empleado mediante su CURP.
     *
     * @param curp CURP que se desea buscar.
     * @return Empleado encontrado.
     */
    @Transactional(readOnly = true)
    public Empleado consultarPorCurp(String curp) {
        String curpNormalizada = normalizar(curp);

        if (curpNormalizada.length() != 18) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La CURP debe tener 18 caracteres"
            );
        }

        return empleadoRepository.findByCurp(curpNormalizada)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró un empleado con esa CURP"
            ));
    }

    /**
     * Normaliza un identificador antes de guardarlo o consultarlo.
     *
     * @param valor Texto recibido.
     * @return Texto sin espacios exteriores y en mayúsculas.
     */
    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La CURP y el número de empleado no pueden estar vacíos"
            );
        }

        return valor.trim().toUpperCase(Locale.ROOT);
    }

    /**
 * Consulta un empleado por su identificador interno.
 *
 * @param id Identificador generado por la base de datos.
 * @return Empleado encontrado.
 */
@Transactional(readOnly = true)
public Empleado consultarPorId(Long id) {
    if (id == null || id <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "El ID debe ser mayor que cero"
        );
    }

    return empleadoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No existe empleado con ese ID"
        ));
}
}