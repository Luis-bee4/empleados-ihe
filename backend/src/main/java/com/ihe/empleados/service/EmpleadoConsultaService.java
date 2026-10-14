package com.ihe.empleados.service;

import com.ihe.empleados.entity.Empleado;
import com.ihe.empleados.repository.EmpleadoRepository;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consulta empleados con filtros, paginación y ordenamiento.
 */
@Service
public class EmpleadoConsultaService {

    private final EmpleadoRepository repositorio;

    /**
     * Recibe el repositorio mediante inyección de dependencias.
     */
    public EmpleadoConsultaService(EmpleadoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Obtiene una página de empleados que cumplan todos los filtros.
     *
     * Las páginas comienzan en cero.
     * Las fechas desde y hasta incluyen los días completos.
     */
    @Transactional(readOnly = true)
    public Page<Empleado> consultar(
            Map<String, String> filtros,
            LocalDate desde,
            LocalDate hasta,
            int pagina,
            int tamanio,
            String ordenarPor,
            String direccion) {

        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La página debe ser mayor o igual a 0 y el tamaño entre 1 y 100"
            );
        }

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha inicial no puede ser posterior a la final"
            );
        }

        List<String> columnas = List.of(
            "numeroEmpleado", "curp", "nombreCompleto", "areaAdscripcion",
            "puesto", "fechaIngreso", "estatus", "fechaRegistro"
        );

        if (!columnas.contains(ordenarPor)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Columna de ordenamiento inválida"
            );
        }

        if (!"ASC".equalsIgnoreCase(direccion)
                && !"DESC".equalsIgnoreCase(direccion)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La dirección debe ser ASC o DESC"
            );
        }

        Sort.Direction sentido = Sort.Direction.fromString(direccion);

        Sort orden = "nombreCompleto".equals(ordenarPor)
            ? Sort.by(sentido, "nombres", "primerApellido", "segundoApellido")
            : Sort.by(sentido, ordenarPor);

        // Desempatar por ID mantiene un orden estable entre páginas.
        orden = orden.and(Sort.by(Sort.Direction.ASC, "id"));

        Specification<Empleado> especificacion = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            // Aplicar búsquedas parciales sin distinguir mayúsculas.
            for (String campo : List.of(
                    "numeroEmpleado", "curp", "areaAdscripcion", "puesto")) {

                String valor = filtros.get(campo);

                if (valor != null && !valor.isBlank()) {
                    condiciones.add(cb.like(
                        cb.lower(raiz.<String>get(campo)),
                        patron(valor),
                        '\\'
                    ));
                }
            }

            // El estatus se compara de forma exacta.
            String estatus = filtros.get("estatus");
            if (estatus != null && !estatus.isBlank()) {
                condiciones.add(cb.equal(
                    cb.lower(raiz.<String>get("estatus")),
                    estatus.trim().toLowerCase(Locale.ROOT)
                ));
            }

            // Construir el nombre completo para permitir su búsqueda.
            String nombre = filtros.get("nombreCompleto");
            if (nombre != null && !nombre.isBlank()) {
                var nombreCompleto = cb.concat(
                    cb.concat(
                        cb.concat(raiz.<String>get("nombres"), " "),
                        raiz.<String>get("primerApellido")
                    ),
                    cb.concat(
                        " ",
                        cb.coalesce(raiz.<String>get("segundoApellido"), "")
                    )
                );

                condiciones.add(cb.like(
                    cb.lower(cb.trim(nombreCompleto)),
                    patron(nombre),
                    '\\'
                ));
            }

            // Incluir desde el inicio del primer día.
            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(
                    raiz.<LocalDateTime>get("fechaRegistro"),
                    desde.atStartOfDay()
                ));
            }

            // Incluir todo el último día mediante un límite exclusivo.
            if (hasta != null) {
                condiciones.add(cb.lessThan(
                    raiz.<LocalDateTime>get("fechaRegistro"),
                    hasta.plusDays(1).atStartOfDay()
                ));
            }

            return cb.and(condiciones.toArray(new Predicate[0]));
        };

        return repositorio.findAll(
            especificacion,
            PageRequest.of(pagina, tamanio, orden)
        );
    }

    /**
     * Escapa caracteres especiales de LIKE y permite coincidencias parciales.
     */
    private String patron(String valor) {
        String texto = valor.trim().toLowerCase(Locale.ROOT)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");

        return "%" + texto + "%";
    }
}