@ECHO off
SETLOCAL EnableDelayedExpansion

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::: The following properties need to be modified by the users :::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: The location where the Wildfly12 instance is installed
SET JBOSS_HOME=C:\path\to\wildfly12

:: The name of the standalone configuration file that need to be updated: standalone-full.xml for a
:: non-clustered Wildfly12 environment and standalone-full-ha.xml for a clustered one.
SET SERVER_CONFIG=standalone-full.xml
:: SET SERVER_CONFIG=standalone-full-ha.xml

:: MySQL configuration
SET DB_TYPE=MySQL
SET DB_HOST=localhost
SET "DB_NAME=domibus?autoReconnect=true^&useSSL=false"
SET DB_PORT=3306
SET DB_USER=edelivery
SET DB_PASS=edelivery
SET JDBC_CONNECTION_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/!DB_NAME!
SET JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\mysql\main
SET JDBC_DRIVER_NAME=mysql-connector-java-5.1.46.jar

:: Oracle configuration
:: SET DB_TYPE=Oracle
:: SET DB_HOST=localhost
:: SET DB_PORT=1521
:: SET DB_USER=edelivery_user
:: SET DB_PASS=edelivery_password
:: SET JDBC_CONNECTION_URL="jdbc:oracle:thin:@%DB_HOST%:%DB_PORT%[:SID|/Service]"
:: SET JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\oracle\main
:: SET JDBC_DRIVER_NAME=ojdbc6-12.1.0.2.jar

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::: The following part is not to be modified by the users ::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

ECHO --------------JBOSS_HOME: %JBOSS_HOME%
ECHO --------------SERVER_CONFIG: %SERVER_CONFIG%
ECHO --------------DB_TYPE: %DB_TYPE%
ECHO --------------DB_HOST: %DB_HOST%
ECHO --------------DB_NAME: %DB_NAME%
ECHO --------------DB_PORT: %DB_PORT%
ECHO --------------DB_USER: %DB_USER%
ECHO --------------DB_PASS: %DB_PASS%
ECHO --------------JDBC_CONNECTION_URL: %JDBC_CONNECTION_URL%
ECHO --------------JDBC_DRIVER_DIR: %JDBC_DRIVER_DIR%
ECHO --------------JDBC_DRIVER_NAME: %JDBC_DRIVER_NAME%

ECHO --------------Configure Wildfly12 to resolve parameter values from properties files
@PowerShell -Command "(Get-Content %JBOSS_HOME%/bin/jboss-cli.xml) -replace '<resolve-parameter-values>false</resolve-parameter-values>', '<resolve-parameter-values>true</resolve-parameter-values>' | Out-File -encoding UTF8 %JBOSS_HOME%/bin/jboss-cli.xml"

ECHO --------------Prepare
SET > env.properties
@PowerShell -Command "(Get-Content env.properties) -replace '\\', '\\' | Out-File -encoding ASCII env.properties"
@PowerShell -Command "(Get-Content env.properties) -replace '\^&', '&' | Out-File -encoding ASCII env.properties"

ECHO --------------Configure Wildfly12
IF "%SERVER_CONFIG%" == "standalone-full-ha.xml" (
	%JBOSS_HOME%\bin\jboss-cli.bat --file=resources\domibus-configuration-%DB_TYPE%-cluster.cli --properties=env.properties
) ELSE (
	%JBOSS_HOME%\bin\jboss-cli.bat --file=resources\domibus-configuration-%DB_TYPE%.cli --properties=env.properties
)

ECHO --------------Clean
DEL env.properties
