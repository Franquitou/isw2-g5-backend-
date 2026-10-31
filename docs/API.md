# API — Liga Amateur Santafesina

Contrato REST acordado por Franco (A) y Marcos (B) para los cuatro módulos. Los endpoints se implementan progresivamente en PRs separados; **este documento define interfaces, no acredita por sí mismo que funcionen**. Esta rama añade RF-02; consultar cada PR integrado para conocer el estado real de los demás módulos. Actualizar este archivo en el mismo PR que cambie una interfaz.

Fuente funcional: `liga_amateur_opencode/SPEC.md` y el PDF del cliente (ambos en la raíz del espacio de trabajo); estructura exacta: `backend/src/main/resources/schema.sql` (ruta relativa a la raíz del repositorio backend). Las aclaraciones acordadas constan en `docs/PREGUNTAS_PENDIENTES.md`. No crear tablas ni operaciones de negocio fuera de ese alcance.

## 1. Convenciones generales

| Tema | Convención |
|---|---|
| Rutas | Prefijo `/api`, recursos plurales. Los parámetros `{id}`, `{torneoId}`, `{equipoId}`, `{partidoId}`, `{eventoId}` son IDs `INT` positivos generados por H2; `numeroFecha` es una jornada entera desde 1. |
| Formato | JSON UTF-8, `Content-Type: application/json` en cuerpos; propiedades `camelCase`. Las listas son arreglos JSON, sin paginación obligatoria. |
| Fechas y horas | ISO-8601 con offset para `fechaHoraInicio` y `fechaAlta`. Interpretar el `TIMESTAMP` sin zona de H2 en `America/Argentina/Buenos_Aires`: al recibir un valor con offset, convertir el instante a esa zona y guardar la fecha/hora local; al leer, asociar esa zona al valor y emitir ISO-8601 con su offset. Ej.: `2026-10-28T18:00:00-03:00`. No confundir `numeroFecha` (jornada) con una fecha calendario. Los `timestamp` de error son instantes UTC (`Z`). |
| Respuestas | Siempre DTOs; nunca entidades JPA, contraseñas ni hashes. `201` con DTO al crear; `200` con DTO al consultar/actualizar; `204` sin cuerpo al borrar. |
| Errores | Contrato común `ApiErrorResponse` (sección 3). |
| Autenticación | JWT Bearer de 8 horas, firmado con HS256 y secreto seguro por variable de entorno, nunca versionado. BCrypt y autenticación RF-01 ya integrados en `main`. |
| Expo | URL base configurable por entorno/dispositivo; `localhost` del teléfono no es el servidor. No publicar el secreto JWT en la app ni en variables públicas de Expo. |

La conversión de `fechaHoraInicio` se aplica antes de comparar los inicios para RN-01/RN-02; dos horas exactas de separación son válidas. Esta convención no modifica el tipo `TIMESTAMP` del SQL.

## 2. Autenticación y permisos

- Los endpoints públicos no requieren token.
- Los endpoints protegidos esperan `Authorization: Bearer <token>`; nunca se envía el secreto de firma al cliente.
- Sin token o con token inválido/expirado: **HTTP 401**.
- Con token válido pero rol insuficiente: **HTTP 403**.
- Roles: `ADMIN` y `DELEGADO`. Un usuario inactivo no puede autenticarse (RF-01, RN-09).
- Los 401/403 originados por Spring Security utilizan el cuerpo de la sección 3 mediante la configuración integrada en RF-01, no mediante `@RestControllerAdvice` por sí solo.

### Matriz de permisos

| Requerimiento | Público | Delegado | Admin |
|---|---:|---:|---:|
| RF-01 Iniciar sesión | Sí | Sí | Sí |
| RF-02 Administrar usuarios | No | No | Sí |
| RF-03 Gestionar equipos | No | No | Sí |
| RF-04 Gestionar jugadores | No | No | Sí |
| RF-05 Consultar catálogo | Sí | Sí | Sí |
| RF-06 Programar partidos | No | No | Sí |
| RF-07 Cambiar etapa de partido | No | Sí | Sí |
| RF-08 Registrar eventos | No | Sí | Sí |
| RF-09 Corregir eventos | No | Sí | Sí |
| RF-10 Consultar partidos y resultados | Sí | Sí | Sí |
| RF-11 Tabla de posiciones | Sí | Sí | Sí |
| RF-12 Suspendidos de una fecha | Sí | Sí | Sí |

