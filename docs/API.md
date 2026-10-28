# API — Liga Amateur Santafesina

Documento vivo del contrato HTTP del backend. **Estado actual: base compartida, sin endpoints de negocio implementados.** Este archivo se actualiza en el mismo PR que agrega o modifica cada endpoint.

## 1. Convenciones generales

| Tema | Convención |
|---|---|
| Formato | JSON UTF-8, `Content-Type: application/json` |
| Fechas y horas | ISO-8601. Los `timestamp` de error se expresan en UTC (`Z`). La zona horaria de `fecha_hora_inicio` está pendiente de confirmar (ver `PREGUNTAS_PENDIENTES.md`). |
| Respuestas | Siempre DTOs; nunca entidades JPA, contraseñas ni hashes. |
| Errores | Contrato común `ApiErrorResponse` (sección 3). |
| Autenticación | JWT `Bearer` con vigencia de 8 horas (pendiente de implementación, RF-01). |
| Prefijo de rutas | Pendiente de acordar por el grupo antes del primer endpoint. |

## 2. Autenticación y permisos

- Los endpoints públicos no requieren token.
- Los endpoints protegidos esperan `Authorization: Bearer <token>`.
- Sin token o con token inválido/expirado: **HTTP 401**.
- Con token válido pero rol insuficiente: **HTTP 403**.
- Roles: `ADMIN` y `DELEGADO`. Un usuario inactivo no puede autenticarse (RF-01, RN-09).

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

## 3. Formato de error (implementado)

Todas las respuestas de error usan este cuerpo:

```json
{
  "status": 400,
  "message": "La solicitud contiene datos inválidos",
  "timestamp": "2026-10-28T12:00:00Z",
  "path": "/ruta/solicitada",
  "validationErrors": [
    { "field": "name", "message": "no debe estar vacío" }
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
| 400 | Cuerpo mal formado o validación fallida. |
| 401 | Falta autenticación o el token no es válido. |
| 403 | El usuario autenticado no tiene el rol requerido. |
| 404 | Recurso inexistente. |
| 405 | Método HTTP no admitido. |
| 409 | Conflicto de datos (pendiente de implementar para violaciones de integridad). |
| 415 | `Content-Type` no admitido. |
| 500 | Error interno; la traza queda en el log del servidor, nunca en la respuesta. |

Los errores inesperados se registran con `log.error` antes de devolver un 500 genérico. Cuando se incorpore Spring Security, sus puntos de entrada de autenticación y autorización deben devolver este mismo contrato JSON.

## 4. Endpoints

**No hay endpoints implementados todavía.** Esta tabla se completa a medida que cada módulo se integra:

| RF | Recurso | Estado |
|---|---|---|
| RF-01 | Inicio de sesión | Pendiente |
| RF-02 | Usuarios | Pendiente |
| RF-03 | Equipos | Pendiente |
| RF-04 | Jugadores | Pendiente |
| RF-05 | Torneo, canchas, árbitros, equipos y jugadores | Pendiente |
| RF-06 | Programación de partidos | Pendiente |
| RF-07 | Estado de partido | Pendiente |
| RF-08 | Registro de eventos | Pendiente |
| RF-09 | Corrección de eventos | Pendiente |
| RF-10 | Consulta de partidos y resultados | Pendiente |
| RF-11 | Tabla de posiciones | Pendiente |
| RF-12 | Suspendidos por fecha | Pendiente |

## 5. Contratos a acordar antes de implementar

Cada integrante debe proponer en su rama, y acordar con el resto, las rutas, cuerpos, respuestas y errores de su módulo antes de programarlos. Como mínimo:

- **A (RF-01, RF-02):** login, datos devueltos junto al token, alta/modificación/baja lógica de usuarios, cambio de contraseña y rol.
- **B (RF-03, RF-04, RF-05):** catálogo público y CRUD de equipos y jugadores, incluida la forma de listar registros inactivos para poder reactivarlos.
- **C (RF-06, RF-07):** alta/edición/baja de partidos y transiciones de estado permitidas.
- **D (RF-08 a RF-12):** eventos, detalle de partido, tabla y suspendidos.

Reglas para todos:

- No inventar campos ni comportamientos que no estén en `SPEC.md`.
- Validar siempre en el backend; las validaciones del cliente son complementarias.
- Documentar en este archivo cada endpoint con método, ruta, permisos, cuerpo, respuesta y errores.
- Mantener el contrato de error de la sección 3.
