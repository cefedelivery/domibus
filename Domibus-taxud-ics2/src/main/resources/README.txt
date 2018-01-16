BUILD

Do not add the default-plugins profile as we introduce a circular dependency between "Domibus MSH Default JMS Plugin" and "Domibus MSH" ins the scope of this
poc.

CONFIGURATION

create directory %DOMAIN_HOME%\conf\taxud-umds
copy logback-file.xml into the new directory and rename it to logback.xml

In case there is a need to override application.properties value, copy it to the new directory
and override the needed property.

Add following line into setDomainEnv in order to externalise the mock configuration and logging.

set EXTRA_JAVA_PROPERTIES=%EXTRA_JAVA_PROPERTIES% -Dspring.config.location=classpath:/application.properties,%DOMAIN_HOME%\conf\taxud-umds\application.properties
set EXTRA_JAVA_PROPERTIES=%EXTRA_JAVA_PROPERTIES% -Dlogging.config=%DOMAIN_HOME%\conf\taxud-umds\logback.xml


