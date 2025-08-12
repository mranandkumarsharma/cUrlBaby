@echo off
echo 🔄 Cleaning old class files...
rd /s /q bin 2>nul
mkdir bin

echo 🛠️ Compiling Java sources...

REM Set lib path (updated to match current structure)
set LIB_PATH=lib

REM Set output directory (updated to match current structure)
set OUT_DIR=bin

REM Set source directory
set SRC_DIR=src

REM Collect all source files from src directory
dir /b /s %SRC_DIR%\com\curlbaby\*.java > sources.txt

REM Build classpath from jars
setlocal enabledelayedexpansion
set CP=
for %%i in (%LIB_PATH%\*.jar) do (
    set CP=!CP!;%%i
)
REM Remove leading semicolon if CP is not empty
if defined CP set CP=%CP:~1%

REM Compile with proper classpath
if defined CP (
    javac -d %OUT_DIR% -cp "%CP%" @sources.txt
) else (
    echo ⚠️ No JAR files found in %LIB_PATH%. Compiling without external dependencies...
    javac -d %OUT_DIR% @sources.txt
)

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed.
    del sources.txt 2>nul
    exit /b %ERRORLEVEL%
)

echo ✅ Compilation successful. Classes are in %OUT_DIR%.
echo 📁 Project structure:
echo    📂 src\com\curlbaby\ - Source files
echo    📂 lib\ - JAR dependencies  
echo    📂 bin\ - Compiled classes

del sources.txt
