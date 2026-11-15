@echo off
if not exist "bin\com\metromind\MetroMindTest.class" (
    echo Binaries not found. Compiling first...
    call compile.bat
)
java -cp bin com.metromind.MetroMindTest
pause
