$ErrorActionPreference = 'Stop'
$Project = $PSScriptRoot
$Backend = Join-Path $Project 'backend'
$Frontend = Join-Path $Project 'frontend'
$Work = Join-Path $Project 'work'
New-Item -ItemType Directory -Path $Work -Force | Out-Null

function Test-LocalPort([int]$Port) {
    $Client = [System.Net.Sockets.TcpClient]::new()
    try {
        $Connect = $Client.BeginConnect('127.0.0.1', $Port, $null, $null)
        return $Connect.AsyncWaitHandle.WaitOne(500) -and $Client.Connected
    } catch {
        return $false
    } finally {
        $Client.Dispose()
    }
}

foreach ($Port in @(8080, 5173)) {
    if (Test-LocalPort $Port) {
        throw "Port $Port is already in use. Stop the existing service, then run this script again."
    }
}

$Java = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $Java = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $InstalledJdks = Get-ChildItem 'C:\Program Files\Java' -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending
    if ($InstalledJdks) { $Java = Join-Path $InstalledJdks[0].FullName 'bin\java.exe' }
    if (-not $Java) { $Java = (Get-Command java.exe -ErrorAction SilentlyContinue).Source }
}
if (-not (Test-Path -LiteralPath $Java)) { throw 'Java was not found. Install Java 17 or newer.' }
$Node = (Get-Command node.exe -ErrorAction Stop).Source
$Jar = Join-Path $Backend 'target\digital-twin-student-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $Jar)) { throw 'Backend JAR is missing. Build backend with Maven first.' }

if (-not (Test-Path (Join-Path $Frontend 'node_modules'))) {
    Push-Location $Frontend
    try {
        & npm.cmd install
        if ($LASTEXITCODE -ne 0) { throw 'npm install failed.' }
    } finally { Pop-Location }
}

$SecretBytes = New-Object byte[] 48
$Generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Generator.GetBytes($SecretBytes)
$PreviousProfile = $env:SPRING_PROFILES_ACTIVE
$PreviousSecret = $env:JWT_SECRET
$PreviousOrigin = $env:FRONTEND_URL
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:JWT_SECRET = [Convert]::ToBase64String($SecretBytes)
$env:FRONTEND_URL = 'http://localhost:5173'

$BackendOut = Join-Path $Work 'backend.log'
$BackendErr = Join-Path $Work 'backend-error.log'
try {
    $BackendProcess = Start-Process -FilePath $Java -ArgumentList @('-jar', "`"$Jar`"") -WorkingDirectory $Backend -WindowStyle Hidden -PassThru -RedirectStandardOutput $BackendOut -RedirectStandardError $BackendErr
} finally {
    $env:SPRING_PROFILES_ACTIVE = $PreviousProfile
    $env:JWT_SECRET = $PreviousSecret
    $env:FRONTEND_URL = $PreviousOrigin
}

try {
    $Ready = $false
    for ($Attempt = 0; $Attempt -lt 40; $Attempt++) {
        if ($BackendProcess.HasExited) { throw "Backend exited during startup. See $BackendErr" }
        try {
            $Health = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 2
            if ($Health.StatusCode -eq 200) { $Ready = $true; break }
        } catch { }
        Start-Sleep -Milliseconds 750
    }
    if (-not $Ready) { throw "Backend did not become ready. See $BackendOut and $BackendErr" }

    $FrontendOut = Join-Path $Work 'frontend.log'
    $FrontendErr = Join-Path $Work 'frontend-error.log'
    $Vite = Join-Path $Frontend 'node_modules\vite\bin\vite.js'
    $FrontendProcess = Start-Process -FilePath $Node -ArgumentList @("`"$Vite`"", '--host', 'localhost') -WorkingDirectory $Frontend -WindowStyle Hidden -PassThru -RedirectStandardOutput $FrontendOut -RedirectStandardError $FrontendErr
    $Ready = $false
    for ($Attempt = 0; $Attempt -lt 30; $Attempt++) {
        if ($FrontendProcess.HasExited) { throw "Frontend exited during startup. See $FrontendErr" }
        try {
            $Page = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:5173/' -TimeoutSec 2
            if ($Page.StatusCode -eq 200) { $Ready = $true; break }
        } catch { }
        Start-Sleep -Milliseconds 500
    }
    if (-not $Ready) { throw "Frontend did not become ready. See $FrontendOut and $FrontendErr" }

    [ordered]@{ backendPid = $BackendProcess.Id; frontendPid = $FrontendProcess.Id } |
        ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Work 'local-dev-pids.json')
    Write-Host 'Digital Twin is running.' -ForegroundColor Green
    Write-Host 'Dashboard: http://localhost:5173'
    Write-Host 'Backend health: http://localhost:8080/actuator/health'
    Write-Host 'Stop both services: .\stop-local.ps1'
    Write-Host 'Local profile uses in-memory data; it resets when the backend stops.'
} catch {
    if ($FrontendProcess -and -not $FrontendProcess.HasExited) { Stop-Process -Id $FrontendProcess.Id -Force }
    if ($BackendProcess -and -not $BackendProcess.HasExited) { Stop-Process -Id $BackendProcess.Id -Force }
    throw
}
