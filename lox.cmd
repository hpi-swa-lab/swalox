FOR /F "USEBACKQ delims=" %%F IN (`mvn -q exec:exec "-Dexec.executable=cmd" "-Dexec.args='/c echo %%classpath'"`) DO SET "CLASSPATH=%%F"
SET CLASSPATH=%CLASSPATH:"=%
SET "CLASSPATH=%CLASSPATH%;%~dp0target\classes"

IF ["%JAVA_HOME%"] == [""] (
    SET "JAVA=java"
) ELSE (
    SET "JAVA=%JAVA_HOME%\bin\java.exe"
)

"%JAVA%" -cp "%CLASSPATH%" "de.hpi.swa.lox.cli.LoxMain" %*