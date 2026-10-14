# Registro de empleados IHE

Aplicación web para administrar empleados mediante **Angular**, **Spring Boot** y **PostgreSQL**.

## Funcionalidades

- Registrar empleados con validaciones.
- Generar automáticamente el número de empleado: `IHE001`, `IHE002`…
- Consultar empleados por CURP e ID.
- Listar empleados con filtros, ordenamiento y paginación.
- Generar reportes por periodo de **fecha de ingreso**.
- Exportar los reportes a CSV.

## Estructura del proyecto

| Carpeta / archivo | Contenido |
|---|---|
| `backend/` | API REST desarrollada con Spring Boot |
| `frontend/` | Interfaz web desarrollada con Angular |
| `database/01_schema.sql` | Tablas, restricciones y secuencias de PostgreSQL |
| `README.md` | Instrucciones de configuración y ejecución |

## 1. Requisitos

| Herramienta | Requisito |
|---|---|
| Java | JDK 21 |
| Node.js y npm | Compatibles con la versión de Angular de `frontend/package.json` |
| PostgreSQL | Versión 18, con el servicio encendido |
| PowerShell | Versión 7 para utilizar `Read-Host -MaskInput` |
| Maven | Se incluye Maven Wrapper; no requiere instalación independiente |

Se necesita conexión a internet para descargar las dependencias inicialmente.

### Verificar instalaciones

```powershell
java -version
node -v
npm -v
psql --version
```

## 2. Base de datos

| Parámetro | Valor |
|---|---|
| Servidor | `localhost` |
| Puerto | `5432` |
| Base de datos | `ihe_empleados` |
| Usuario | `postgres` |

> **Si la base de datos ya existe y está configurada, omite su creación y la importación del esquema.**

### Aplicación de empleados IHE

## 3. Ejecutar el backend

Abre una terminal de PowerShell y ejecuta:

```powershell
Set-Location "C:\Users\Pc\Desktop\EXAMEN\backend"
.\mvnw.cmd spring-boot:run
```

Espera a que aparezca el mensaje:

```text
Started EmpleadosApplication
```

Deja esta terminal abierta.

## 4. Ejecutar el frontend

Abre otra terminal de PowerShell.

La primera vez, instala las dependencias:

```powershell
Set-Location "C:\Users\Pc\Desktop\EXAMEN\frontend"
npm ci
```

Después inicia la aplicación:

```powershell
npx ng serve --open
```

El navegador abrirá la aplicación en:

[http://localhost:4200](http://localhost:4200)

Para las siguientes ejecuciones:

```powershell
Set-Location "C:\Users\Pc\Desktop\EXAMEN\frontend"
npx ng serve --open
```

### Detener la aplicación

Presiona **Ctrl + C** en cada terminal.

> Mantén PostgreSQL encendido y ambas terminales abiertas mientras utilizas la aplicación. Si guardaste el proyecto en otra ubicación, cambia las rutas de los comandos.

## 5. Pantallas

| Pantalla | Función |
|---|---|
| Inicio | Menú de navegación |
| Registrar empleado | Captura y validación de datos |
| Consultar empleado | Búsqueda por CURP |
| Lista de empleados | Tabla con filtros, ordenamiento y paginación |
| Reportes | Consulta por fecha de ingreso y descarga CSV |

El **número de empleado** y la **fecha de registro** se asignan automáticamente.

PostgreSQL impide valores duplicados mediante restricciones `UNIQUE` en CURP y número de empleado.

## 6. API REST

**URL base:** [http://localhost:8080](http://localhost:8080)

| Método | Endpoint | Función |
|---|---|---|
| `POST` | `/api/empleados` | Registrar empleado |
| `GET` | `/api/empleados/curp/{curp}` | Consultar por CURP |
| `GET` | `/api/empleados/id/{id}` | Consultar por ID interno |
| `GET` | `/api/empleados` | Consulta general |
| `GET` | `/api/empleados/reporte` | Reporte por fecha de ingreso |
| `GET` | `/api/empleados/reporte/csv` | Exportar reporte a CSV |

### Consulta general

```text
http://localhost:8080/api/empleados?pagina=0&tamanio=10&ordenarPor=numeroEmpleado&direccion=ASC
```

| Parámetro | Descripción |
|---|---|
| `pagina` | Número de página; comienza en `0` |
| `tamanio` | Registros por página, entre `1` y `100` |
| `ordenarPor` | Campo de ordenamiento |
| `direccion` | `ASC` o `DESC` |
| `numeroEmpleado` | Filtro por número |
| `curp` | Filtro por CURP |
| `nombreCompleto` | Filtro por nombre y apellidos |
| `areaAdscripcion` | Filtro por área |
| `puesto` | Filtro por puesto |
| `estatus` | Filtro por estatus |
| `desde` y `hasta` | Periodo de **fecha de registro**, formato `yyyy-MM-dd` |

Campos disponibles para ordenar: `numeroEmpleado`, `curp`, `nombreCompleto`, `areaAdscripcion`, `puesto`, `fechaIngreso`, `estatus` y `fechaRegistro`.

### Reporte por fecha de ingreso

```text
http://localhost:8080/api/empleados/reporte?fechaInicial=2026-01-01&fechaFinal=2026-03-31
```

Ambas fechas son obligatorias. El resultado incluye los empleados del periodo y el **total de registros encontrados**.

### Exportación CSV

```text
http://localhost:8080/api/empleados/reporte/csv?fechaInicial=2026-01-01&fechaFinal=2026-03-31
```

Nombre del archivo descargado:

```text
empleados_2026-01-01_2026-03-31.csv
```


## 7. Solución de problemas

| Problema | Qué revisar |
|---|---|
| No conecta con el backend | Spring Boot debe estar ejecutándose en `8080` |
| No conecta con PostgreSQL | Servicio, puerto, usuario y contraseña |
| Error de CORS | Abrir Angular desde http://localhost:4200 |
| Puerto ocupado | Detener el proceso que utiliza `8080` o `4200` |
| Comando no reconocido | Instalación, PATH y apertura de una terminal nueva |
| CURP duplicada | Utilizar una CURP que no esté registrada |