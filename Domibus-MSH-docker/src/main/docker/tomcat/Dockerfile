FROM java:7-jre

MAINTAINER CEF eDelivery <CEF-EDELIVERY-SUPPORT@ec.europa.eu>

ARG DEBIAN_FRONTEND=noninteractive

ARG DOMIBUS_VERSION
ENV DOMIBUS_VERSION ${DOMIBUS_VERSION}

####### BAMBOO_SHARE http://edelquality.westeurope.cloudapp.azure.com:8080/artifact/CE-DD/shared/build-$BUILD_NUMBER/Domibus/
ARG ARTIFACTS_REPOSITORY
ENV ARTIFACTS_REPOSITORY ${ARTIFACTS_REPOSITORY}

ARG SOURCE_CODE
ENV SOURCE_CODE ${SOURCE_CODE}

ARG PARTY
ENV PARTY ${PARTY:-blue}

RUN echo $PARTY

ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.0.24
ENV DOCKERIZE_VERSION v0.2.0

ENV MYSQL_CONNECTOR mysql-connector-java-5.1.40
ENV DOMIBUS_DIST /usr/local/domibusDist
ENV TOMCAT_FULL_DISTRIBUTION /usr/local/tomcat
ENV CATALINA_HOME $TOMCAT_FULL_DISTRIBUTION/domibus

ENV ADMIN_USER "admin"
ENV ADMIN_PASSW "123456"

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install -y unzip
RUN apt-get install -y curl

COPY ./domibus-distribution-$DOMIBUS_VERSION-tomcat-full.zip .
RUN unzip  -d $TOMCAT_FULL_DISTRIBUTION domibus-distribution-$DOMIBUS_VERSION-tomcat-full.zip

COPY ./domibus-distribution-$DOMIBUS_VERSION-sample-configuration-and-testing.zip .
RUN unzip -d $DOMIBUS_DIST domibus-distribution-$DOMIBUS_VERSION-sample-configuration-and-testing.zip

COPY ./policies .

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz
RUN wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && unzip -d $DOMIBUS_DIST $MYSQL_CONNECTOR.zip

RUN cp $DOMIBUS_DIST/$MYSQL_CONNECTOR/$MYSQL_CONNECTOR-bin.jar $CATALINA_HOME/lib

RUN echo "s/blue_gw/${PARTY}_gw/g"

RUN sed -i "s/blue_gw/${PARTY}_gw/g" $CATALINA_HOME/conf/domibus/domibus.properties

RUN chmod 777 $CATALINA_HOME/bin/*.sh
RUN sed -i 's/\r$//' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>/sleep 120;/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/4096/1024/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i "s/localhost:3306/mysql$PARTY:3306/g" $CATALINA_HOME/conf/domibus/domibus.properties
RUN sed -i "s/serverName=localhost/serverName=mysql$PARTY/g" $CATALINA_HOME/conf/domibus/domibus.properties
RUN echo "s/localhost:3306/mysql$PARTY:3306/g"

######## Set Test Platform certificates ########
RUN mkdir $CATALINA_HOME/conf/domibus/keystores
RUN cp $DOMIBUS_DIST/conf/domibus/keystores/* $CATALINA_HOME/conf/domibus/keystores/
RUN cp signOnly.xml $CATALINA_HOME/conf/domibus/policies/
RUN cp doNothingPolicy.xml $CATALINA_HOME/conf/domibus/policies/

RUN echo '#!/bin/bash' >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "cd $CATALINA_HOME" >> $DOMIBUS_DIST/entrypoint.sh
RUN echo './bin/catalina.sh start' >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "while true ; do sleep 1 ; done;" >> $DOMIBUS_DIST/entrypoint.sh
RUN chmod 777 $DOMIBUS_DIST/entrypoint.sh
EXPOSE 8080

WORKDIR $DOMIBUS_DIST
ENTRYPOINT ["./entrypoint.sh"]

