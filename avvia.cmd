@echo off
rem Avvia backend e frontend in due finestre separate.
rem Chiudere una finestra ferma il processo corrispondente.
setlocal
cd /d "%~dp0"

if not exist "U5W1D2\env.properties" (
    echo.
    echo Manca il file U5W1D2\env.properties.
    echo Crealo con DB_PASSWORD e JWT_SECRET, poi rilancia questo script.
    echo.
    pause
    exit /b 1
)

if not exist "frontend\node_modules" (
    echo Installo le dipendenze del frontend...
    call npm --prefix frontend install || (
        echo Installazione fallita.
        pause
        exit /b 1
    )
)

echo Avvio backend su http://localhost:3001 ...
start "Backend U5W1D2" /d "%~dp0U5W1D2" cmd /k mvnw.cmd spring-boot:run

echo Avvio frontend su http://localhost:5173 ...
start "Frontend U5W1D2" /d "%~dp0frontend" cmd /k npm run dev

echo.
echo Due finestre aperte. Il backend impiega qualche secondo prima di rispondere.
endlocal
