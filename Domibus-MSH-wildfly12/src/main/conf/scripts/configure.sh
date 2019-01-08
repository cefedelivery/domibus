#!/bin/bash

########################################################################################################################
########################## The following properties need to be modified by the users ###################################
########################################################################################################################

# The location where the Wildfly12 instance is installed
JBOSS_HOME=/path/to/wildfly12

# The name of the standalone configuration file that need to be updated: standalone-full.xml for a
# non-clustered Wildfly12 environment and standalone-full-ha.xml for a clustered one.
SERVER_CONFIG=standalone-full.xml
#SERVER_CONFIG=standalone-full-ha.xml

# MySQL configuration
DB_TYPE=MySQL
DB_HOST=localhost
DB_NAME=domibus?autoReconnect=true\&useSSL=false
DB_PORT=3306
DB_USER=edelivery
DB_PASS=edelivery
JDBC_CONNECTION_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
JDBC_DRIVER_DIR=${JBOSS_HOME}/modules/system/layers/base/com/mysql/main
JDBC_DRIVER_NAME=mysql-connector-java-5.1.46.jar

# Oracle configuration
#DB_TYPE=Oracle
#DB_HOST=localhost
#DB_PORT=1521
#DB_USER=edelivery_user
#DB_PASS=edelivery_password
#JDBC_CONNECTION_URL="jdbc:oracle:thin:@${DB_HOST}:${DB_PORT}[:SID|/Service]"
#JDBC_DRIVER_DIR=${JBOSS_HOME}/modules/system/layers/base/com/oracle/main
#JDBC_DRIVER_NAME=ojdbc6-12.1.0.2.jar

########################################################################################################################
############################ The following part is not to be modified by the users #####################################
########################################################################################################################

echo "--------------JBOSS_HOME: ${JBOSS_HOME}"
echo "--------------SERVER_CONFIG: ${SERVER_CONFIG}"
echo "--------------DB_TYPE: ${DB_TYPE}"
echo "--------------DB_HOST: ${DB_HOST}"
echo "--------------DB_NAME: ${DB_NAME}"
echo "--------------DB_PORT: ${DB_PORT}"
echo "--------------DB_USER: ${DB_USER}"
echo "--------------DB_PASS: ${DB_PASS}"
echo "--------------JDBC_CONNECTION_URL: ${JDBC_CONNECTION_URL}"
echo "--------------JDBC_DRIVER_DIR: ${JDBC_DRIVER_DIR}"
echo "--------------JDBC_DRIVER_NAME: ${JDBC_DRIVER_NAME}"

echo "--------------Configure Wildfly12 to resolve parameter values from properties files"
sed -i "s/<resolve-parameter-values>false<\/resolve-parameter-values>/\
<resolve-parameter-values>true<\/resolve-parameter-values>/" \
$JBOSS_HOME/bin/jboss-cli.xml

echo "--------------Prepare"
export JBOSS_HOME SERVER_CONFIG DB_TYPE DB_HOST DB_NAME DB_PORT DB_PORT DB_USER DB_PASS JDBC_CONNECTION_URL JDBC_DRIVER_DIR JDBC_DRIVER_NAME
printenv > env.properties

echo "--------------Configure Wildfly12"
if [ "${SERVER_CONFIG}" == "standalone-full-ha.xml" ] ; then
	${JBOSS_HOME}/bin/jboss-cli.sh --file=resources/domibus-configuration-${DB_TYPE}-cluster.cli --properties=env.properties
else
	${JBOSS_HOME}/bin/jboss-cli.sh --file=resources/domibus-configuration-${DB_TYPE}.cli --properties=env.properties
fi

echo "--------------Clean"
rm env.properties
