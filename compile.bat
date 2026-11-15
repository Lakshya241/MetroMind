@echo off
echo ===================================================
echo   Compiling MetroMind Transit Engine (Java)...
echo ===================================================

if not exist "bin" mkdir bin
if not exist "exports" mkdir exports

javac -d bin -sourcepath src src\Main.java src\MetroMindTest.java src\model\*.java src\graph\*.java src\route\*.java src\fare\*.java src\service\*.java src\util\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Output directory: bin\
) else (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed.
)
