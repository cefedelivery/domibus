Add following line into setDomainEnv in order to externalise the mock configuration.
set EXTRA_JAVA_PROPERTIES=%EXTRA_JAVA_PROPERTIES% -Dspring.config.location=classpath:/application.properties,%DOMAIN_HOME%\conf\domibus\application.properties

spring.profiles.active should be put to dev or stress depending of what is needed.
dev=console, stress=file
set EXTRA_JAVA_PROPERTIES=%EXTRA_JAVA_PROPERTIES% -Dspring.profiles.active=stress

copy the application.properties into %DOMAIN_HOME%\conf\domibus\