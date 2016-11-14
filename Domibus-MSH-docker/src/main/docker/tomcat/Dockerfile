FROM java:7-jre

MAINTAINER Cosmin Baciu <cosmin.baciu@ext.ec.europa.eu>

ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.0.24
ENV DOCKERIZE_VERSION v0.2.0
ENV TOMCAT_ZIP_URL ./domibus-tomcat-full.zip

ENV SAMPLE_CONFIGURATION_URL ./domibus-sample-configuration-and-testing.zip
ENV MYSQL_CONNECTOR_URL ./mysql-connector-java-5.1.38-bin.jar

ENV DOMIBUS_DIST /usr/local/domibusDist
ENV TOMCAT_FULL_DISTRIBUTION /usr/local/tomcat
ENV CATALINA_HOME $TOMCAT_FULL_DISTRIBUTION/domibus

RUN apt-get update
RUN apt-get install -y wget
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

COPY $TOMCAT_ZIP_URL $DOMIBUS_DIST/
COPY $SAMPLE_CONFIGURATION_URL $DOMIBUS_DIST/

RUN unzip $DOMIBUS_DIST/domibus-tomcat-full.zip -d $TOMCAT_FULL_DISTRIBUTION/
RUN unzip -j $DOMIBUS_DIST/domibus-sample-configuration-and-testing.zip conf/domibus/keystores/\* -d $CATALINA_HOME/conf/domibus/keystores
COPY $MYSQL_CONNECTOR_URL $CATALINA_HOME/lib/


RUN chmod 777 $CATALINA_HOME/bin/*.sh

RUN sed -i 's/#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>/export CATALINA_HOME=$CATALINA_HOME/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh

EXPOSE 8080


WORKDIR $CATALINA_HOME
ENTRYPOINT ["./bin/catalina.sh", "run"]