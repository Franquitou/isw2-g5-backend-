<#
.SYNOPSIS
    Carga equipos y jugadores de demostracion en el backend de la liga.

.DESCRIPTION
    Usa la API real con la cuenta ADMIN, asi que respeta todas las validaciones
    del backend (nombres unicos, DNI unico, camiseta 1-99, baja logica).

    El script es idempotente: si un equipo ya existe en el torneo lo reutiliza y
    si un DNI ya esta cargado lo omite. Se puede ejecutar varias veces sin
    duplicar datos.

    Recordatorio: con spring.sql.init.mode=always la base se reinicia en cada
    arranque del backend. Volve a ejecutar este script despues de iniciarlo.

.PARAMETER ApiUrl
    URL base de la API, con el prefijo /api. Por defecto http://localhost:8080/api

.PARAMETER Username
    Usuario ADMIN. Por defecto admin

.PARAMETER Password
    Contrasena del ADMIN. Por defecto Admin2026!

.PARAMETER TorneoId
    Torneo donde se cargan los equipos. Por defecto 1

.EXAMPLE
    .\seed-demo.ps1

.EXAMPLE
    .\seed-demo.ps1 -ApiUrl http://192.168.0.253:8080/api

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\seed-demo.ps1
#>
[CmdletBinding()]
param(
    [string]$ApiUrl = 'http://localhost:8080/api',
    [string]$Username = 'admin',
    [string]$Password = 'Admin2026!',
    [int]$TorneoId = 1
)

$ErrorActionPreference = 'Stop'
$ApiUrl = $ApiUrl.TrimEnd('/')

function Write-Ok {
    param([string]$Texto)
    Write-Host $Texto -ForegroundColor Green
}

function Write-Aviso {
    param([string]$Texto)
    Write-Host $Texto -ForegroundColor Yellow
}

function Invoke-LigaApi {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Path,
        [object]$Body,
        [hashtable]$Headers = @{}
    )

    $request = @{
        Method      = $Method
        Uri         = "$ApiUrl$Path"
        Headers     = $Headers
        ContentType = 'application/json; charset=utf-8'
        TimeoutSec  = 20
    }
    if ($null -ne $Body) {
        # Se envian bytes UTF-8 para que los acentos no dependan de la consola.
        $request.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 5))
    }
    Invoke-RestMethod @request
}

function Get-LigaLista {
    param(
        [Parameter(Mandatory)][string]$Path,
        [hashtable]$Headers = @{}
    )
    # Invoke-RestMethod puede devolver $null o un Object[] vacio para un arreglo
    # JSON sin elementos. Se normaliza a un arreglo real antes de iterar.
    $respuesta = Invoke-LigaApi -Method Get -Path $Path -Headers $Headers
    if ($null -eq $respuesta) { return @() }
    return @($respuesta)
}

$catalogo = @(
    @{
        nombre    = 'Barrio Norte FC'
        barrio    = 'Norte'
        jugadores = @(
            @{ nombre = 'Lucas'; apellido = 'Pereyra'; dni = '40111222'; numeroCamiseta = 1 }
            @{ nombre = 'Mateo'; apellido = 'Acosta'; dni = '40222333'; numeroCamiseta = 4 }
            @{ nombre = 'Santiago'; apellido = 'Benitez'; dni = '40333444'; numeroCamiseta = 7 }
            @{ nombre = 'Nicolas'; apellido = 'Medina'; dni = '40444555'; numeroCamiseta = 9 }
            @{ nombre = 'Tomas'; apellido = 'Rios'; dni = '40555666'; numeroCamiseta = 10 }
        )
    },
    @{
        nombre    = 'Los Piratas del Sur'
        barrio    = 'Sur'
        jugadores = @(
            @{ nombre = 'Franco'; apellido = 'Sosa'; dni = '40666777'; numeroCamiseta = 2 }
            @{ nombre = 'Agustin'; apellido = 'Vera'; dni = '40777888'; numeroCamiseta = 5 }
            @{ nombre = 'Joaquin'; apellido = 'Luna'; dni = '40888999'; numeroCamiseta = 8 }
            @{ nombre = 'Bruno'; apellido = 'Castro'; dni = '40999000'; numeroCamiseta = 9 }
            @{ nombre = 'Emiliano'; apellido = 'Paz'; dni = '41000111'; numeroCamiseta = 11 }
        )
    },
    @{
        nombre    = 'Atletico Candioti'
        barrio    = 'Candioti'
        jugadores = @(
            @{ nombre = 'Damian'; apellido = 'Aguirre'; dni = '41111222'; numeroCamiseta = 1 }
            @{ nombre = 'Rodrigo'; apellido = 'Chavez'; dni = '41222333'; numeroCamiseta = 3 }
            @{ nombre = 'Facundo'; apellido = 'Herrera'; dni = '41333444'; numeroCamiseta = 6 }
            @{ nombre = 'Leandro'; apellido = 'Molina'; dni = '41444555'; numeroCamiseta = 9 }
            @{ nombre = 'Ivan'; apellido = 'Duarte'; dni = '41555666'; numeroCamiseta = 10 }
        )
    },
    @{
        nombre    = 'Union Alto Verde'
        barrio    = 'Alto Verde'
        jugadores = @(
            @{ nombre = 'Gonzalo'; apellido = 'Ferrari'; dni = '41666777'; numeroCamiseta = 4 }
            @{ nombre = 'Martin'; apellido = 'Cabrera'; dni = '41777888'; numeroCamiseta = 5 }
            @{ nombre = 'Sebastian'; apellido = 'Godoy'; dni = '41888999'; numeroCamiseta = 7 }
            @{ nombre = 'Alejo'; apellido = 'Quiroga'; dni = '41999000'; numeroCamiseta = 9 }
            @{ nombre = 'Nahuel'; apellido = 'Britos'; dni = '42000111'; numeroCamiseta = 11 }
        )
    }
)

