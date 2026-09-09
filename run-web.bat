@echo off
echo ===============================================================
echo  Starting Public Bus Route Finder Web Server...
echo ===============================================================

REM Compile backend classes
if not exist "out\production\Public-Bus-Route-Finder-Kathmandu-Valley" mkdir "out\production\Public-Bus-Route-Finder-Kathmandu-Valley"

echo Compiling Java backend...
javac -cp "src;lib/*" -d out/production/Public-Bus-Route-Finder-Kathmandu-Valley src/model/enums/*.java src/model/*.java src/service/*.java src/util/*.java src/server/*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed! Please check Java installation.
    pause
    exit /b %ERRORLEVEL%
)

echo Starting WebServer at http://localhost:8080 ...
start http://localhost:8080
java -cp "out/production/Public-Bus-Route-Finder-Kathmandu-Valley;lib/*" server.WebServer

pause
