FROM mysql:5.7

MAINTAINER CEF eDelivery <CEF-EDELIVERY-SUPPORT@ec.europa.eu>

ARG SQLSCRIPTS_VERSION
ENV SQLSCRIPTS_VERSION ${SQLSCRIPTS_VERSION:-3.3}

ARG DOMIBUS_VERSION
ENV DOMIBUS_VERSION ${DOMIBUS_VERSION:-3.3-SNAPSHOT}

ENV SQL_SCRIPTS_URL ./domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip
ENV SQL_SCRIPTS_HOME /usr/local/domibusSql

RUN apt-get update
RUN apt-get install -y wget
RUN apt-get install unzip

COPY ./domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip .
RUN unzip  -d $SQL_SCRIPTS_HOME domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip
RUN cp $SQL_SCRIPTS_HOME/sql-scripts/mysql5innoDb-$SQLSCRIPTS_VERSION.ddl /docker-entrypoint-initdb.d/mysql5innoDb-$SQLSCRIPTS_VERSION.sql

EXPOSE 3306