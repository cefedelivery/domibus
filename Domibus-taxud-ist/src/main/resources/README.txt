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

add the following properties into domibus.properties

#umds url
domibus.c4.rest.authenticate.endpoint=http://localhost:8001/Domibus-taxud-ics2-1.0-SNAPSHOT/authenticate

#c4 url
domibus.c4.rest.payload.endpoint=http://localhost:8001/Domibus-taxud-ics2-1.0-SNAPSHOT/message

#If the the association "service:action" equals the property the domain will be CUST else it will be TAX
domibus.taxud.cust.domain=bdx:noprocess:TC7Leg1

#Externalisation of the jms plugin providing queue concurrency.
domibus.internal.queue.concurency=30-30

#If true disable umds/c4 delivering and submission back.
domibus.do.not.deliver=true

Remove following properties from application.properties when deploying in weblogic.

server.port =
server.contextPath=


