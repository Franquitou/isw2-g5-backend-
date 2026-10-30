# Preguntas y decisiones pendientes

Registro de temas que **no están definidos** en `SPEC.md` o en el esquema. Ninguna persona debe resolverlos por su cuenta: se consulta al cliente o se acuerda en el grupo y se actualiza este archivo antes de implementar.

**Estados:** `Abierta` (sin respuesta), `Acordada` (decisión registrada), `Cerrada` (ya no aplica).

## 1. Preguntas para el cliente

| ID | Pregunta | Contexto / impacto | Responsable | Estado |
|---|---|---|---|---|
| P-01 | Si una fecha tiene partidos `SUSPENDIDO`, ¿esa fecha se considera terminada para habilitar la siguiente? | **Acordado:** un partido `SUSPENDIDO` no bloquea la fecha siguiente. RN-07 se valida al iniciar un partido, no al programarlo. Afecta RF-06 y RF-12. | C / D | Acordada |
| P-02 | ¿Cómo se determina cuál es la **última fecha** del torneo? | **Acordado:** no se define una última fecha. Las suspensiones se calculan dinámicamente desde partidos `FINALIZADO` de N-1; si no se programa una fecha posterior, la sanción no se traslada a otra competición. Afecta RF-12. | D | Acordada |
| P-03 | ¿Se puede programar un partido de una fecha futura mientras una fecha anterior sigue en curso? | **Acordado:** sí, la programación anticipada está permitida. RN-07 se valida al iniciar el partido, no al crearlo. No queda una duda distinta pendiente sobre este punto. | C | Acordada |
| P-04 | ¿El número de camiseta debe ser único dentro de un equipo? | **Acordado:** no agregar unicidad. El número es opcional (`NULL`); si se informa, va de 1 a 99 y se valida también en DTO con `@Min(1)`/`@Max(99)`. | B | Acordada |
| P-05 | ¿Cómo debe ver el administrador los registros inactivos para poder reactivarlos? | **Acordado:** `GET /api/usuarios?activo=true\|false` admite filtro opcional, solo ADMIN; `PATCH /api/usuarios/{id}/estado` permite reactivar usuarios. RF-03/04 solo requieren baja lógica de equipos/jugadores, sin reactivación adicional. | A / B | Acordada |
| P-06 | ¿Qué ocurre con los partidos y eventos históricos de un equipo o jugador que se desactiva? | RN-09 exige baja lógica, pero no define si un equipo desactivado conserva sus partidos programados ni si esos partidos deben cancelarse. | B / C | Abierta |
| P-07 | ¿Qué zona horaria debe usarse para `fecha_hora_inicio`? | **Acordado:** `America/Argentina/Buenos_Aires` para el `TIMESTAMP` sin zona; API ISO-8601 con offset. Convertir antes de comparar los instantes de RN-01/RN-02 (ver `docs/API.md` §1). | C | Acordada |

## 2. Decisiones técnicas a acordar en el grupo

Estas no dependen del cliente, pero deben quedar cerradas antes del módulo correspondiente.

| ID | Tema | Propuesta / nota | Responsable | Estado |
|---|---|---|---|---|
| T-01 | Prefijo y versionado de rutas | **Acordado:** prefijo `/api`, recursos plurales. Sin versionado adicional obligatorio. | Todos | Acordada |
| T-02 | Secreto y algoritmo del JWT | **Acordado:** HS256, secreto seguro por variable de entorno nunca versionado, validez de 8 horas (RNF-05). La configuración aún no está implementada. | A | Acordada |
| T-03 | Contrato 401/403 con Spring Security | **Acordado e integrado en RF-01:** los puntos de entrada de autenticación y autorización devuelven `ApiErrorResponse`. | A | Acordada |
| T-04 | Violaciones de integridad y conflictos | **Acordado:** HTTP 409 con `ApiErrorResponse`. RF-02 incorpora conflictos de username y RN-08 en esta rama; cada módulo debe implementar y probar los suyos. | A / cada módulo | Acordada |
| T-05 | Respuesta ante `Content-Type` no admitido | Agregar prueba del caso 415 y verificar el cuerpo de error uniforme (sugerido en la revisión del PR #1). | A | Abierta |
| T-06 | Formato de fechas en la API | **Acordado:** ISO-8601 con offset para fecha/hora del partido; interpretar los `TIMESTAMP` H2 en `America/Argentina/Buenos_Aires`; errores UTC (`Z`). | Todos | Acordada |
| T-07 | Manejo de `spring.sql.init.mode` | **Acordado:** `always` en el primer arranque crea el esquema y carga los datos iniciales; `never` en arranques posteriores conserva los datos (ver README). El perfil de pruebas en memoria queda como T-08. | A | Acordada |
| T-08 | Perfil de pruebas con H2 en memoria | Evaluar un `application-test.properties` con H2 en memoria para que las pruebas no toquen `./data/liga_amateur` ni dependan de `always`/`never`. | A | Abierta |

## 3. Decisiones acordadas

Registro de las decisiones ya tomadas por el grupo; las filas correspondientes de las tablas anteriores quedan en estado `Acordada`.

- **28/10/2026 — P-01 y P-03:** RN-07 se valida al **iniciar** un partido, no al programarlo. Se permite programar fechas futuras y un partido `SUSPENDIDO` no impide avanzar a la fecha siguiente.
- **28/10/2026 — P-02:** no se define una “última fecha” en el modelo. Las suspensiones se calculan dinámicamente desde la fecha N-1; si no existe una fecha N+1, la sanción no se aplica.
- **28/10/2026 — T-07:** el procedimiento de arranque de H2 ya está acordado en el README (`always` la primera vez, `never` después). Solo queda abierto el perfil de pruebas en memoria (T-08).
- **28/10/2026 — P-04/P-05/P-07 y T-01 a T-04/T-06:** Franco y Marcos aprobaron camiseta nullable sin unicidad, consulta/reactivación de usuarios sin reactivación extra de equipos/jugadores, zona `America/Argentina/Buenos_Aires`, rutas `/api`, HS256 con secreto de entorno, formato 401/403 y 409. El contrato está detallado en `docs/API.md`; **su implementación sigue pendiente**.

## 4. Cómo registrar una respuesta

Al obtener una respuesta del cliente o un acuerdo del grupo:

1. Cambiar el estado a `Acordada` o `Cerrada`.
2. Escribir la decisión en la columna correspondiente o en una nota debajo de la tabla.
3. Actualizar `SPEC.md` solo si el cliente modificó el requerimiento; en caso contrario, la decisión vive en este archivo.
4. Si la decisión afecta la API, reflejarla en `docs/API.md`.
