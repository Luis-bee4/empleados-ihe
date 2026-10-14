import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

/** Información principal que se muestra en la tabla. */
interface EmpleadoResumen {
  numeroEmpleado: string;
  curp: string;
  nombreCompleto: string;
  areaAdscripcion: string;
  puesto: string;
  fechaIngreso: string;
  estatus: string;
  fechaRegistro: string;
}

/** Respuesta paginada del backend. */
interface PaginaEmpleados {
  empleados: EmpleadoResumen[];
  pagina: number;
  tamanio: number;
  totalElementos: number;
  totalPaginas: number;
  primera: boolean;
  ultima: boolean;
}

/**
 * Muestra empleados con filtros, ordenamiento y paginación en el servidor.
 */
@Component({
  selector: 'app-lista-empleados',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './lista-empleados.html',
  styleUrl: './lista-empleados.css'
})
export class ListaEmpleados implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);

  readonly cargando = signal(false);
  readonly error = signal('');
  readonly resultado = signal<PaginaEmpleados | null>(null);

  /** Configuración de consulta aplicada a la tabla. */
  pagina = 0;
  tamanio = 10;
  ordenarPor = 'numeroEmpleado';
  direccion = 'ASC';

  readonly filtros = this.fb.nonNullable.group({
    numeroEmpleado: [''],
    curp: [''],
    nombreCompleto: [''],
    areaAdscripcion: [''],
    puesto: [''],
    estatus: ['']
  });

  /** Mantiene los filtros aplicados al cambiar de página. */
  private filtrosAplicados = this.filtros.getRawValue();

  readonly camposFiltro = [
    { clave: 'numeroEmpleado', etiqueta: 'Número de empleado' },
    { clave: 'curp', etiqueta: 'CURP' },
    { clave: 'nombreCompleto', etiqueta: 'Nombre completo' },
    { clave: 'areaAdscripcion', etiqueta: 'Área' },
    { clave: 'puesto', etiqueta: 'Puesto' },
    { clave: 'estatus', etiqueta: 'Estatus' }
  ];

  readonly columnas = [
    { clave: 'numeroEmpleado', etiqueta: 'Número de empleado' },
    { clave: 'curp', etiqueta: 'CURP' },
    { clave: 'nombreCompleto', etiqueta: 'Nombre completo' },
    { clave: 'areaAdscripcion', etiqueta: 'Área' },
    { clave: 'puesto', etiqueta: 'Puesto' },
    { clave: 'fechaIngreso', etiqueta: 'Fecha de ingreso' },
    { clave: 'estatus', etiqueta: 'Estatus' },
    { clave: 'fechaRegistro', etiqueta: 'Fecha de registro' }
  ];

  /** Carga la primera página al abrir la pantalla. */
  ngOnInit(): void {
    this.cargar();
  }

  /** Consulta únicamente la página solicitada al backend. */
  cargar(): void {
    if (this.cargando()) {
      return;
    }

    this.cargando.set(true);
    this.error.set('');
    this.resultado.set(null);

    let parametros = new HttpParams()
      .set('pagina', this.pagina)
      .set('tamanio', this.tamanio)
      .set('ordenarPor', this.ordenarPor)
      .set('direccion', this.direccion);

    for (const [campo, valor] of Object.entries(this.filtrosAplicados)) {
      if (valor.trim()) {
        parametros = parametros.set(campo, valor.trim());
      }
    }

    this.http.get<PaginaEmpleados>(
      'http://localhost:8080/api/empleados',
      { params: parametros }
    )
    .pipe(finalize(() => this.cargando.set(false)))
    .subscribe({
      next: respuesta => this.resultado.set(respuesta),
      error: (error: HttpErrorResponse) => {
        this.error.set(
          error.status === 0
            ? 'No se pudo conectar con el backend.'
            : 'No se pudo cargar la lista de empleados.'
        );
      }
    });
  }

  /** Aplica los filtros y vuelve a la primera página. */
  buscar(): void {
    if (this.cargando()) {
      return;
    }

    this.filtrosAplicados = this.filtros.getRawValue();
    this.pagina = 0;
    this.cargar();
  }

  /** Limpia los filtros y consulta nuevamente. */
  limpiar(): void {
    if (this.cargando()) {
      return;
    }

    this.filtros.reset();
    this.buscar();
  }

  /** Cambia de página dentro de los límites disponibles. */
  cambiarPagina(nuevaPagina: number): void {
    const datos = this.resultado();

    if (this.cargando() || !datos
        || nuevaPagina < 0 || nuevaPagina >= datos.totalPaginas) {
      return;
    }

    this.pagina = nuevaPagina;
    this.cargar();
  }

  /** Cambia la cantidad de filas y vuelve a la primera página. */
  cambiarTamanio(evento: Event): void {
    if (this.cargando()) {
      return;
    }

    this.tamanio = Number((evento.target as HTMLSelectElement).value);
    this.pagina = 0;
    this.cargar();
  }

  /** Ordena por una columna; otra pulsación invierte la dirección. */
  ordenar(campo: string): void {
    if (this.cargando()) {
      return;
    }

    this.direccion = this.ordenarPor === campo && this.direccion === 'ASC'
      ? 'DESC'
      : 'ASC';

    this.ordenarPor = campo;
    this.pagina = 0;
    this.cargar();
  }
}