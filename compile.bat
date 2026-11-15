@echo off
echo ===================================================
echo   Compiling MetroMind Transit Engine (Java)...
echo ===================================================

if not exist "bin" mkdir bin
if not exist "exports" mkdir exports

javac -d bin -sourcepath src src\com\metromind\Main.java src\com\metromind\MetroMindTest.java src\com\metromind\model\*.java src\com\metromind\graph\*.java src\com\metromind\routing\*.java src\com\metromind\fare\*.java src\com\metromind\service\*.java src\com\metromind\util\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Output directory: bin\
) else (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed.
)
