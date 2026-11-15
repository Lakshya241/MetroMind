@echo off
if not exist "bin\MetroMindTest.class" (
    echo Binaries not found. Compiling first...
    call compile.bat
)
java -cp bin MetroMindTest
pause
