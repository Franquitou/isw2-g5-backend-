# Liga Amateur Santafesina — backend

API del proyecto universitario Liga Amateur Santafesina. El backend incluye la base compartida de Spring Boot, el formato común de errores, la configuración de seguridad y RF-01 (login/JWT). Esta rama agrega la gestión de usuarios RF-02; todavía no está integrada en `main`.

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
├── catalogo/              # canchas y árbitros (José)
│   ├── controller/CatalogoController.java
│   ├── dto/CanchaResponse.java, ArbitroResponse.java
│   ├── entity/Cancha.java, Arbitro.java
│   ├── repository/CanchaRepository.java, ArbitroRepository.java
│   └── service/CatalogoService.java
├── partido/               # partidos (José)
│   ├── controller/PartidoController.java
│   ├── dto/PartidoWriteRequest.java, PartidoEstadoRequest.java, PartidoResponse.java
│   ├── entity/Partido.java, EstadoPartido.java
│   ├── repository/PartidoRepository.java
│   └── service/PartidoService.java, HorarioPartido.java
└── common/
    ├── dto/ApiErrorResponse.java
    └── error/ApiExceptionHandler.java
```

Cada módulo futuro deberá agrupar sus controladores, servicios, repositorios, entidades y DTOs bajo el paquete raíz, coordinando primero los contratos entre quienes desarrollen seguridad/usuarios, equipos/jugadores, partidos y eventos/resultados/suspensiones. No se crean paquetes o clases vacías de esos módulos. Validar siempre las reglas de negocio en el backend; exponer DTOs, nunca entidades JPA, contraseñas ni hashes. Mantener la documentación de API actualizada **a medida que existan endpoints**.

### Contrato compartido para errores

Los errores HTTP manejados por Spring MVC y los fallos inesperados usan un objeto JSON con `status` (código HTTP), `message` (mensaje seguro), `timestamp` (instante UTC ISO-8601), `path` (ruta solicitada, si se conoce) y `validationErrors` (lista, vacía salvo errores de validación; cada entrada contiene `field` y `message`). No se incluyen trazas ni valores rechazados en el cuerpo. Una validación fallida devuelve 400; un método no admitido, 405; un recurso inexistente, 404; un fallo inesperado, 500 sin sus detalles internos. Los módulos que se agreguen deben conservar este contrato y usar los estados adecuados a sus operaciones. RF-02 añade 409 para conflictos; las rutas se documentan en `docs/API.md`.

Las pruebas aisladas del manejador de errores usan MockMvc sin iniciar el contexto completo ni H2:

```powershell
.\mvnw.cmd -Dtest=ApiExceptionHandlerTest test
```

Las pruebas de canchas y árbitros tampoco tocan `./data/liga_amateur`: las de controller y service usan dobles de prueba, y `CatalogoRepositoryTest` usa `@DataJpaTest`, que ejecuta `schema.sql` y `data.sql` sobre una H2 en memoria y valida las entidades contra el esquema (`ddl-auto=validate`):

```powershell
.\mvnw.cmd -Dtest="Catalogo*Test" test
```

Las pruebas de partidos usan el mismo criterio: `EstadoPartidoTest` y `HorarioPartidoTest` no inician Spring, y `ProgramacionPartidosApiTest`, `ProgramacionPartidosConcurrenciaTest` y `EstadoPartidosApiTest` levantan el contexto sobre una H2 **en memoria** (`jdbc:h2:mem:...`) con `schema.sql` y `data.sql`, sin tocar `./data/liga_amateur`:

```powershell
.\mvnw.cmd '-Dtest=EstadoPartidoTest,HorarioPartidoTest,ProgramacionPartidos*Test,EstadoPartidosApiTest' test
```

El comando sin filtro `test` incluiría `contextLoads()` y puede reinicializar H2: **no ejecutarlo sin autorización y sin prever la pérdida de datos**. Documentar los comandos y resultados efectivamente ejecutados en cada PR.

## Documentación

- **`docs/API.md`**: convenciones HTTP, formato de error implementado, permisos y estado de los endpoints. Actualizarlo en el mismo PR que agrega o modifica cada endpoint.
- **`docs/PREGUNTAS_PENDIENTES.md`**: preguntas para el cliente y decisiones técnicas que el grupo todavía debe acordar. No resolver por cuenta propia un tema marcado como abierto.
- **`README.md`**: este archivo; mantenerlo actualizado con prerequisitos, configuración, ejecución y pruebas.
- **`SPEC.md`**: especificación funcional y fuente de verdad de los requerimientos (se entrega junto con el proyecto; no se modifica sin confirmación del cliente).

### Autenticación RF-01 sobre seguridad base

`POST /api/auth/login` es público. Comprueba la cuenta activa y el hash BCrypt; responde `token`, `nombreCompleto` y `rol`, sin contraseña ni hash. El JWT HS256 vence a las ocho horas. El filtro Bearer verifica firma y vencimiento, y consulta de nuevo que el usuario siga activo y conserve su rol antes de habilitar rutas protegidas. La cadena permite consultas públicas sin token, controla ADMIN/DELEGADO según `docs/API.md`, deniega rutas no previstas y devuelve 401/403 con `ApiErrorResponse`. Usa sesiones sin estado y no habilita formulario web ni HTTP Basic. La contraseña de desarrollo que Spring pueda mostrar en sus logs **no** es la del administrador precargado ni se utiliza para ingresar a la API.

Para configurar la clave localmente, establecer `LIGA_JWT_SECRET` con **al menos 32 bytes UTF-8** de valor impredecible. Ejemplo de generación para la sesión actual de PowerShell, sin guardar la clave en el repositorio:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:LIGA_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

No copiar la clave a `application.properties`, capturas, commits ni variables públicas de Expo. Sin la variable, el backend no puede crear el servicio JWT. **No iniciar la aplicación sin revisar antes `spring.sql.init.mode`**: `always` recrea la H2 en archivo.

### Inicio rápido con `run-local.ps1`

En la raíz del repositorio hay un script que automatiza el arranque local:

```powershell
.\run-local.ps1
```

El script genera `LIGA_JWT_SECRET` y la guarda como variable de usuario de Windows la primera vez, reutiliza la clave existente en los arranques siguientes, avisa si el puerto 8080 ya está ocupado y ejecuta `spring-boot:run` desde `backend/`. La clave no se imprime en pantalla ni se escribe en el repositorio.

Si PowerShell bloquea la ejecución de scripts:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-local.ps1
```

