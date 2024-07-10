@echo off

if "%JAVA_HOME%" == "" (
  set JAVA_CMD="java"
) else (
  set JAVA_CMD="%JAVA_HOME%\bin\java"
)

set JQASSISTANT_HOME=%~dp0%\..

%JAVA_CMD% %JQASSISTANT_OPTS% -jar "%JQASSISTANT_HOME%\lib\${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" %*
