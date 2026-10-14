import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

/**
 * Captura y valida los datos para registrar empleados del IHE.
 */
@Component({
  selector: 'app-registrar-empleado',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './registrar-empleado.html',
  styleUrl: './registrar-empleado.css'
})
export class RegistrarEmpleado {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);

  guardando = false;
  mensaje = '';
  exito = false;

  /** Campos utilizados para construir el formulario en la plantilla. */
  readonly campos = [
    { clave: 'curp', etiqueta: 'CURP', tipo: 'text', maximo: 18, obligatorio: true },
    { clave: 'nombres', etiqueta: 'Nombre(s)', tipo: 'text', maximo: 100, obligatorio: true },
    { clave: 'primerApellido', etiqueta: 'Primer apellido', tipo: 'text', maximo: 100, obligatorio: true },
    { clave: 'segundoApellido', etiqueta: 'Segundo apellido', tipo: 'text', maximo: 100, obligatorio: false },
    { clave: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'date', maximo: 10, obligatorio: true },
    { clave: 'sexo', etiqueta: 'Sexo', tipo: 'text', maximo: 255, obligatorio: true },
    { clave: 'correoElectronico', etiqueta: 'Correo electrónico', tipo: 'email', maximo: 150, obligatorio: true },
    { clave: 'telefono', etiqueta: 'Teléfono', tipo: 'tel', maximo: 15, obligatorio: false },
    { clave: 'areaAdscripcion', etiqueta: 'Área de adscripción', tipo: 'text', maximo: 150, obligatorio: true },
    { clave: 'puesto', etiqueta: 'Puesto', tipo: 'text', maximo: 150, obligatorio: true },
    { clave: 'fechaIngreso', etiqueta: 'Fecha de ingreso', tipo: 'date', maximo: 10, obligatorio: true },
    { clave: 'estatus', etiqueta: 'Estatus', tipo: 'text', maximo: 255, obligatorio: true }
  ];

  /**
   * Modelo del formulario.
   * El patrón de texto obligatorio evita aceptar únicamente espacios.
   */
  readonly formulario = this.fb.nonNullable.group({
    curp: ['', [
      Validators.required,
      Validators.minLength(18),
      Validators.maxLength(18),
      Validators.pattern(/^[A-Za-z0-9]{18}$/)
    ]],
    nombres: ['', this.obligatorio(100)],
    primerApellido: ['', this.obligatorio(100)],
    segundoApellido: ['', Validators.maxLength(100)],
    fechaNacimiento: ['', Validators.required],
    sexo: ['', this.obligatorio(255)],
   correoElectronico: ['', [
   Validators.required,
   Validators.email,
   Validators.pattern(/^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/),
    Validators.maxLength(150)
   ]],
    telefono: ['', Validators.maxLength(15)],
    areaAdscripcion: ['', this.obligatorio(150)],
    puesto: ['', this.obligatorio(150)],
    fechaIngreso: ['', Validators.required],
    estatus: ['', this.obligatorio(255)]
  });

  /** Construye las reglas para los textos obligatorios. */
  private obligatorio(maximo: number) {
    return [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(maximo)
    ];
  }

  /** Devuelve un mensaje cuando el usuario ya interactuó con el campo. */
  errorCampo(clave: string): string {
    const control = this.formulario.get(clave);

    if (!control || !control.touched || !control.errors) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Este campo es obligatorio.';
    }

    if (clave === 'curp') {
      return 'La CURP debe contener exactamente 18 letras o números.';
    }

    if (clave === 'correoElectronico'
        && (control.hasError('email') || control.hasError('pattern'))) {
      return 'Escribe un correo con dominio y extensión, por ejemplo usuario@dominio.com.';
    }
    if (control.hasError('email')) {
      return 'Escribe un correo electrónico válido.';
    }

    if (control.hasError('maxlength')) {
      return `Máximo ${control.errors['maxlength'].requiredLength} caracteres.`;
    }

    return 'Escribe un valor que no contenga únicamente espacios.';
  }

  /**
   * Valida y envía el registro.
   * Conserva los datos capturados si el servidor rechaza la solicitud.
   */
  registrar(): void {
    if (this.guardando) {
      return;
    }

    this.mensaje = '';
    this.exito = false;
    this.formulario.markAllAsTouched();

    // Identificar qué controles impiden enviar el formulario.
Object.entries(this.formulario.controls).forEach(([campo, control]) => {
  if (control.invalid) {
    console.log('Campo inválido:', campo, control.errors);
  }
});

    if (this.formulario.invalid) {
  const camposInvalidos = this.campos
    .filter(campo => this.formulario.get(campo.clave)?.invalid)
    .map(campo => campo.etiqueta);

  this.mensaje = 'Revisa estos campos: ' + camposInvalidos.join(', ');
  return;
}

    const datos = this.formulario.getRawValue();

    const empleado = {
      ...datos,
      curp: datos.curp.trim().toUpperCase(),
      nombres: datos.nombres.trim(),
      primerApellido: datos.primerApellido.trim(),
      segundoApellido: datos.segundoApellido.trim() || null,
      sexo: datos.sexo.trim(),
      correoElectronico: datos.correoElectronico.trim(),
      telefono: datos.telefono.trim() || null,
      areaAdscripcion: datos.areaAdscripcion.trim(),
      puesto: datos.puesto.trim(),
      estatus: datos.estatus.trim()
    };

    this.guardando = true;

        this.http.post<{ mensaje: string }>(
      'http://localhost:8080/api/empleados',
      empleado
    )
    .pipe(finalize(() => this.guardando = false))
    .subscribe({
      next: respuesta => {
        this.exito = true;
        this.mensaje =
          respuesta.mensaje || 'Empleado registrado correctamente.';
        this.formulario.reset();
      },
      error: (error: HttpErrorResponse) => {
        this.exito = false;

        if (error.status === 0) {
          this.mensaje =
            'No se pudo conectar con el backend. Verifica que esté encendido.';

        } else if (error.status === 409) {
          this.mensaje = error.error?.mensaje
            || 'La CURP o el número de empleado ya están registrados.';

        } else if (error.status === 400) {
          const errores: Record<string, string> =
            error.error?.errores || {};

          const detalles = Object.entries(errores).map(
            ([campo, mensaje]) => {
              const etiqueta = this.campos.find(
                item => item.clave === campo
              )?.etiqueta || campo;

              return `${etiqueta}: ${mensaje}`;
            }
          );

          this.mensaje = detalles.length > 0
            ? detalles.join(' · ')
            : error.error?.mensaje || 'Revisa los datos capturados.';

        } else if (error.status === 503) {
          this.mensaje =
            'El servicio de datos no está disponible. Inténtalo más tarde.';

        } else {
          this.mensaje =
            'No se pudo registrar al empleado. Inténtalo nuevamente.';
        }
      }
    });
  }
}