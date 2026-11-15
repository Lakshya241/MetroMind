@echo off
if not exist "bin\com\metromind\Main.class" (
    echo Binaries not found. Compiling first...
    call compile.bat
)
java -cp bin com.metromind.Main
pause
