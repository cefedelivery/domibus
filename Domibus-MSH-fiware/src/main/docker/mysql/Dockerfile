FROM mysql:5.7

MAINTAINER CEF eDelivery <CEF-EDELIVERY-SUPPORT@ec.europa.eu>

ENV DOMIBUS_VERSION_MINOR .0
ENV DOMIBUS_VERSION 3.2
ENV SQL_SCRIPTS_URL ./domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip
ENV SQL_SCRIPTS_HOME /usr/local/domibusSql

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install unzip

RUN wget https://ec.europa.eu/cefdigital/artifact/content/repositories/public/eu/domibus/domibus-MSH/$DOMIBUS_VERSION/domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip \
    && unzip  -d $SQL_SCRIPTS_HOME domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip

RUN cp $SQL_SCRIPTS_HOME/sql-scripts/mysql5innoDb-$DOMIBUS_VERSION$DOMIBUS_VERSION_MINOR.ddl /docker-entrypoint-initdb.d/mysql5innoDb-$DOMIBUS_VERSION$DOMIBUS_VERSION_MINOR.sql

EXPOSE 3306