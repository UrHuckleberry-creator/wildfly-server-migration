@echo off
rem -------------------------------------------------------------------------
rem JBoss Server Migration Tool for Windows
rem -------------------------------------------------------------------------
rem
rem A simple tool for migrating servers.

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

set "BASE_DIR=%CD%"

rem Setup JBoss specific properties
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

:setModularJdk
    "%JAVA%" --add-modules=java.se -version >nul 2>&1 && (set MODULAR_JDK=true) || (set MODULAR_JDK=false)
exit /B 0

:setDefaultModularJvmOptions
  call :setModularJdk
  if "!MODULAR_JDK!" == "true" (
    echo "%~1" | findstr /I "\-\-add\-modules" > nul
    if errorlevel == 1 (
      rem Set default modular jdk options
      set "DEFAULT_MODULAR_JVM_OPTIONS=!DEFAULT_MODULAR_JVM_OPTIONS! --add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
      set "DEFAULT_MODULAR_JVM_OPTIONS=!DEFAULT_MODULAR_JVM_OPTIONS! --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
      set "DEFAULT_MODULAR_JVM_OPTIONS=!DEFAULT_MODULAR_JVM_OPTIONS! --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED"
      set "DEFAULT_MODULAR_JVM_OPTIONS=!DEFAULT_MODULAR_JVM_OPTIONS! --add-modules=java.se"
    ) else (
      set "DEFAULT_MODULAR_JVM_OPTIONS="
    )
  )
exit /B 0

rem set default modular jvm parameters
setlocal EnableDelayedExpansion
call :setDefaultModularJvmOptions "!JAVA_OPTS!"
set "JAVA_OPTS=!JAVA_OPTS! !DEFAULT_MODULAR_JVM_OPTIONS!"
setlocal DisableDelayedExpansion

set "JAVA_OPTS=%JAVA_OPTS% -Djboss.server.migration.baseDir=%BASE_DIR%"

echo "%JAVA_OPTS%" | findstr /I "logging.configuration" > nul
if errorlevel == 1 (
  set "JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=org.jboss.logmanager.LogManager -Dlogging.configuration=file:%BASE_DIR%\configuration\logging.properties -Djboss.server.migration.logfile=%BASE_DIR%\logs\migration.log"
) else (
  echo logging.configuration already set in JAVA_OPTS
)

"%JAVA%" %JAVA_OPTS% ^
    -cp "%BASE_DIR%\lib\*" ^
    org.jboss.migration.cli.CommandLineServerMigration ^
    %*

set /A RC=%errorlevel%
:END
if "x%NOPAUSE%" == "x" pause

if "x%RC%" == "x" (
  set /A RC=0
)
exit /B %RC%
