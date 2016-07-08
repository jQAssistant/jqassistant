@echo off
set BIN_DIR=%~dp0%
@if "%JQASSISTANT_HOME%" == ""  (
  set JQASSISTANT_HOME=%BIN_DIR%\..
)
set LIB_DIR=%JQASSISTANT_HOME%\lib
java %JQASSISTANT_OPTS% -jar "%LIB_DIR%\${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" %*