## 3. Formato de error (implementado para errores de Spring MVC)

El cuerpo común está implementado para errores manejados por Spring MVC (validación de cuerpo, JSON mal formado, 404, 405 y fallos inesperados) y para 401/403 de Spring Security. La prueba específica de 415 sigue pendiente. Esta rama incorpora 409 para conflictos de RF-02; las reglas de otros módulos deben mapear sus propios conflictos al mismo cuerpo (ver `PREGUNTAS_PENDIENTES.md`).

El formato definido es:

```json
{
  "status": 400,
  "message": "La solicitud contiene datos inválidos",
  "timestamp": "2026-10-28T12:00:00Z",
  "path": "/ruta/solicitada",
  "validationErrors": [
    { "field": "nombre", "message": "no debe estar vacío" }
  ]
}
```

| Campo | Descripción |
|---|---|
| `status` | Código HTTP de la respuesta. |
| `message` | Mensaje comprensible y seguro; no revela detalles internos. |
| `timestamp` | Instante UTC en que se generó la respuesta. |
| `path` | Ruta solicitada, si se conoce. |
| `validationErrors` | Lista de errores de validación; vacía salvo en errores 400 de validación. No incluye el valor rechazado. |

### Códigos de estado

| Código | Uso |
|---|---|
| 400 | JSON/ID/parámetro inválido o validación de entrada fallida. |
| 401 | Credenciales incorrectas, token ausente, inválido o expirado en una operación protegida. No indicar qué credencial falló. |
| 403 | El usuario autenticado no tiene el rol requerido. |
| 404 | ID de recurso inexistente. |
| 405 | Método HTTP no admitido. |
| 409 | Conflicto de unicidad, de estado o de una regla de negocio. RF-02 usa este cuerpo para username repetido y RN-08; otros módulos deben implementar sus propios casos. |
| 415 | `Content-Type` no admitido. |
| 500 | Error interno; la traza queda en el log del servidor, nunca en la respuesta. |

Los errores inesperados se registran en el servidor antes de devolver un 500 genérico. Las validaciones y mensajes nunca deben incluir contraseñas, hashes, tokens, valores rechazados ni trazas. Los filtros de seguridad producen esta respuesta para 401/403; el manejo MVC no los cubre por sí mismo. En las secciones siguientes, los códigos indican el **contrato HTTP**, no que todos los endpoints estén integrados en `main`.

## 4. Franco (A): autenticación y usuarios — RF-01, RF-02

| Método y ruta | Acceso | Entrada | Respuesta exitosa | Errores relevantes |
|---|---|---|---|---|
| `POST /api/auth/login` | Público | `LoginRequest`: `username` y `password` obligatorios. | `200 LoginResponse`: `token`, `nombreCompleto`, `rol`. | `400` cuerpo inválido; `401` credenciales incorrectas o usuario inactivo, sin revelar cuál dato falló. |
| `GET /api/usuarios?activo=true\|false` | ADMIN | Filtro opcional `activo`; sin filtro muestra activos. | `200 UsuarioResponse[]`. | `400` filtro inválido; `401`/`403`. |
| `GET /api/usuarios/{id}` | ADMIN | ID positivo; permite ver el estado de un usuario inactivo. | `200 UsuarioResponse`. | `400`, `401`, `403`, `404`. |
| `POST /api/usuarios` | ADMIN | `UsuarioCreateRequest`: `username`, `password`, `nombreCompleto`, `rol`. | `201 UsuarioResponse`. | `400` campos inválidos; `401`/`403`; `409` username repetido. |
| `PUT /api/usuarios/{id}` | ADMIN | `UsuarioUpdateRequest`: **ambos** `nombreCompleto` y `rol`; no se admite cambiar `username`. | `200 UsuarioResponse`. | `400`, `401`, `403`, `404`; `409` si un admin intenta quitarse ADMIN. |
| `PATCH /api/usuarios/{id}/password` | ADMIN | `PasswordUpdateRequest`: `password`. | `200 UsuarioResponse`, sin password/hash. | `400` longitud inválida; `401`/`403`/`404`. |
| `PATCH /api/usuarios/{id}/estado` | ADMIN | `UsuarioEstadoRequest`: `activo` booleano obligatorio. Desactiva o reactiva. | `200 UsuarioResponse`. | `400`, `401`, `403`, `404`; `409` si un admin intenta desactivarse a sí mismo. |