El script no modifica `spring.sql.init.mode`: el reinicio de la base H2 en cada arranque sigue el comportamiento documentado en esta sección.

### Datos de demostración

`seed-demo.ps1` carga cuatro equipos con cinco jugadores cada uno usando la API real y la cuenta ADMIN, así que respeta las validaciones del backend. Es idempotente sobre los registros **activos**: reutiliza los equipos por nombre y omite los jugadores cuyo DNI ya exista en el plantel.

La contraseña del ADMIN no está escrita en el script. Se resuelve, en este orden, desde el parámetro `-Password`, la variable de entorno `LIGA_ADMIN_PASSWORD` o una solicitud interactiva que no muestra lo que se escribe:

```powershell
$env:LIGA_ADMIN_PASSWORD = 'tu-contrasena'
.\seed-demo.ps1
```

Si el backend corre en otra máquina o puerto:

```powershell
$env:LIGA_ADMIN_PASSWORD = 'tu-contrasena'
.\seed-demo.ps1 -ApiUrl http://192.168.0.253:8080/api
```

Un `409` es un conflicto de unicidad, no necesariamente una baja lógica: el nombre de equipo puede estar en uso en el torneo o el DNI puede pertenecer a un jugador activo de otro equipo. Por eso el script usa mensajes neutrales: informa el conflicto, omite ese registro y continúa con los demás. Si se omite un equipo, sus jugadores no se procesan y se informan aparte en el resumen como **jugadores sin procesar (equipo omitido)**, para que el total no parezca incompleto.

Conviene leer el resumen final: las líneas `omitidos (409)` y `sin procesar` indican los conflictos que el script no pudo resolver por API.

Como `spring.sql.init.mode=always` reinicia la base en cada arranque, el script debe ejecutarse después de iniciar el backend.

Las pruebas MVC de seguridad/login usan `spring.sql.init.mode=never` y una URL H2 **en memoria** como salvaguarda; esa porción MVC no registra `DataSource` y simula el repositorio. Verifican el recorrido HTTP login → Bearer → ruta protegida, pero **no sustituyen una prueba posterior con persistencia real**. Desde el proyecto Maven:

```powershell
.\mvnw.cmd '-Dtest=AuthFlowTest,SecurityConfigurationTest,JwtServiceTest,JwtAuthenticationFilterTest,AuthServiceTest,AuthControllerTest' test
```

No ejecutar `test` sin filtro mientras el contexto completo pueda reinicializar la H2 en archivo. Las dependencias de seguridad ya fueron aportadas por el PR #3: este incremento no modifica `pom.xml`.

### Gestión de usuarios RF-02 (esta rama)

Solo ADMIN puede consultar, crear, actualizar, cambiar contraseña y desactivar/reactivar usuarios en `/api/usuarios`. Por defecto el listado muestra activos; `?activo=false` muestra inactivos para su reactivación. El username permanece inmutable; el backend aplica BCrypt a contraseñas nuevas y prohíbe al administrador quitarse ADMIN o desactivarse (RN-08). La baja es lógica (RN-09), sin DELETE físico. Los cuerpos, permisos y errores están en `docs/API.md`.

Estas pruebas no usan la H2 persistente: los tests de DTO/servicio/errores son aislados; `UsuarioControllerTest` usa un contexto MVC sin `DataSource` con `spring.sql.init.mode=never`; `UsuarioRepositoryTest` reemplaza el datasource por H2 **en memoria** y valida el SQL original sin tocar `./data/liga_amateur`:

```powershell
.\mvnw.cmd '-Dtest=UsuarioDtoValidationTest,UsuarioServiceTest,UsuarioControllerTest,UsuarioRepositoryTest,ApiExceptionHandlerTest' test
```

## Trabajo en equipo

Cada funcionalidad se implementa en su propia rama creada desde `main` actualizado; no desarrollar ni hacer push directo sobre `main`. Mantener commits pequeños y descriptivos; revisar `git diff`, `git status`, build, pruebas y documentación antes de abrir un Pull Request hacia `main`. Cada PR debe ser aprobado por **otro integrante** antes de integrarse. Los cambios de frontend llevan rama y PR propios en el repositorio de la app; acordar antes los DTOs y el contrato de API entre ambos repositorios. No hacer push ni merge sin autorización del coordinador.
