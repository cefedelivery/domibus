FROM java:7-jre

MAINTAINER CEF eDelivery <CEF-EDELIVERY-SUPPORT@ec.europa.eu>

ENV DOMIBUS_VERSION 3.2

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
RUN apt-get install unzip
RUN apt-get install curl

RUN wget https://ec.europa.eu/cefdigital/artifact/content/repositories/public/eu/domibus/domibus-MSH/$DOMIBUS_VERSION/domibus-MSH-$DOMIBUS_VERSION-tomcat-full.zip \
    && unzip -d $TOMCAT_FULL_DISTRIBUTION domibus-MSH-$DOMIBUS_VERSION-tomcat-full.zip
RUN wget https://ec.europa.eu/cefdigital/artifact/content/repositories/public/eu/domibus/domibus-MSH/$DOMIBUS_VERSION/domibus-MSH-$DOMIBUS_VERSION-sample-configuration-and-testing.zip \
    && unzip -d $DOMIBUS_DIST domibus-MSH-$DOMIBUS_VERSION-sample-configuration-and-testing.zip
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

RUN wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && unzip -d $DOMIBUS_DIST $MYSQL_CONNECTOR.zip

RUN cp $DOMIBUS_DIST/$MYSQL_CONNECTOR/$MYSQL_CONNECTOR-bin.jar $CATALINA_HOME/lib

RUN chmod 777 $CATALINA_HOME/bin/*.sh
RUN sed -i 's/\r$//' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>/sleep 120;/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh
RUN sed -i 's/localhost:3306/mysql:3306/g' $CATALINA_HOME/conf/domibus/domibus-datasources.xml

######## Set Test Platform certificates ########
RUN sed -i 's/gateway_truststore.jks/ceftestparty8gwtruststore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
RUN sed -i 's/gateway_keystore.jks/ceftestparty8gwkeystore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
RUN sed -i 's/blue_gw/ceftestparty8gw/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
COPY domibus-ceftestparty8gw-pmode.xml $DOMIBUS_DIST/conf/pmodes/
RUN mkdir $CATALINA_HOME/conf/domibus/keystores

RUN wget -O ceftestparty8gwtruststore.jks 'https://github.com/cefedelivery/domibus/blob/master/Domibus-MSH/src/test/resources/keystores/ceftestparty8gwtruststore.jks?raw=true'
RUN cp ceftestparty8gwtruststore.jks $CATALINA_HOME/conf/domibus/keystores/

RUN wget -O ceftestparty8gwkeystore.jks 'https://github.com/cefedelivery/domibus/blob/master/Domibus-MSH/src/test/resources/keystores/ceftestparty8gwkeystore.jks?raw=true'
RUN cp ceftestparty8gwkeystore.jks $CATALINA_HOME/conf/domibus/keystores/

RUN echo '#!/bin/bash' >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "cd $CATALINA_HOME" >> $DOMIBUS_DIST/entrypoint.sh
RUN echo './bin/catalina.sh start' >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "while ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus/home; do sleep 1 && echo -n .; done;" >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "curl --user $ADMIN_USER:$ADMIN_PASSW -X POST -F pmode=@\"$DOMIBUS_DIST/conf/pmodes/domibus-ceftestparty8gw-pmode.xml\" http://localhost:8080/domibus/home/uploadPmodeFile" >> $DOMIBUS_DIST/entrypoint.sh
RUN echo "while true ; do sleep 1 ; done;" >> $DOMIBUS_DIST/entrypoint.sh
RUN chmod 777 $DOMIBUS_DIST/entrypoint.sh
EXPOSE 8080

WORKDIR $DOMIBUS_DIST
ENTRYPOINT ["./entrypoint.sh"]