`UsuarioResponse` contiene `id`, `username`, `nombreCompleto`, `rol`, `activo`, `fechaAlta`; nunca `password`, `passwordHash` ni el hash BCrypt. `username` obligatorio, único e inmutable, máximo 40 caracteres; `password` mínimo 8 caracteres y se almacena solo con BCrypt; `nombreCompleto` obligatorio, máximo 100; `rol` es `ADMIN` o `DELEGADO`. RN-08 se comprueba con la identidad del administrador autenticado. No existe registro público de usuarios ni recuperación de contraseña.

**Estado de RF-02 en esta rama:** controlador, servicio y DTOs incorporados; 409 usa el formato común sin exponer datos internos. La respuesta `fechaAlta` interpreta el `TIMESTAMP` de la base en `America/Argentina/Buenos_Aires` y lo emite con offset ISO-8601. El alta recarga el valor por defecto generado por la base antes de responder. Su funcionamiento se verifica en los tests aislados de usuario y H2 en memoria; aún no está fusionado a `main`.

Ejemplos (RF-01 y RF-02):

```http
POST /api/auth/login
Content-Type: application/json

{"username":"admin","password":"<contraseña>"}
```

```json
{"token":"<jwt>","nombreCompleto":"Administrador de la liga","rol":"ADMIN"}
```

```http
POST /api/usuarios
Authorization: Bearer <token>
Content-Type: application/json

{"username":"delegado1","password":"<contraseña-de-8-o-más-caracteres>","nombreCompleto":"Delegado Uno","rol":"DELEGADO"}
```

```json
{"id":2,"username":"delegado1","nombreCompleto":"Delegado Uno","rol":"DELEGADO","activo":true,"fechaAlta":"2026-10-28T18:00:00-03:00"}
```

Para editar se envía `{"nombreCompleto":"Delegado Uno","rol":"DELEGADO"}` completo; para cambiar la contraseña `{"password":"<contraseña-nueva>"}`; para reactivar `{"activo":true}`. Los cuerpos de respuesta de estos cambios tienen la forma de `UsuarioResponse`.

## 5. Marcos (B): catálogo, equipos y jugadores — RF-03, RF-04, RF-05

| Método y ruta | Acceso | Entrada | Respuesta exitosa | Errores relevantes |
|---|---|---|---|---|
| `GET /api/torneos` | Público | Sin cuerpo. | `200 TorneoResponse[]`, solo activos. | Errores HTTP generales. |
| `GET /api/torneos/{torneoId}/equipos` | Público | ID de torneo. | `200 EquipoResponse[]`, solo activos. | `400` ID inválido; `404` torneo inexistente. |
| `GET /api/equipos/{equipoId}/jugadores` | Público | ID de equipo. | `200 JugadorResponse[]`, solo activos. | `400` ID inválido; `404` equipo inexistente. |
| `POST /api/torneos/{torneoId}/equipos` | ADMIN | `EquipoCreateRequest`: `nombre`, `barrio` opcional; `torneoId` procede de la ruta. | `201 EquipoResponse`. | `400` entrada inválida; `401`/`403`; `404` torneo inexistente; `409` nombre repetido en el torneo. |
| `PUT /api/equipos/{id}` | ADMIN | `EquipoUpdateRequest`: `nombre` obligatorio y `barrio` incluido, que puede ser `null`; no cambia de torneo. | `200 EquipoResponse`. | `400`, `401`, `403`, `404`, `409` por nombre repetido. |
| `DELETE /api/equipos/{id}` | ADMIN | Sin cuerpo; **baja lógica** (`activo=false`). | `204` sin cuerpo. | `400`, `401`, `403`, `404`. |
| `POST /api/equipos/{equipoId}/jugadores` | ADMIN | `JugadorCreateRequest`: `nombre`, `apellido`, `dni`, `numeroCamiseta` opcional. | `201 JugadorResponse`. | `400`, `401`, `403`, `404` equipo inexistente, `409` DNI repetido. |
| `PUT /api/jugadores/{id}` | ADMIN | `JugadorUpdateRequest`: `nombre`, `apellido`, `dni`, `numeroCamiseta` (puede ser `null`); **no** recibe `equipoId`. | `200 JugadorResponse`. | `400`, `401`, `403`, `404`, `409` DNI repetido. |
| `DELETE /api/jugadores/{id}` | ADMIN | Sin cuerpo; **baja lógica** (`activo=false`). | `204` sin cuerpo. | `400`, `401`, `403`, `404`. |

