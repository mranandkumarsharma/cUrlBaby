@echo off
setlocal enabledelayedexpansion

REM Set script directory to current location
set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%

REM Create necessary directories
mkdir bin 2>nul
mkdir lib 2>nul

REM Check if Java is installed
where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java to run cUrlBaby
    exit /b 1
)

REM Check for Jackson JARs
set FOUND_ALL=true

for %%J in (
    jackson-core-2.17.0.jar
    jackson-databind-2.17.0.jar
    jackson-annotations-2.17.0.jar
) do (
    if not exist lib\%%J (
        echo Missing: %%J in lib
        set FOUND_ALL=false
    )
)

if "%FOUND_ALL%"=="false" (
    echo.
    echo Please download required Jackson JARs and place them in lib:
    echo - jackson-core-2.17.0.jar
    echo - jackson-databind-2.17.0.jar
    echo - jackson-annotations-2.17.0.jar
    echo.
    exit /b 1
)

REM Collect Java files from src directory
set JAVA_FILES=
for /R src %%f in (com\curlbaby\*.java) do (
    set JAVA_FILES=!JAVA_FILES! %%f
)

if "%JAVA_FILES%"=="" (
    echo Error: No Java source files found in src\com\curlbaby directory
    exit /b 1
)

REM Set classpath separator
set CP_SEP=;

REM Compile Java files
echo Compiling cUrlBaby application with Jackson...
javac -cp "lib\*" -d bin %JAVA_FILES%
if errorlevel 1 (
    echo Compilation failed. Please fix the errors.
    exit /b 1
)

REM Run Java application
echo Compilation successful. Starting cUrlBaby application...
echo.
java -Djava.awt.headless=true -cp "bin%CP_SEP%lib\*" com.curlbaby.CurlBabyApp %*
