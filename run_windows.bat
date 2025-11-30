@echo off
title Obsidian Vault Launcher
color 0A

:: OBSIDIAN VAULT - Windows Launcher
:: ---------------------------------
:: Place this file in the project root (next to pom.xml)

cls
echo ==================================================
echo      OBSIDIAN VAULT - AUTOMATED LAUNCHER
echo ==================================================
echo.

:: Check for Java
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [!] ERROR: Java Development Kit (JDK) is not installed or not in your PATH.
    echo     Please install JDK 17+ and try again.
    echo.
    pause
    exit /b
)

:: Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

:: Compile
:: We point javac to src\main\java\ObsidianVault.java
echo [*] Compiling Source Code...
javac -d bin src\main\java\ObsidianVault.java

if %errorlevel% neq 0 (
    color 0C
    echo.
    echo [!] Compilation Failed. See errors above.
    pause
    exit /b
)

:: Run
echo [+] Compilation Successful.
echo [*] Launching Vault...
timeout /t 2 >nul
cls
:: Run using the classpath (-cp) pointing to bin
java -cp bin ObsidianVault

:: Clean exit
echo.
echo [System Halted]
pause