`TorneoResponse`: `id`, `nombre`, `anio`, `activo`. Es un dato precargado, **sin endpoints de escritura**. Las consultas de canchas y árbitros pertenecen a José (sección 6). `EquipoResponse`: `id`, `torneoId`, `nombre`, `barrio`, `activo`; nombre obligatorio, máximo 80, único dentro del torneo; barrio opcional, máximo 80. `JugadorResponse`: `id`, `equipoId`, `nombre`, `apellido`, `dni`, `numeroCamiseta`, `activo`; nombre/apellido obligatorios, máximo 60 cada uno; DNI obligatorio, único global, máximo 10 caracteres (se trata como cadena, no se impone formato numérico adicional). `numeroCamiseta` es `null` u opcional, y cuando se informa va de 1 a 99 (`@Min(1)`, `@Max(99)` y `CHECK` SQL); **no se exige unicidad de camiseta**. El equipo del jugador existente es inmutable. Equipo desactivado no puede elegirse para nuevos partidos. No se incorporan endpoints de reactivación de equipos o jugadores. La baja lógica no borra el historial; P-06 deja abierta la consecuencia sobre partidos programados, por lo que **no se define aquí una cancelación automática**.

Ejemplos (RF-03 a RF-05):

```http
POST /api/torneos/1/equipos
Authorization: Bearer <token>
Content-Type: application/json

{"nombre":"Barrio Norte","barrio":null}
```

```json
{"id":7,"torneoId":1,"nombre":"Barrio Norte","barrio":null,"activo":true}
```

```http
POST /api/equipos/7/jugadores
Authorization: Bearer <token>
Content-Type: application/json

{"nombre":"Juan","apellido":"Pérez","dni":"30111222","numeroCamiseta":null}
```

```json
{"id":15,"equipoId":7,"nombre":"Juan","apellido":"Pérez","dni":"30111222","numeroCamiseta":null,"activo":true}
```

Un `PUT /api/jugadores/15` vuelve a incluir los cuatro campos editables; `DELETE /api/jugadores/15` responde `204`. Ejemplo de lectura pública: `GET /api/torneos/1/equipos` → `[{"id":7,"torneoId":1,"nombre":"Barrio Norte","barrio":null,"activo":true}]`. Los ejemplos ilustran el formato, no son datos precargados ni pruebas ejecutadas.

## 6. José (C): canchas, árbitros, programación, estados y listado — RF-05 (canchas y árbitros), RF-06, RF-07, RF-10 (listado)

### 6.1 Canchas y árbitros (implementado)

| Método y ruta | Acceso | Entrada | Respuesta exitosa | Errores relevantes |
|---|---|---|---|---|
| `GET /api/canchas` | Público | Sin cuerpo. | `200 CanchaResponse[]`, solo activas, ordenadas por `nombre`. | Errores HTTP generales. Las escrituras no existen: Spring Security las rechaza con `401`/`403`. |
| `GET /api/arbitros` | Público | Sin cuerpo. | `200 ArbitroResponse[]`, solo activos, ordenados por `apellido` y `nombre`. | Errores HTTP generales. Las escrituras no existen: Spring Security las rechaza con `401`/`403`. |

`CanchaResponse`: `id`, `nombre`, `direccion` (puede ser `null`), `activo`. `ArbitroResponse`: `id`, `nombre`, `apellido`, `dni` (cadena), `activo`. Son datos precargados por `data.sql`, **sin endpoints de escritura**. El filtro de activos se resuelve en la consulta a la base. Implementado en el paquete `isw2_G5.liga_amateur.catalogo`; las entidades `Cancha` y `Arbitro` son las que usará la programación de partidos.

```http
GET /api/canchas
```

```json
[{"id":1,"nombre":"Cancha Municipal Norte","direccion":"Av. Blas Parera 7400","activo":true},{"id":3,"nombre":"Complejo El Alero","direccion":"Pje. Los Ceibos 1350","activo":true},{"id":2,"nombre":"Predio La Costanera","direccion":"Av. Almirante Brown 5200","activo":true}]
```

### 6.2 Programación (RF-06: implementado), estados y listado (pendientes)

