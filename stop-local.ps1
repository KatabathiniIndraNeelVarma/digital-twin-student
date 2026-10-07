$ErrorActionPreference = 'Stop'
$PidFile = Join-Path $PSScriptRoot 'work\local-dev-pids.json'
if (-not (Test-Path -LiteralPath $PidFile)) { Write-Host 'No local app process record found.'; exit 0 }
$Processes = Get-Content -Raw -LiteralPath $PidFile | ConvertFrom-Json
foreach ($Entry in @(@{ Id = [int]$Processes.frontendPid; Name = 'node' }, @{ Id = [int]$Processes.backendPid; Name = 'java' })) {
    $Process = Get-Process -Id $Entry.Id -ErrorAction SilentlyContinue
    if ($Process -and $Process.ProcessName -eq $Entry.Name) {
        Stop-Process -Id $Entry.Id -Force
        Write-Host "Stopped $($Entry.Name) process $($Entry.Id)."
    }
}
Remove-Item -LiteralPath $PidFile -Force
