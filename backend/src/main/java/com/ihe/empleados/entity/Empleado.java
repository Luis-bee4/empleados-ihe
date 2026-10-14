package com.ihe.empleados.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un empleado del IHE.
 *
 * Cada instancia corresponde a un registro de la tabla empleados.
 * Las validaciones establecen los datos obligatorios y sus longitudes.
 */
@Entity
@Table(name = "empleados")
public class Empleado {

    /** Identificador interno generado por PostgreSQL. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** CURP obligatoria de exactamente 18 caracteres. */
    @NotBlank(message = "La CURP es obligatoria")
    @Size(min = 18, max = 18, message = "La CURP debe tener 18 caracteres")
    @Column(nullable = false, length = 18, unique = true)
    private String curp;

    /** Nombre o nombres del empleado. */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    /** Primer apellido obligatorio. */
    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "El primer apellido admite hasta 100 caracteres")
    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    /** Segundo apellido opcional. */
    @Size(max = 100, message = "El segundo apellido admite hasta 100 caracteres")
    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    /** Fecha de nacimiento obligatoria. */
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /** Sexo obligatorio; los valores permitidos se definirán según el ejercicio. */
    @NotBlank(message = "El sexo es obligatorio")
    @Column(nullable = false)
    private String sexo;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Pattern(
       regexp = "^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$",
      message = "El correo debe incluir un dominio con extensión, por ejemplo usuario@dominio.com"
   )
   @Size(max = 150, message = "El correo admite hasta 150 caracteres")
   @Column(name = "correo_electronico", nullable = false, length = 150)
   private String correoElectronico;

    /** Teléfono opcional de hasta 15 caracteres. */
    @Size(max = 15, message = "El teléfono admite hasta 15 caracteres")
    @Column(length = 15)
    private String telefono;

    /** Se genera en el backend y solo se incluye en las respuestas JSON. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(
       name = "numero_empleado",
       nullable = false,
       length = 20,
       unique = true,
       updatable = false
    )  
    private String numeroEmpleado;

    /** Área de adscripción obligatoria. */
    @NotBlank(message = "El área de adscripción es obligatoria")
    @Size(max = 150, message = "El área admite hasta 150 caracteres")
    @Column(name = "area_adscripcion", nullable = false, length = 150)
    private String areaAdscripcion;

    /** Puesto obligatorio. */
    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 150, message = "El puesto admite hasta 150 caracteres")
    @Column(nullable = false, length = 150)
    private String puesto;

    /** Fecha de ingreso obligatoria. */
    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    /** Estatus obligatorio; los valores permitidos se definirán según el ejercicio. */
    @NotBlank(message = "El estatus es obligatorio")
    @Column(nullable = false)
    private String estatus;

    /** Fecha y hora de registro, generadas al insertar el empleado. */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Constructor sin argumentos requerido por JPA. */
    public Empleado() {
    }

    /**
     * Asigna la fecha de registro antes de guardar un empleado nuevo.
     * No toma una fecha proporcionada por el cliente.
     */
    @PrePersist
    private void asignarFechaRegistro() {
        fechaRegistro = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }

    public String getAreaAdscripcion() { return areaAdscripcion; }
    public void setAreaAdscripcion(String areaAdscripcion) {
        this.areaAdscripcion = areaAdscripcion;
    }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
