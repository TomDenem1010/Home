@echo off
setlocal DisableDelayedExpansion

set "CONTAINER_NAME=oracle-xe"
set "IMAGE_NAME=gvenzl/oracle-xe:21-slim"
set "DATABASE_VOLUME=oracle-xe-data"
set "PDB_NAME=XEPDB1"
set "STARTUP_RETRIES=120"
set "ENV_FILE=%~dp0.env"

if not exist "%ENV_FILE%" (
    echo .env file not found: %ENV_FILE%
    exit /b 1
)

set "DB_USERNAME="
set "DB_PASSWORD="
for /f "usebackq tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
    if /i "%%A"=="DB_USERNAME" set "DB_USERNAME=%%B"
    if /i "%%A"=="DB_PASSWORD" set "DB_PASSWORD=%%B"
)

if not defined DB_USERNAME (
    echo Missing DB_USERNAME value in %ENV_FILE%.
    exit /b 1
)
if not defined DB_PASSWORD (
    echo Missing DB_PASSWORD value in %ENV_FILE%.
    exit /b 1
)

docker version >nul 2>&1
if errorlevel 1 (
    echo Docker is not available.
    exit /b 1
)

echo Removing the previous Oracle XE database...
for /f %%I in ('docker ps -aq --filter "name=^/%CONTAINER_NAME%$"') do set "EXISTING_CONTAINER=%%I"
if defined EXISTING_CONTAINER (
    docker rm -f %CONTAINER_NAME% >nul
    if errorlevel 1 goto :remove_container_failed
)

for /f %%I in ('docker volume ls -q --filter "name=^%DATABASE_VOLUME%$"') do set "EXISTING_VOLUME=%%I"
if defined EXISTING_VOLUME (
    docker volume rm %DATABASE_VOLUME% >nul
    if errorlevel 1 goto :remove_volume_failed
)

echo Creating Oracle XE container...
docker run -d ^
    --name %CONTAINER_NAME% ^
    -p 1521:1521 ^
    -e "ORACLE_PASSWORD=%DB_PASSWORD%" ^
    -v "%DATABASE_VOLUME%:/opt/oracle/oradata" ^
    %IMAGE_NAME% >nul
if errorlevel 1 goto :create_container_failed

echo Waiting for Oracle XE startup...
set /a RETRIES_LEFT=%STARTUP_RETRIES%
:wait_for_oracle
docker logs %CONTAINER_NAME% 2>&1 | findstr /C:"DATABASE IS READY TO USE!" >nul
if not errorlevel 1 goto :oracle_ready

set /a RETRIES_LEFT-=1
if %RETRIES_LEFT% LEQ 0 goto :startup_timeout
timeout /t 5 /nobreak >nul
goto :wait_for_oracle

:oracle_ready
set "ORACLE_USERNAME=%DB_USERNAME%"

set "TEMP_SQL=%TEMP%\oracle-xe-create-user-%RANDOM%%RANDOM%.sql"
(
    echo WHENEVER SQLERROR EXIT SQL.SQLCODE;
    echo ALTER SESSION SET CONTAINER = %PDB_NAME%;
    echo CREATE USER %ORACLE_USERNAME% IDENTIFIED BY "%DB_PASSWORD%";
    echo GRANT DBA TO %ORACLE_USERNAME%;
    echo GRANT ALL PRIVILEGES TO %ORACLE_USERNAME%;
    echo GRANT UNLIMITED TABLESPACE TO %ORACLE_USERNAME%;
    echo EXIT;
) > "%TEMP_SQL%"

echo Creating %ORACLE_USERNAME% administrator in %PDB_NAME%...
docker exec -i --user oracle %CONTAINER_NAME% sqlplus -s / as sysdba < "%TEMP_SQL%"
set "SQLPLUS_EXIT_CODE=%ERRORLEVEL%"
del "%TEMP_SQL%"
if not "%SQLPLUS_EXIT_CODE%"=="0" goto :create_user_failed

echo Done. Connection: jdbc:oracle:thin:@//localhost:1521/%PDB_NAME%
exit /b 0

:remove_container_failed
echo Could not remove container %CONTAINER_NAME%.
exit /b 1

:remove_volume_failed
echo Could not remove volume %DATABASE_VOLUME%.
exit /b 1

:create_container_failed
echo Could not create the Oracle XE container from image %IMAGE_NAME%.
exit /b 1

:startup_timeout
echo Oracle XE did not become ready within 600 seconds. Check: docker logs %CONTAINER_NAME%
exit /b 1

:create_user_failed
echo Oracle user creation failed.
exit /b 1
