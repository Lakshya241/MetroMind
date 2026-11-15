@echo off
if not exist "bin\Main.class" (
    echo Binaries not found. Compiling first...
    call compile.bat
)
java -cp bin Main
pause
