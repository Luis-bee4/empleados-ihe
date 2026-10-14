package com.ihe.empleados.repository;

import java.time.LocalDate;

import com.ihe.empleados.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Permite acceder a los empleados almacenados en PostgreSQL.
 *
 * JpaRepository proporciona operaciones como guardar, listar y buscar por ID.
 * JpaSpecificationExecutor permite construir consultas con filtros combinables.
 * Spring Data JPA genera la implementación de esta interfaz automáticamente.
 */
public interface EmpleadoRepository
        extends JpaRepository<Empleado, Long>,
                JpaSpecificationExecutor<Empleado> {

    /**
     * Busca un empleado por su CURP.
     *
     * @param curp CURP que se desea consultar.
     * @return Empleado encontrado o un Optional vacío si no existe.
     */
    Optional<Empleado> findByCurp(String curp);

    /**
     * Comprueba si una CURP ya está registrada.
     *
     * @param curp CURP que se desea verificar.
     * @return true si existe un empleado con esa CURP.
     */
    boolean existsByCurp(String curp);

    /**
     * Comprueba si un número de empleado ya está registrado.
     *
     * @param numeroEmpleado Número que se desea verificar.
     * @return true si el número ya está registrado.
     */
    boolean existsByNumeroEmpleado(String numeroEmpleado);

    /**
    * Obtiene empleados cuya fecha de ingreso está dentro del periodo.
    *
    * Between incluye tanto la fecha inicial como la fecha final.
    *
    * @param fechaInicial Inicio del periodo.
    * @param fechaFinal Fin del periodo.
    * @return Empleados ordenados por fecha de ingreso e ID.
    */
    List<Empleado> findByFechaIngresoBetweenOrderByFechaIngresoAscIdAsc(
      LocalDate fechaInicial,
      LocalDate fechaFinal
      );
      /**
     * Obtiene un número único de la secuencia de PostgreSQL.
     */
     @Query(value = "SELECT nextval('public.empleado_numero_seq')", nativeQuery = true)
     Long obtenerSiguienteNumero();
}