# Check the actual packaged launcher and bundled runtime, using isolated user data.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$exe = Join-Path $projectRoot 'dist\RubiksCubeScrambleWidget\RubiksCubeScrambleWidget.exe'
if (-not (Test-Path -LiteralPath $exe)) { throw 'Run build.cmd first.' }
$profileDir = Join-Path $projectRoot ('.build\smoke-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $profileDir -Force | Out-Null
$previousAppData = $env:APPDATA
$process = $null
try {
    $env:APPDATA = $profileDir
    $process = Start-Process -FilePath $exe -ArgumentList '--hidden' -WindowStyle Hidden -PassThru
    $log = Join-Path $profileDir 'RubiksCubeScrambleWidget\widget.log'
    $started = $false
    for ($i = 0; $i -lt 60; $i++) {
        if ($process.HasExited) { throw "Packaged app exited with code $($process.ExitCode)." }
        if ((Test-Path -LiteralPath $log) -and ((Get-Content -LiteralPath $log -Raw) -match 'Started; Java')) {
            $started = $true
            break
        }
        Start-Sleep -Milliseconds 250
    }
    if (-not $started) { throw "App did not report startup. Inspect $profileDir" }
    Start-Sleep -Seconds 3
    if ($process.HasExited) { throw 'Packaged app exited after startup.' }
    $content = Get-Content -LiteralPath $log -Raw
    if ($content -match 'SEVERE|WARNING|autostart available: false') { throw $content }
    $process.Refresh()
    Write-Output $content.Trim()
    Write-Output ('Packaged app is running; working set: {0:N1} MiB (one local measurement).' -f ($process.WorkingSet64 / 1MB))
} finally {
    $env:APPDATA = $previousAppData
    if ($process -and -not $process.HasExited) { Stop-Process -Id $process.Id }
}
