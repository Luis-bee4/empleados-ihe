import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { finalize } from 'rxjs';

/** Información del empleado incluida en el reporte. */
interface EmpleadoReporte {
  id: number;
  numeroEmpleado: string;
  curp: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string | null;
  areaAdscripcion: string;
  puesto: string;
  fechaIngreso: string;
  estatus: string;
  fechaRegistro: string;
}

/** Respuesta del reporte generado por Spring Boot. */
interface ReporteRespuesta {
  fechaInicial: string;
  fechaFinal: string;
  totalEmpleados: number;
  empleados: EmpleadoReporte[];
}

/**
 * Valida una fecha yyyy-MM-dd recibida del control date.
 * Devuelve null cuando el valor no representa una fecha válida.
 */
function convertirFecha(valor: string): string | null {
  const partes = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor);

  if (!partes) {
    return null;
  }

  const anio = Number(partes[1]);
  const mes = Number(partes[2]);
  const dia = Number(partes[3]);

  if (anio < 1 || mes < 1 || mes > 12 || dia < 1) {
    return null;
  }

  const bisiesto =
    anio % 400 === 0 || (anio % 4 === 0 && anio % 100 !== 0);

  const diasPorMes = [
    31, bisiesto ? 29 : 28, 31, 30, 31, 30,
    31, 31, 30, 31, 30, 31
  ];

  return dia <= diasPorMes[mes - 1] ? valor : null;
}

/** Valida cada fecha; required comprueba los valores vacíos. */
function validarFecha(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  return convertirFecha(String(control.value))
    ? null
    : { fechaInvalida: true };
}

/** Comprueba que el periodo esté en orden cronológico. */
function validarPeriodo(control: AbstractControl): ValidationErrors | null {
  const inicial = convertirFecha(control.get('fechaInicial')?.value || '');
  const final = convertirFecha(control.get('fechaFinal')?.value || '');

  return inicial && final && inicial > final
    ? { periodoInvalido: true }
    : null;
}

/**
 * Consulta empleados por fecha de ingreso y exporta el reporte a CSV.
 */
@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css'
})
export class Reportes {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);

  readonly generando = signal(false);
  readonly exportando = signal(false);
  readonly reporte = signal<ReporteRespuesta | null>(null);
  readonly mensaje = signal('');
  readonly esError = signal(false);

  readonly formulario = this.fb.nonNullable.group({
    fechaInicial: ['', [Validators.required, validarFecha]],
    fechaFinal: ['', [Validators.required, validarFecha]]
  }, { validators: validarPeriodo });

  /** Muestra los errores de una fecha después de interactuar con ella. */
  errorFecha(campo: string): string {
    const control = this.formulario.get(campo);

    if (!control?.touched || !control.errors) {
      return '';
    }

    return control.hasError('required')
      ? 'La fecha es obligatoria.'
      : 'Selecciona una fecha válida.';
  }

  /** Obtiene un periodo válido para enviarlo al backend. */
  private obtenerPeriodo(): {
    fechaInicial: string;
    fechaFinal: string;
  } | null {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid) {
      this.esError.set(true);
      this.mensaje.set('Revisa las fechas del periodo.');
      return null;
    }

    return this.formulario.getRawValue();
  }

  /** Genera el reporte y muestra sus resultados en la tabla. */
  generarReporte(): void {
    if (this.generando() || this.exportando()) {
      return;
    }

    this.mensaje.set('');
    this.esError.set(false);
    this.reporte.set(null);

    const periodo = this.obtenerPeriodo();

    if (!periodo) {
      return;
    }

    this.generando.set(true);

    this.http.get<ReporteRespuesta>(
      'http://localhost:8080/api/empleados/reporte',
      { params: new HttpParams({ fromObject: periodo }) }
    )
    .pipe(finalize(() => this.generando.set(false)))
    .subscribe({
      next: respuesta => this.reporte.set(respuesta),
      error: (error: HttpErrorResponse) => this.mostrarError(error)
    });
  }

  /** Descarga el CSV del periodo seleccionado en el formulario. */
  exportarCsv(): void {
    if (this.generando() || this.exportando()) {
      return;
    }

    this.mensaje.set('');
    this.esError.set(false);

    const periodo = this.obtenerPeriodo();

    if (!periodo) {
      return;
    }

    this.exportando.set(true);

    this.http.get(
      'http://localhost:8080/api/empleados/reporte/csv',
      {
        params: new HttpParams({ fromObject: periodo }),
        responseType: 'blob'
      }
    )
    .pipe(finalize(() => this.exportando.set(false)))
    .subscribe({
      next: archivo => {
        const url = URL.createObjectURL(archivo);
        const enlace = document.createElement('a');

        enlace.href = url;
        enlace.download =
          `empleados_${periodo.fechaInicial}_${periodo.fechaFinal}.csv`;

        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();

        setTimeout(() => URL.revokeObjectURL(url), 1000);

        this.mensaje.set('Se inició la descarga del archivo CSV.');
      },
      error: (error: HttpErrorResponse) => this.mostrarError(error)
    });
  }

  /** Informa errores de conexión o procesamiento. */
  private mostrarError(error: HttpErrorResponse): void {
    this.esError.set(true);

    this.mensaje.set(
      error.status === 0
        ? 'No se pudo conectar con el backend. Verifica que esté encendido.'
        : 'No se pudo completar la solicitud. Revisa el periodo e inténtalo nuevamente.'
    );
  }
}