FROM mysql:5.7

MAINTAINER Cosmin Baciu <cosmin.baciu@ext.ec.europa.eu>

ENV SQL_SCRIPTS_URL ./domibus-sql-scripts.zip
ENV SQL_SCRIPTS_HOME /usr/local/domibusSql
ENV DOMIBUS_VERSION 3.1.1

COPY $SQL_SCRIPTS_URL $SQL_SCRIPTS_HOME/

RUN apt-get update
RUN apt-get install unzip

RUN unzip $SQL_SCRIPTS_HOME/domibus-sql-scripts.zip -d $SQL_SCRIPTS_HOME/
RUN cp $SQL_SCRIPTS_HOME/sql-scripts/mysql5innoDb-$DOMIBUS_VERSION.ddl /docker-entrypoint-initdb.d/mysql5innoDb-$DOMIBUS_VERSION.sql
