@echo off
setlocal
if not exist "%~dp0dist\RubiksCubeScrambleWidget\RubiksCubeScrambleWidget.exe" (
  echo First build the project with build.cmd, or download a ready-to-run ZIP from Releases.
  pause
  exit /b 1
)
start "" "%~dp0dist\RubiksCubeScrambleWidget\RubiksCubeScrambleWidget.exe" %*
