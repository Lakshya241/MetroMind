@echo off
title MetroMind - Bengaluru Namma Metro
echo ========================================================
echo   Compiling and Launching MetroMind Transit Engine...
echo ========================================================

javac *.java
if %ERRORLEVEL% equ 0 (
    java Main %*
) else (
    echo [ERROR] Java compilation failed. Please verify JDK 11+ is installed.
    pause
)
if "%1"=="" pause
