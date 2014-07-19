@echo off
set BIN_DIR=%~dp0%
set LIB_DIR=%BIN_DIR%\..\lib
echo %LIB_DIR%
java -jar %LIB_DIR%\${project.artifactId}-${project.version}.${project.packaging} %*
