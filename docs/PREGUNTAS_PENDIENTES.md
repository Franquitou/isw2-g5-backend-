# Preguntas y decisiones pendientes

Registro de temas que **no están definidos** en `SPEC.md` o en el esquema. Ninguna persona debe resolverlos por su cuenta: se consulta al cliente o se acuerda en el grupo y se actualiza este archivo antes de implementar.

**Estados:** `Abierta` (sin respuesta), `Acordada` (decisión registrada), `Cerrada` (ya no aplica).

## 1. Preguntas para el cliente

| ID | Pregunta | Contexto / impacto | Responsable | Estado |
|---|---|---|---|---|
| P-01 | Si una fecha tiene partidos `SUSPENDIDO`, ¿esa fecha se considera terminada para habilitar la siguiente? | RN-07 exige que una fecha no empiece hasta que termine la anterior, pero un partido suspendido no se reprograma. Afecta RF-06 y RF-12. | C / D | Abierta |
| P-02 | ¿Cómo se determina cuál es la **última fecha** del torneo? | La tabla `torneo` no guarda cantidad de fechas. RN-03 indica que la suspensión de la última fecha se pierde, pero no define cómo identificarla. Afecta RF-12. | D | Abierta |
| P-03 | ¿Se puede programar un partido de una fecha futura mientras una fecha anterior sigue en curso? | RN-07 se aplica a RF-06; no queda claro si bloquea la **programación** anticipada o solo el **inicio** de la fecha siguiente. | C | Abierta |
| P-04 | ¿El número de camiseta debe ser único dentro de un equipo? | El esquema solo exige el rango 1–99; no declara unicidad por equipo. Afecta RF-04. | B | Abierta |
| P-05 | ¿Cómo debe ver el administrador los registros inactivos para poder reactivarlos? | RF-05 indica que por defecto solo se muestran activos, pero RF-02 a RF-04 exigen desactivar y reactivar usuarios, equipos y jugadores. No está definido el mecanismo (por ejemplo, un parámetro de consulta). | A / B | Abierta |
| P-06 | ¿Qué ocurre con los partidos y eventos históricos de un equipo o jugador que se desactiva? | RN-09 exige baja lógica, pero no define si un equipo desactivado conserva sus partidos programados ni si esos partidos deben cancelarse. | B / C | Abierta |
| P-07 | ¿Qué zona horaria debe usarse para `fecha_hora_inicio`? | La columna es `TIMESTAMP` sin zona. La comparación de RN-01 y RN-02 (separación de 2 horas) depende de una interpretación uniforme. | C | Abierta |

## 2. Decisiones técnicas a acordar en el grupo

Estas no dependen del cliente, pero deben quedar cerradas antes del módulo correspondiente.

| ID | Tema | Propuesta / nota | Responsable | Estado |
|---|---|---|---|---|
| T-01 | Prefijo y versionado de rutas | Definir antes del primer endpoint, por ejemplo `/api` o `/api/v1`, y usarlo en todos los módulos. | Todos | Abierta |
| T-02 | Secreto y algoritmo del JWT | Externalizar el secreto por variable de entorno; no versionarlo. Definir algoritmo y que la expiración sea exactamente de 8 horas (RNF-05). | A | Abierta |
| T-03 | Contrato 401/403 con Spring Security | Configurar `AuthenticationEntryPoint` y `AccessDeniedHandler` para devolver `ApiErrorResponse` en lugar del cuerpo por defecto de Spring. | A | Abierta |
| T-04 | Violaciones de integridad | Incorporar `DataIntegrityViolationException` con HTTP 409 y el contrato común (sugerido en la revisión del PR #1). | A | Abierta |
| T-05 | Respuesta ante `Content-Type` no admitido | Agregar prueba del caso 415 y verificar el cuerpo de error uniforme (sugerido en la revisión del PR #1). | A | Abierta |
| T-06 | Formato de fechas en la API | Usar ISO-8601 en todas las respuestas; definir si se devuelve con offset o en UTC. | Todos | Abierta |
| T-07 | Manejo de `spring.sql.init.mode` | Acordar el procedimiento de arranque para no perder datos (ver README) y si se agrega un perfil de pruebas con H2 en memoria. | A | Abierta |

## 3. Cómo registrar una respuesta

Al obtener una respuesta del cliente o un acuerdo del grupo:

1. Cambiar el estado a `Acordada` o `Cerrada`.
2. Escribir la decisión en la columna correspondiente o en una nota debajo de la tabla.
3. Actualizar `SPEC.md` solo si el cliente modificó el requerimiento; en caso contrario, la decisión vive en este archivo.
4. Si la decisión afecta la API, reflejarla en `docs/API.md`.
