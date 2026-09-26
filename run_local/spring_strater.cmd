@echo off
setlocal
pushd "%~dp0.."
call mvn clean package -DskipTests spring-boot:run
set "RUN_EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %RUN_EXIT_CODE%
