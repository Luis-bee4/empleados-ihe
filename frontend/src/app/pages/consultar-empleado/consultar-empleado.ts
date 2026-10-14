import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

/** Datos del empleado devueltos por el backend. */
interface EmpleadoRespuesta {
  id: number;
  curp: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string | null;
  fechaNacimiento: string;
  sexo: string;
  correoElectronico: string;
  telefono: string | null;
  numeroEmpleado: string;
  areaAdscripcion: string;
  puesto: string;
  fechaIngreso: string;
  estatus: string;
  fechaRegistro: string;
}

/** Respuesta del servicio de consulta por CURP. */
interface ConsultaCurpRespuesta {
  existe: boolean;
  mensaje: string;
  empleado: EmpleadoRespuesta | null;
}

/**
 * Consulta un empleado por CURP mediante el servicio REST de Spring Boot.
 */
@Component({
  selector: 'app-consultar-empleado',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './consultar-empleado.html',
  styleUrl: './consultar-empleado.css'
})
export class ConsultarEmpleado {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);

  /** Signals actualizan la pantalla cuando termina la petición HTTP. */
  readonly buscando = signal(false);
  readonly mensaje = signal('');
  readonly empleado = signal<EmpleadoRespuesta | null>(null);
  readonly esError = signal(false);

  readonly formulario = this.fb.nonNullable.group({
    curp: ['', [
      Validators.required,
      Validators.pattern(/^[A-Za-z0-9]{18}$/)
    ]]
  });

  /** Explica los errores del campo después de interactuar con él. */
  get errorCurp(): string {
    const control = this.formulario.controls.curp;

    if (!control.touched || control.valid) {
      return '';
    }

    return control.hasError('required')
      ? 'La CURP es obligatoria.'
      : 'La CURP debe contener exactamente 18 letras o números.';
  }

  /**
   * Busca la CURP y elimina los resultados de cualquier consulta anterior.
   */
  buscar(): void {
    if (this.buscando()) {
      return;
    }

    this.empleado.set(null);
    this.mensaje.set('');
    this.esError.set(false);

    // Normalizar antes de validar y enviar.
    const curp = this.formulario.controls.curp.value.trim().toUpperCase();
    this.formulario.controls.curp.setValue(curp);
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid) {
      return;
    }

    this.buscando.set(true);

    this.http.get<ConsultaCurpRespuesta>(
      `http://localhost:8080/api/empleados/curp/${encodeURIComponent(curp)}`
    )
    .pipe(finalize(() => this.buscando.set(false)))
    .subscribe({
      next: respuesta => {
        this.empleado.set(respuesta.empleado);
        this.mensaje.set(respuesta.mensaje);
      },
      error: (error: HttpErrorResponse) => {
        this.esError.set(true);

        if (error.status === 404) {
          this.mensaje.set(
            error.error?.mensaje || 'No existe empleado con esa CURP.'
          );
        } else if (error.status === 0) {
          this.mensaje.set(
            'No se pudo conectar con el backend. Verifica que esté encendido.'
          );
        } else {
          this.mensaje.set(
            error.error?.mensaje || 'No se pudo realizar la consulta.'
          );
        }
      }
    });
  }
}