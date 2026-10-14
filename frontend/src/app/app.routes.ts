import { Routes } from '@angular/router';

/*
  Rutas de las pantallas de administración de empleados IHE.
 */

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/inicio/inicio').then(m => m.Inicio)
  },
  {
    path: 'registrar-empleado',
    loadComponent: () =>
      import('./pages/registrar-empleado/registrar-empleado')
        .then(m => m.RegistrarEmpleado)
  },
  {
    path: 'consultar-empleado',
    loadComponent: () =>
      import('./pages/consultar-empleado/consultar-empleado')
        .then(m => m.ConsultarEmpleado)
  },
  {
    path: 'lista-empleados',
    loadComponent: () =>
      import('./pages/lista-empleados/lista-empleados')
        .then(m => m.ListaEmpleados)
  },
  {
    path: 'reportes',
    loadComponent: () =>
      import('./pages/reportes/reportes').then(m => m.Reportes)
  },
  {
    path: '**',
    redirectTo: ''
  }
];