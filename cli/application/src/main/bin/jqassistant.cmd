@echo off
set JQASSISTANT_HOME=%~dp0%\..
java %JQASSISTANT_OPTS% -jar "%JQASSISTANT_HOME%\lib\${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" %*
