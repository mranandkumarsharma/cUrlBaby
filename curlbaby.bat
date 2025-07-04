@echo off
setlocal enabledelayedexpansion

REM Set script directory to current location
set SCRIPT_DIR=%~dp0
cd /d %SCRIPT_DIR%

REM Create necessary directories
mkdir curlbaby\target\classes 2>nul
mkdir curlbaby\lib 2>nul

REM Check if Java is installed
where java >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java to run cUrlBaby
    exit /b 1
)

REM Check if json-simple JAR exists, otherwise copy from backup
if not exist curlbaby\lib\json-simple-1.1.1.jar (
    set FOUND_JAR=false
    for %%d in ("backup\client\lib" "..\backup\client\lib" "lib") do (
        if exist %%d\json-simple-1.1.1.jar (
            echo Copying json-simple-1.1.1.jar from %%d
            copy "%%d\json-simple-1.1.1.jar" "curlbaby\lib\" >nul
            set FOUND_JAR=true
            goto :after_copy
        )
    )
    :after_copy
    if not "%FOUND_JAR%"=="true" (
        echo Warning: json-simple-1.1.1.jar not found. The application may not function correctly.
    )
)

REM Collect Java files
set JAVA_FILES=
for /R %%f in (com\curlbaby\*.java) do (
    set JAVA_FILES=!JAVA_FILES! %%f
)

if "%JAVA_FILES%"=="" (
    echo Error: No Java source files found in com\curlbaby directory
    exit /b 1
)

REM Set classpath separator
set CP_SEP=;

REM Compile Java files
echo Compiling cUrlBaby application...
javac -cp "curlbaby\lib\*" -d curlbaby\target\classes %JAVA_FILES%
if errorlevel 1 (
    echo Compilation failed. Please fix the errors.
    exit /b 1
)

REM Run Java application
echo Compilation successful. Starting cUrlBaby application...
echo.
java -Djava.awt.headless=true -cp "curlbaby\target\classes%CP_SEP%curlbaby\lib\*" com.curlbaby.CurlBabyApp %*
