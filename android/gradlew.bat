@ECHO OFF
SETLOCAL

SET "APP_HOME=%~dp0"
SET "PROJECT_DIR=%APP_HOME%."
SET "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
SET "LOCAL_GRADLE=%APP_HOME%..\tools\gradle\current\bin\gradle.bat"

IF EXIST "%WRAPPER_JAR%" (
  IF EXIST "%APP_HOME%..\tools\jdk\current\bin\java.exe" (
    SET "JAVA_EXE=%APP_HOME%..\tools\jdk\current\bin\java.exe"
  ) ELSE (
    SET "JAVA_EXE=java"
  )
  "%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
  ENDLOCAL
  EXIT /B %ERRORLEVEL%
)

IF EXIST "%LOCAL_GRADLE%" (
  IF EXIST "%APP_HOME%..\tools\jdk\current\bin\java.exe" (
    SET "JAVA_HOME=%APP_HOME%..\tools\jdk\current"
    SET "PATH=%JAVA_HOME%\bin;%PATH%"
  )
  "%LOCAL_GRADLE%" -p "%PROJECT_DIR%" %*
  ENDLOCAL
  EXIT /B %ERRORLEVEL%
)

ECHO Missing wrapper jar and local Gradle.
ECHO Run scripts\bootstrap-gradle.ps1 after download-dev-env.ps1.
ENDLOCAL
