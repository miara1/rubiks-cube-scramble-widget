@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\package-windows.ps1" %*
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)
