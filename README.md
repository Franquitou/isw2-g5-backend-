# Liga Amateur Santafesina — backend

API del proyecto universitario Liga Amateur Santafesina. Esta rama prepara únicamente la base compartida: arranque Spring Boot, scripts SQL originales y formato común de errores. Todavía no hay endpoints funcionales, autenticación ni módulos de negocio.

## Tecnologías y requisitos

- Java 17 (JDK) y conexión para descargar dependencias Maven la primera vez.
- Spring Boot 3.5.9, Maven Wrapper incluido, Spring Web, Spring Data JPA, Bean Validation y H2 en archivo.
- Git para trabajar en ramas independientes. El frontend React Native/Expo vive en otro repositorio.

El proyecto Maven está en **`backend/`** respecto de la raíz de *este repositorio*. Los comandos siguientes se ejecutan desde la raíz del repositorio backend en PowerShell:

```powershell
cd backend
java -version
.\mvnw.cmd dependency:go-offline
.\mvnw.cmd -DskipTests compile
```

La descarga inicial puede requerir acceso a internet. Para compilar y generar el paquete sin ejecutar pruebas ni arrancar el servidor:

```powershell
.\mvnw.cmd -DskipTests package
```

Para iniciar **intencionalmente** el servidor desde esa misma carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

No iniciar el servidor ni ejecutar pruebas que carguen el contexto Spring Boot antes de leer la advertencia de H2 siguiente. La ruta relativa de la base depende del directorio de ejecución: al usar estos comandos, `./data/liga_amateur` corresponde a `backend/backend/data/` desde la carpeta principal del proyecto.

## Datos H2: comportamiento intencional

`backend/src/main/resources/application.properties` define `jdbc:h2:file:./data/liga_amateur`, `spring.jpa.hibernate.ddl-auto=none` y **`spring.sql.init.mode=always`**. En cada inicio con `always`, `schema.sql` ejecuta `DROP TABLE` y vuelve a crear las ocho tablas; `data.sql` vuelve a cargar el administrador, torneo, canchas y árbitros. **Se pierden los equipos, jugadores, partidos, eventos y demás cambios cargados previamente.** Esto es intencional durante el desarrollo y las pruebas; no se modificaron los scripts ni el valor configurado.

Para conservar datos en arranques posteriores, el procedimiento acordado es:

1. Realizar **una primera ejecución deliberada** con `spring.sql.init.mode=always`, que crea el esquema y carga los datos iniciales.
2. Antes del siguiente arranque, cambiar manualmente en `application.properties` a `spring.sql.init.mode=never`. Con `never` no se vuelven a ejecutar los scripts y se conserva el archivo H2 existente.
3. Si se necesita reiniciar el esquema intencionalmente, volver a `always` sabiendo que se borran los datos. No usar `never` sobre una base nueva sin tablas.

No incluir archivos de `data/` en commits. El test preexistente `LigaAmateurApplicationTests.contextLoads()` **arranca el contexto** y puede ejecutar los scripts con `always`; consultarlo antes de correr el conjunto completo de pruebas.

## Organización y convenciones

El paquete Java raíz existente es `isw2_G5.liga_amateur` bajo `backend/src/main/java/`. Por ahora contiene:

```text
isw2_G5.liga_amateur/
├── LigaAmateurApplication.java
└── common/
    ├── dto/ApiErrorResponse.java
    └── error/ApiExceptionHandler.java
```

Cada módulo futuro deberá agrupar sus controladores, servicios, repositorios, entidades y DTOs bajo el paquete raíz, coordinando primero los contratos entre quienes desarrollen seguridad/usuarios, equipos/jugadores, partidos y eventos/resultados/suspensiones. No se crean paquetes o clases vacías de esos módulos. Validar siempre las reglas de negocio en el backend; exponer DTOs, nunca entidades JPA, contraseñas ni hashes. Mantener la documentación de API actualizada **a medida que existan endpoints**.

### Contrato compartido para errores

Los errores HTTP manejados por Spring MVC y los fallos inesperados usan un objeto JSON con `status` (código HTTP), `message` (mensaje seguro), `timestamp` (instante UTC ISO-8601), `path` (ruta solicitada, si se conoce) y `validationErrors` (lista, vacía salvo errores de validación; cada entrada contiene `field` y `message`). No se incluyen trazas ni valores rechazados en el cuerpo. Una validación fallida devuelve 400; un método no admitido, 405; un recurso inexistente, 404; un fallo inesperado, 500 sin sus detalles internos. Los módulos que se agreguen deben conservar este contrato y usar los estados adecuados a sus operaciones. No hay rutas de negocio implementadas todavía.

Las pruebas aisladas del manejador de errores usan MockMvc sin iniciar el contexto completo ni H2:

```powershell
.\mvnw.cmd -Dtest=ApiExceptionHandlerTest test
```

El comando sin filtro `test` incluiría `contextLoads()` y puede reinicializar H2: **no ejecutarlo sin autorización y sin prever la pérdida de datos**. Documentar los comandos y resultados efectivamente ejecutados en cada PR.

## Documentación

- **`docs/API.md`**: convenciones HTTP, formato de error implementado, permisos y estado de los endpoints. Actualizarlo en el mismo PR que agrega o modifica cada endpoint.
- **`docs/PREGUNTAS_PENDIENTES.md`**: preguntas para el cliente y decisiones técnicas que el grupo todavía debe acordar. No resolver por cuenta propia un tema marcado como abierto.
- **`README.md`**: este archivo; mantenerlo actualizado con prerequisitos, configuración, ejecución y pruebas.
- **`SPEC.md`**: especificación funcional y fuente de verdad de los requerimientos (se entrega junto con el proyecto; no se modifica sin confirmación del cliente).

## Trabajo en equipo

Cada funcionalidad se implementa en su propia rama creada desde `main` actualizado; no desarrollar ni hacer push directo sobre `main`. Mantener commits pequeños y descriptivos; revisar `git diff`, `git status`, build, pruebas y documentación antes de abrir un Pull Request hacia `main`. Cada PR debe ser aprobado por **otro integrante** antes de integrarse. Los cambios de frontend llevan rama y PR propios en el repositorio de la app; acordar antes los DTOs y el contrato de API entre ambos repositorios. No hacer push ni merge sin autorización del coordinador.