Estado de implementación: `POST`, `PUT` y `DELETE /api/partidos` están implementados en `isw2_G5.liga_amateur.partido`; `PATCH /api/partidos/{id}/estado` (RF-07) y el listado por jornada (RF-10) siguen pendientes.

| Método y ruta | Acceso | Entrada | Respuesta exitosa | Errores relevantes |
|---|---|---|---|---|
| `POST /api/partidos` | ADMIN | `PartidoWriteRequest`: `torneoId`, `numeroFecha`, `equipoLocalId`, `equipoVisitanteId`, `canchaId`, `arbitroId`, `fechaHoraInicio`. No recibe estado ni marcador. | `201 PartidoResponse`, nace `PROGRAMADO`. | `400` formato/rango; `401`/`403`; `404` FK inexistente; `409` equipos/torneo/actividad, equipo duplicado en fecha o conflictos RN-01/02. |
| `PUT /api/partidos/{id}` | ADMIN | `PartidoWriteRequest` **completo**; solo en `PROGRAMADO`. | `200 PartidoResponse`. | `400`, `401`, `403`, `404`, `409` estado/reglas/conflictos. |
| `DELETE /api/partidos/{id}` | ADMIN | Sin cuerpo; borrado físico **solo** si `PROGRAMADO`. | `204` sin cuerpo. | `400`, `401`, `403`, `404`, `409` estado incorrecto. |
| `PATCH /api/partidos/{id}/estado` | DELEGADO o ADMIN | `PartidoEstadoRequest`: `estado` obligatorio. | `200 PartidoResponse` con el nuevo estado. | `400` estado inválido; `401`/`403`/`404`; `409` transición prohibida o RN-07. |
| `GET /api/torneos/{torneoId}/partidos?numeroFecha=N` | Público | `torneoId` positivo y `numeroFecha` obligatorio, desde 1. | `200 PartidoListadoResponse[]` con resultado derivado. | `400` filtro inválido/faltante; `404` torneo inexistente. |

`PartidoResponse` contiene `id`, `torneoId`, `numeroFecha`, `equipoLocalId`, `equipoVisitanteId`, `canchaId`, `arbitroId`, `fechaHoraInicio`, `estado`. `PartidoListadoResponse` agrega a esos campos `resultado: {golesLocal, golesVisitante}`; la misma estructura `resultado` se usa en el detalle de Mateo. IDs existentes y positivos; jornada ≥1; equipos distintos, del mismo torneo y no desactivados para partido nuevo; cancha y árbitro activos; un equipo no juega dos veces en una misma jornada. Cancha y árbitro no pueden estar asignados a otro partido **no suspendido** con inicios separados por menos de dos horas (RN-01/02). Ver sección 1 para comparar instantes con offset.

Decisiones de implementación de RF-06:

- Orden de validación: primero el cuerpo (`400`), después las referencias inexistentes (`404`) y por último las reglas de negocio (`409`), cada una con un mensaje propio en `message`.
- Un partido `SUSPENDIDO` no ocupa cancha, árbitro ni jornada: como no se reprograma, se puede crear su reemplazo en la misma jornada.
- Equipos, cancha y árbitro dados de baja no pueden **asignarse**. Al reprogramar se permite conservar una asignación que el partido ya tenía, aunque se haya dado de baja después; qué ocurre con esos partidos es la pregunta abierta P-06.
- `fechaHoraInicio` se responde siempre con segundos y el offset de la liga (`2026-11-07T18:00:00-03:00`), aunque se haya enviado con otro offset (`2026-11-07T21:00:00Z` se guarda y responde como 18:00 de Santa Fe). Un valor sin offset es `400`.

Transiciones exclusivas: `PROGRAMADO` → `EN_JUEGO` o `SUSPENDIDO`; `EN_JUEGO` → `FINALIZADO` o `SUSPENDIDO`. `FINALIZADO` y `SUSPENDIDO` son finales. Al **iniciar** un partido de la jornada N, RN-07 impide el cambio si algún partido de N−1 sigue `PROGRAMADO` o `EN_JUEGO`; se permite programar fechas futuras con anticipación. Un suspendido no se reprograma: se crea otro partido conforme a RF-06.

Ejemplos (RF-06, RF-07 y listado RF-10):

```http
POST /api/partidos
Authorization: Bearer <token>
Content-Type: application/json

{"torneoId":1,"numeroFecha":1,"equipoLocalId":7,"equipoVisitanteId":8,"canchaId":1,"arbitroId":1,"fechaHoraInicio":"2026-10-28T18:00:00-03:00"}
```