Write-Host ''
Write-Host "Cargando datos de demostracion en $ApiUrl (torneo $TorneoId)" -ForegroundColor Cyan
Write-Host ''

try {
    $login = Invoke-LigaApi -Method Post -Path '/auth/login' -Body @{
        username = $Username
        password = $Password
    }
} catch {
    Write-Host "No se pudo iniciar sesion como '$Username'." -ForegroundColor Red
    Write-Host 'Verifica que el backend este iniciado, que la URL sea correcta y que la cuenta exista.' -ForegroundColor Red
    exit 1
}

if ($login.rol -ne 'ADMIN') {
    Write-Host "La cuenta '$Username' no es ADMIN (rol: $($login.rol))." -ForegroundColor Red
    exit 1
}
Write-Ok "Sesion iniciada como $($login.nombreCompleto) ($($login.rol))."
Write-Host ''

try {
    $equiposExistentes = @{}
    Get-LigaLista -Path "/torneos/$TorneoId/equipos" -Headers @{ Authorization = "Bearer $($login.token)" } |
        ForEach-Object { $equiposExistentes[$_.nombre] = $_ }
} catch {
    Write-Host "No se pudo leer el torneo $TorneoId. Verifica que exista." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

$auth = @{ Authorization = "Bearer $($login.token)" }
$equiposCreados = 0
$equiposReutilizados = 0
$jugadoresCreados = 0
$jugadoresExistentes = 0

foreach ($equipo in $catalogo) {
    if ($equiposExistentes.ContainsKey($equipo.nombre)) {
        $equipoId = $equiposExistentes[$equipo.nombre].id
        $equiposReutilizados++
        Write-Host ("  = {0} (id {1})" -f $equipo.nombre, $equipoId)
    } else {
        $creado = Invoke-LigaApi -Method Post -Path "/torneos/$TorneoId/equipos" -Headers $auth -Body @{
            nombre = $equipo.nombre
            barrio = $equipo.barrio
        }
        $equipoId = $creado.id
        $equiposCreados++
        Write-Ok ("  + {0} (id {1})" -f $equipo.nombre, $equipoId)
    }

    $dnis = @{}
    Get-LigaLista -Path "/equipos/$equipoId/jugadores" -Headers $auth |
        ForEach-Object { $dnis[$_.dni] = $true }

    foreach ($jugador in $equipo.jugadores) {
        if ($dnis.ContainsKey($jugador.dni)) {
            $jugadoresExistentes++
            continue
        }
        Invoke-LigaApi -Method Post -Path "/equipos/$equipoId/jugadores" -Headers $auth -Body $jugador | Out-Null
        $jugadoresCreados++
    }
}

Write-Host ''
Write-Host 'Resumen' -ForegroundColor Cyan
Write-Host "  Equipos creados:      $equiposCreados"
Write-Host "  Equipos ya existentes: $equiposReutilizados"
Write-Host "  Jugadores creados:     $jugadoresCreados"
Write-Host "  Jugadores ya existentes: $jugadoresExistentes"
Write-Host ''

$equiposFinales = @(Get-LigaLista -Path "/torneos/$TorneoId/equipos")
$totalJugadores = 0
foreach ($equipo in $equiposFinales) {
    $totalJugadores += @(Get-LigaLista -Path "/equipos/$($equipo.id)/jugadores").Count
}
Write-Ok "Verificacion: $($equiposFinales.Count) equipos activos y $totalJugadores jugadores activos en el torneo $TorneoId."
Write-Host ''
Write-Aviso 'Con spring.sql.init.mode=always estos datos se borran al reiniciar el backend.'
Write-Aviso 'Despues de cada arranque, volve a ejecutar este script.'
