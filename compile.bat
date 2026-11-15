@echo off
echo ===================================================
echo   Compiling MetroMind Transit Engine (Java)...
echo ===================================================

if not exist "bin" mkdir bin
if not exist "exports" mkdir exports
if exist "sources.txt" del sources.txt

powershell -NoProfile -ExecutionPolicy Bypass -Command "$files = (Get-ChildItem -Path src -Filter *.java -Recurse).FullName | ForEach-Object { '\"' + $_.Replace('\', '/') + '\"' }; [System.IO.File]::WriteAllLines('sources.txt', $files)"

javac -d bin @sources.txt
if exist "sources.txt" del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Output directory: bin\
) else (
    echo [ERROR] Compilation failed.
)