```json
{"id":20,"torneoId":1,"numeroFecha":1,"equipoLocalId":7,"equipoVisitanteId":8,"canchaId":1,"arbitroId":1,"fechaHoraInicio":"2026-10-28T18:00:00-03:00","estado":"PROGRAMADO"}
```

`PUT /api/partidos/20` requiere nuevamente el cuerpo completo anterior. `PATCH /api/partidos/20/estado` recibe `{"estado":"EN_JUEGO"}` y responde el mismo `PartidoResponse` con `estado` actualizado. La lectura pública por jornada usa el cálculo de Mateo:

```http
GET /api/torneos/1/partidos?numeroFecha=1
```

```json
[{"id":20,"torneoId":1,"numeroFecha":1,"equipoLocalId":7,"equipoVisitanteId":8,"canchaId":1,"arbitroId":1,"fechaHoraInicio":"2026-10-28T18:00:00-03:00","estado":"EN_JUEGO","resultado":{"golesLocal":0,"golesVisitante":0}}]
```

## 7. Mateo (D): eventos, detalle, posiciones y suspendidos — RF-08 a RF-12

| Método y ruta | Acceso | Entrada | Respuesta exitosa | Errores relevantes |
|---|---|---|---|---|
| `POST /api/partidos/{partidoId}/eventos` | DELEGADO o ADMIN | `EventoCreateRequest`: `jugadorId`, `tipo`, `minuto`. | `201 EventoResponse`. | `400` tipo/minuto; `401`/`403`; `404` partido/jugador inexistente; `409` estado, jugador ajeno/inactivo o suspendido. |
| `DELETE /api/partidos/{partidoId}/eventos/{eventoId}` | DELEGADO o ADMIN | Sin cuerpo; solo en `EN_JUEGO` y evento de ese partido. | `204` sin cuerpo. | `400`, `401`, `403`, `404` IDs/evento ajeno, `409` estado incorrecto. |
| `GET /api/partidos/{id}` | Público | ID de partido positivo. | `200 PartidoDetalleResponse` con resultado y eventos. | `400` ID inválido; `404` inexistente. |
| `GET /api/torneos/{torneoId}/tabla` | Público | ID de torneo positivo. | `200 PosicionResponse[]` ordenado según RF-11. | `400`, `404` torneo inexistente. |
| `GET /api/torneos/{torneoId}/suspendidos?numeroFecha=N` | Público | Torneo positivo; jornada obligatoria desde 1. | `200 SuspendidoResponse[]`. | `400` filtro inválido/faltante; `404` torneo inexistente. |

`EventoResponse`: `id`, `partidoId`, `jugadorId`, `tipo`, `minuto`; `tipo` solo `GOL`, `AMARILLA` o `ROJA`; minuto entero 1–120. Solo se agrega/borra mientras el partido está `EN_JUEGO`. El jugador debe estar activo, ser del equipo local o visitante y no estar suspendido en la jornada del partido (RN-03/04). `PartidoDetalleResponse` contiene los mismos campos de `PartidoResponse` de José, más `resultado: {golesLocal, golesVisitante}` y `eventos: EventoResponse[]`. No cargar resultado manualmente; sin goles es 0–0.

`PosicionResponse`: `equipoId`, `equipo` (nombre), `pj`, `pg`, `pe`, `pp`, `gf`, `gc`, `dg`, `pts`. Contar solo partidos `FINALIZADO`, victoria 3 puntos, empate 1, derrota 0; incluir equipos sin partidos con valores cero; ordenar por más PTS, mayor DG, más GF y nombre alfabético (`SPEC.md` §7). `SuspendidoResponse`: `jugadorId`, `equipoId`, `numeroFecha` (jornada consultada), `motivo` (`ROJA` o `DOBLE_AMARILLA`) y `numeroFechaSancion` (jornada N−1 que produjo la sanción, para satisfacer la referencia a «fecha de sanción» de RF-12). `numeroFechaSancion` es una **jornada**, no un instante calendario: el esquema no registra el instante de cada tarjeta. Si la interpretación solicitada por el cliente fuera una fecha calendario, consultarlo antes de inventar un timestamp de sanción.

La suspensión solo proviene de partidos `FINALIZADO` de N−1 por una roja o dos amarillas **en el mismo partido**; no se acumulan amarillas entre partidos ni se crea tabla de suspensiones. Si no existe una fecha posterior programada, no se traslada una sanción a otra competición.

