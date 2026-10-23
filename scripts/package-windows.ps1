param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    $jdk = $env:JAVA_HOME
    if (-not $jdk) { $jdk = Split-Path -Parent (Split-Path -Parent (Get-Command javac.exe -ErrorAction Stop).Source) }
    foreach ($tool in @('java.exe', 'jlink.exe', 'jpackage.exe')) {
        if (-not (Test-Path -LiteralPath (Join-Path $jdk "bin\$tool"))) { throw 'Install a 64-bit JDK 21 and set JAVA_HOME.' }
    }
    $env:JAVA_HOME = $jdk
    if (-not $SkipBuild) {
        & .\mvnw.cmd -B clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed.' }
    }
    [xml]$pom = Get-Content -LiteralPath 'pom.xml'
    $version = $pom.project.version
    $appName = 'RubiksCubeScrambleWidget'
    $build = Join-Path $projectRoot '.build'
    $dist = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Force -Path $build, $dist | Out-Null

    # Only these generated paths may be removed. Never clean the project or user data.
    function Reset-GeneratedDirectory([string]$path) {
        $absolute = [IO.Path]::GetFullPath($path)
        $allowed = @((Join-Path $build 'package-input'), (Join-Path $build 'runtime'),
                     (Join-Path $build 'source'), (Join-Path $dist $appName))
        if ($absolute -notin $allowed) { throw "Refusing to remove unexpected path: $absolute" }
        if (Test-Path -LiteralPath $absolute) { Remove-Item -LiteralPath $absolute -Recurse -Force }
    }
    $inputDir = Join-Path $build 'package-input'
    $runtimeDir = Join-Path $build 'runtime'
    $sourceDir = Join-Path $build 'source'
    foreach ($path in @($inputDir, $runtimeDir, $sourceDir, (Join-Path $dist $appName))) {
        Reset-GeneratedDirectory $path
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $inputDir 'lib'), $sourceDir | Out-Null
    Copy-Item -LiteralPath 'target\scramble-widget.jar' -Destination $inputDir
    Copy-Item -Path 'target\lib\*.jar' -Destination (Join-Path $inputDir 'lib')

    & (Join-Path $jdk 'bin\jlink.exe') --add-modules java.desktop,java.logging,jdk.unsupported --strip-debug --no-header-files --no-man-pages --compress=zip-6 --output $runtimeDir
    if ($LASTEXITCODE -ne 0) { throw 'Cannot build the Java runtime.' }
    $packageArgs = @('--type', 'app-image', '--name', $appName, '--app-version', $version,
        '--description', 'Offline 3x3 Rubik''s Cube scramble desktop widget powered by TNoodle',
        '--vendor', 'Rubiks Cube Scramble Widget contributors', '--input', $inputDir,
        '--main-jar', 'scramble-widget.jar', '--main-class', 'io.github.scramblewidget.App',
        '--runtime-image', $runtimeDir, '--dest', $dist,
        '--java-options', '-Xms16m', '--java-options', '-Xmx128m',
        '--java-options', '-XX:+UseSerialGC', '--java-options', '--enable-native-access=ALL-UNNAMED',
        '--icon', (Join-Path $projectRoot 'docs\icon.ico'))
    & (Join-Path $jdk 'bin\jpackage.exe') @packageArgs
    if ($LASTEXITCODE -ne 0) { throw 'Cannot package the Windows app.' }

    $appDir = Join-Path $dist $appName
    Copy-Item -LiteralPath 'README.md', 'README.pl.md', 'LICENSE', 'THIRD-PARTY-NOTICES.md' -Destination $appDir
    Copy-Item -LiteralPath 'licenses' -Destination $appDir -Recurse

    # Ship matching sources, including library sources, beside the binary ZIP.
    foreach ($path in @('pom.xml', 'mvnw', 'mvnw.cmd', 'build.cmd', 'run.cmd', '.mvn', '.github', '.gitignore', '.gitattributes',
        'src', 'scripts', 'docs', 'README.md', 'README.pl.md', 'LICENSE', 'THIRD-PARTY-NOTICES.md', 'licenses')) {
        Copy-Item -LiteralPath $path -Destination $sourceDir -Recurse
    }
    & .\mvnw.cmd -B dependency:copy-dependencies -Dclassifier=sources -DincludeScope=runtime "-DoutputDirectory=$sourceDir\third-party-sources"
    if ($LASTEXITCODE -ne 0) { throw 'Cannot collect matching dependency sources.' }
    $binaryZip = Join-Path $dist "rubiks-cube-scramble-widget-$version-windows-x64.zip"
    $sourceZip = Join-Path $dist "rubiks-cube-scramble-widget-$version-source.zip"
    Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
    foreach ($zip in @($binaryZip, $sourceZip)) {
        if (Test-Path -LiteralPath $zip) { Remove-Item -LiteralPath $zip -Force }
    }
    function Write-Zip([string]$directory, [string]$zipPath, [bool]$includeRoot) {
        $basePath = [IO.Path]::GetFullPath($directory).TrimEnd('\') + '\'
        $prefix = if ($includeRoot) { (Split-Path -Leaf $directory) + '/' } else { '' }
        $archive = [IO.Compression.ZipFile]::Open($zipPath, [IO.Compression.ZipArchiveMode]::Create)
        try {
            foreach ($file in Get-ChildItem -LiteralPath $directory -File -Recurse -Force) {
                $entry = $prefix + $file.FullName.Substring($basePath.Length).Replace('\', '/')
                [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $file.FullName, $entry,
                    [IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
        } finally { $archive.Dispose() }
    }
    Write-Zip $appDir $binaryZip $true
    Write-Zip $sourceDir $sourceZip $false
    $checksums = foreach ($zip in @($binaryZip, $sourceZip)) {
        (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant() + '  ' + (Split-Path -Leaf $zip)
    }
    $checksums | Set-Content -LiteralPath (Join-Path $dist 'SHA256SUMS.txt') -Encoding ascii
    Get-Item -LiteralPath $binaryZip, $sourceZip | Select-Object Name, @{Name='MB';Expression={[math]::Round($_.Length / 1MB, 2)}}
    Write-Host "Run: $appDir\$appName.exe"
} finally { Pop-Location }
