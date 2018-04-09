#!/usr/bin/env bash

# C2 - Tomcat (Red) -> C3 Wildfly (Blue)

#cd domibus/Domibus-MSH-soapui-tests
#sleep 500

export JDBC_DRIVERS=${bamboo.JDBC_DRIVERS}
export REPO=${bamboo.REPO}
export DOMIBUS_VERSION=${bamboo.DOMIBUS_VERSION}
export ORACLE_REPO=${bamboo.ORACLE_REPO}


PMODE_FILE_C2=./src/main/soapui/domibus-gw-sample-pmode-blue.xml
PMODE_FILE_C3=./src/main/soapui/domibus-gw-sample-pmode-red.xml
DOMIBUS_DISTRIBUTION=${bamboo_agentWorkingDirectory}/${bamboo.buildKey}/domibus/Domibus-MSH-distribution/target


cp ${JDBC_DRIVERS}/* ./src/main/soapui/lib

echo; echo "Getting IP addresses of Containers"
#DB_C2="`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testtctc_mysqlc2_1`"
#DOM_C2="`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testtctc_tomcatc2_1`"

#DB_C3="`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testtctc_mysqlc3_1`"
#DOM_C3="`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testtctc_tomcatc3_1`"

../../docker/runTests/prepareForTests.sh ${PMODE_FILE_C2} ${PMODE_FILE_C3} \
http://localhost:8082/domibus http://localhost:8083/domibus

../../docker/runTests/runTests.sh \
 ${DOMIBUS_DISTRIBUTION} \
"localUrl=http://localhost:8082/domibus" \
"remoteUrl=http://localhost:8083/domibus" \
"jdbcUrlBlue=jdbc:mysql://localhost:3406/domibus" \
"jdbcUrlRed=jdbc:mysql://localhost:3506/domibus" \
"driverBlue=com.mysql.jdbc.Driver" \
"driverRed=com.mysql.jdbc.Driver" \
"databaseBlue=mysql" \
"databaseRed=mysql" \
"blueDbUser=root" \
"blueDbPassword=123456" \
"redDbUser=root" \
"redDbPassword=123456" \
"6401" \
"localhost" \
"6402" \
"localhost" \