Ejemplos (RF-08 a RF-12):

```http
POST /api/partidos/20/eventos
Authorization: Bearer <token>
Content-Type: application/json

{"jugadorId":15,"tipo":"GOL","minuto":35}
```

```json
{"id":30,"partidoId":20,"jugadorId":15,"tipo":"GOL","minuto":35}
```

`DELETE /api/partidos/20/eventos/30` responde `204` mientras esté en juego. El detalle público comparte el marcador con el listado:

```http
GET /api/partidos/20
```

```json
{"id":20,"torneoId":1,"numeroFecha":1,"equipoLocalId":7,"equipoVisitanteId":8,"canchaId":1,"arbitroId":1,"fechaHoraInicio":"2026-10-28T18:00:00-03:00","estado":"EN_JUEGO","resultado":{"golesLocal":1,"golesVisitante":0},"eventos":[{"id":30,"partidoId":20,"jugadorId":15,"tipo":"GOL","minuto":35}]}
```

```http
GET /api/torneos/1/tabla
```

```json
[{"equipoId":7,"equipo":"Barrio Norte","pj":1,"pg":1,"pe":0,"pp":0,"gf":1,"gc":0,"dg":1,"pts":3}]
```

El ejemplo de tabla supone un partido **FINALIZADO**; un partido `EN_JUEGO` como el del ejemplo de detalle aún no suma puntos. El ejemplo de tabla solo ilustra la forma de una fila, no pretende representar todas las filas del torneo.

```http
GET /api/torneos/1/suspendidos?numeroFecha=2
```

```json
[{"jugadorId":15,"equipoId":7,"numeroFecha":2,"motivo":"ROJA","numeroFechaSancion":1}]
```

## 8. Interfaz interna compartida y dependencias

**Mateo es el único responsable del cálculo de marcador** (RN-06): contrato del servicio interno `MarcadorService.calcular(Integer partidoId) → Marcador(int golesLocal, int golesVisitante)`. Cuenta eventos `GOL` y asigna cada uno al equipo local o visitante por el equipo del jugador, sin goles en contra; sin goles devuelve `0,0`. La firma describe la interfaz a compartir entre C y D, **no un endpoint ni una clase existente**. José la usa para `PartidoListadoResponse`; Mateo la usa para `PartidoDetalleResponse` y la tabla, sin duplicar lógica. José y Mateo coordinan el paquete y la disponibilidad del servicio antes de integrar sus PRs.

1. **Franco → todos:** JWT/roles y cuerpos 401/403 deben permitir las consultas públicas sin token y proteger escrituras según sección 2.
2. **Marcos → José y Mateo:** IDs, actividad y torneo de equipos/jugadores. **José** provee las entidades y consultas de solo lectura de canchas y árbitros. Los DTOs no reemplazan las validaciones en backend.
3. **José → Mateo:** modelo/estado de partidos y la consulta por jornada; RN-07 se valida al iniciar, no al programar.
4. **Mateo → José:** cálculo único de marcador y objeto `resultado` idéntico en listado y detalle; disciplina alimenta la validación de eventos.
5. **Expo → backend:** cliente con URL base configurable, rutas y JSON de este documento; los errores de seguridad aún requieren implementación. No asumir que `localhost` en un teléfono apunta al servidor.

## 9. Orden de implementación recomendado

1. Franco integra la seguridad con consultas públicas y 401/403 uniformes; Marcos prepara Expo y el catálogo en paralelo, cada uno en sus ramas de tarea.
2. Marcos publica los contratos/DTOs de equipos y jugadores; José publica canchas y árbitros e implementa programación y estados a partir de ellos. Mateo prepara pruebas de RN-03/RN-06 y el contrato interno de marcador.
3. Mateo implementa eventos, marcador, detalle, tabla y suspendidos; José conecta el listado con su servicio, sin segundo cálculo.
4. Cada integrante integra sus pantallas y pruebas de backend/Expo; documenta en este archivo cualquier ajuste **acordado** antes de cambiar el contrato de otro módulo. Los PRs de frontend y backend se revisan por separado.

Los módulos todavía no están implementados por el hecho de figurar aquí. No agregar pagos, alineaciones, cambios, penales, recuperación de contraseña ni CRUD de torneos/canchas/árbitros.
