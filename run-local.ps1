<#
.SYNOPSIS
    Arranca el backend de Liga Amateur Santafesina con todo configurado en local.

.DESCRIPTION
    Automatiza el inicio del backend para desarrollo:

    * Genera LIGA_JWT_SECRET la primera vez y la guarda como variable de
      usuario de Windows, para no tener que definirla en cada terminal.
    * Reutiliza la clave existente si ya es valida (32 bytes o mas).
    * Verifica que el puerto 8080 este libre antes de arrancar, para no
      chocar con otra instancia que bloquee la base H2.
    * Ejecuta spring-boot:run desde la carpeta backend/.

    La clave se guarda solo en esta maquina. No se imprime en pantalla ni
    se escribe en el repositorio.

.EXAMPLE
    .\run-local.ps1

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\run-local.ps1
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$Puerto = 8080

function Write-Aviso {
    param([string]$Texto)
    Write-Host $Texto -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Texto)
    Write-Host $Texto -ForegroundColor Green
}

$backendDir = Join-Path $PSScriptRoot 'backend'
$pom = Join-Path $backendDir 'pom.xml'

if (-not (Test-Path $pom)) {
    throw "No se encontro backend/pom.xml. Ejecuta el script desde la raiz del repositorio."
}

$tieneJava = $false
if (Get-Command java -ErrorAction SilentlyContinue) {
    $tieneJava = $true
} elseif (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    $tieneJava = $true
}

if (-not $tieneJava) {
    throw "No se encontro Java. Instala un JDK 17 o superior y volve a intentar."
}

$ocupado = $null
try {
    $ocupado = Get-NetTCPConnection -LocalPort $Puerto -State Listen -ErrorAction SilentlyContinue
} catch {
    $ocupado = $null
}

if ($ocupado) {
    $procesos = ($ocupado | Select-Object -ExpandProperty OwningProcess -Unique) -join ', '
    Write-Aviso "El puerto $Puerto ya esta en uso (PID: $procesos)."
    Write-Aviso 'Puede ser otro backend abierto: solo puede correr uno a la vez.'
    Write-Aviso 'Para liberarlo:'
    Write-Host  "  Get-NetTCPConnection -LocalPort $Puerto -State Listen | ForEach-Object { Stop-Process -Id `$_.OwningProcess -Force }"
    exit 1
}

$secreto = $env:LIGA_JWT_SECRET
if ([string]::IsNullOrWhiteSpace($secreto)) {
    $secreto = [Environment]::GetEnvironmentVariable('LIGA_JWT_SECRET', 'User')
}

$generar = $false
if ([string]::IsNullOrWhiteSpace($secreto)) {
    $generar = $true
} elseif ([Text.Encoding]::UTF8.GetByteCount($secreto) -lt 32) {
    Write-Aviso 'La clave LIGA_JWT_SECRET guardada es demasiado corta (menos de 32 bytes).'
    Write-Aviso 'Se genera una nueva y se reemplaza la anterior.'
    $generar = $true
}

if ($generar) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    } finally {
        $rng.Dispose()
    }
    $secreto = [Convert]::ToBase64String($bytes)
    [Environment]::SetEnvironmentVariable('LIGA_JWT_SECRET', $secreto, 'User')
    Write-Ok 'Clave JWT generada y guardada como variable de usuario (no va al repositorio).'
} else {
    Write-Ok 'Usando la clave JWT ya configurada en esta maquina.'
}

$env:LIGA_JWT_SECRET = $secreto

Write-Host ''
Write-Aviso 'Recordatorio: spring.sql.init.mode=always reinicia la base en cada arranque.'
Write-Aviso 'Se pierden equipos, jugadores y partidos cargados.'
Write-Aviso 'El administrador, el torneo, las canchas y los arbitros se recargan solos.'
Write-Host ''
Write-Host "API:     http://localhost:$Puerto/api"
Write-Host 'Login:   admin / Admin2026!'
Write-Host 'Detener: Ctrl + C'
Write-Host ''

$codigo = 0
Push-Location $backendDir
try {
    & .\mvnw.cmd spring-boot:run
    $codigo = $LASTEXITCODE
} finally {
    Pop-Location
}

exit $codigo
