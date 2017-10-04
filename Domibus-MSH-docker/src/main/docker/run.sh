#!/bin/bash -ex

: ${3?"Usage: $0 SOURCE_CODE_PATH DOMIBUS_VERSION SQLSCRIPTS_VERSION"}

SOURCE_CODE=$1
DOMIBUS_VERSION=$2
SQLSCRIPTS_VERSION=$3

echo $SOURCE_CODE
echo $DOMIBUS_VERSION
echo $SQLSCRIPTS_VERSION

ADMIN_USER="admin"
ADMIN_PASSW="123456"

cp $SOURCE_CODE/target/domibus-distribution-$DOMIBUS_VERSION-sql-scripts.zip ./mysql/
cp $SOURCE_CODE/target/domibus-distribution-$DOMIBUS_VERSION-tomcat-full.zip ./tomcat/
cp $SOURCE_CODE/target/domibus-distribution-$DOMIBUS_VERSION-sample-configuration-and-testing.zip ./tomcat/
cp -R $SOURCE_CODE/../Domibus-MSH-tomcat/src/test/resources/policies/ ./tomcat/policies

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/mysql
sudo docker build --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION --build-arg SQLSCRIPTS_VERSION=$SQLSCRIPTS_VERSION -t domibus/mysql:development .

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/tomcat
sudo docker build --build-arg PARTY=blue --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION -t domibus/tomcat/blue:development .
sudo docker build --build-arg PARTY=red --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION -t domibus/tomcat/red:development .

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/tomcat-mysql-c2-c3-compose
sudo docker-compose up -d

cd $SOURCE_CODE/../Domibus-MSH-tomcat
i=0; while ! curl --output /dev/null --silent --head --fail http://localhost:8180/domibus/; do sleep $((i++)) && echo -n . && if [ $i -eq 100 ]; then break; fi ;   done
i=0; while ! curl --output /dev/null --silent --head --fail http://localhost:9080/domibus/; do sleep $((i++)) && echo -n . && if [ $i -eq 100 ]; then break; fi ;   done
cp ../Domibus-MSH/src/main/conf/pmodes/domibus-gw-sample-pmode-blue.xml .
cp ../Domibus-MSH/src/main/conf/pmodes/domibus-gw-sample-pmode-red.xml .
sed -i -e "s/<blue_hostname>:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/<red_hostname>:8080/domibusred:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/<blue_hostname>:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-red.xml
sed -i -e "s/<red_hostname>:8080/domibusred:8080/g" ./domibus-gw-sample-pmode-red.xml
$SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/uploadPmode.sh localhost:8180 $SOURCE_CODE/../Domibus-MSH-tomcat/domibus-gw-sample-pmode-blue.xml
$SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/uploadPmode.sh localhost:9080 $SOURCE_CODE/../Domibus-MSH-tomcat/domibus-gw-sample-pmode-red.xml

localUrl="localUrl=http://localhost:8180/domibus"
remoteUrl="remoteUrl=http://localhost:9080/domibus"

mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test -DlocalUrl="$localUrl" -DremoteUrl="$remoteUrl" #-DjdbcUrlBlue="$jdbcUrlBlue" -DjdbcUrlRed="$jdbcUrlRed"

cd $SOURCE_CODE/../Domibus-MSH-soapui-tests
cp src/main/soapui/domibus-gw-sample-pmode-blue.xml .
cp src/main/soapui/domibus-gw-sample-pmode-red.xml .
sed -i -e "s/localhost:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/localhost:8180/domibusred:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/localhost:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-red.xml
sed -i -e "s/localhost:8180/domibusred:8080/g" ./domibus-gw-sample-pmode-red.xml


$SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/uploadPmode.sh localhost:8180 $SOURCE_CODE/../Domibus-MSH-soapui-tests/domibus-gw-sample-pmode-blue.xml
$SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/uploadPmode.sh localhost:9080 $SOURCE_CODE/../Domibus-MSH-soapui-tests/domibus-gw-sample-pmode-red.xml


MYSQL_CONNECTOR="mysql-connector-java-5.1.40"

echo $MYSQL_CONNECTOR

sudo apt-get install -y wget
sudo apt-get install -y unzip
sudo wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && sudo unzip -o $MYSQL_CONNECTOR.zip

sudo cp $MYSQL_CONNECTOR/$MYSQL_CONNECTOR-bin.jar ./src/main/soapui/lib

localUrl="localUrl=http://localhost:8180/domibus"
remoteUrl="remoteUrl=http://localhost:9080/domibus"
jdbcUrlBlue="jdbcUrlBlue=jdbc:mysql://127.0.0.1:3304/domibus"
jdbcUrlRed="jdbcUrlRed=jdbc:mysql://127.0.0.1:3302/domibus"

driverBlue="driverBlue=com.mysql.jdbc.Driver"
driverRed="driverRed=com.mysql.jdbc.Driver"
databaseBlue="databaseBlue=mysql"
databaseRed="databaseRed=mysql"
blueDbUser="blueDbUser=root"
blueDbPassword="blueDbPassword=123456"
redDbUser="redDbUser=root"
redDbPassword="redDbPassword=123456"


#mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test -DlocalUrl="$localUrl" -DremoteUrl="$remoteUrl" -DjdbcUrlBlue="$jdbcUrlBlue" -DjdbcUrlRed="$jdbcUrlRed"

mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test -DlocalUrl="$localUrl" -DremoteUrl="$remoteUrl" -DjdbcUrlBlue="$jdbcUrlBlue" -DjdbcUrlRed="$jdbcUrlRed" -DdriverBlue="$driverBlue" -DdriverRed="$driverRed" -DdatabaseBlue="$databaseBlue" -DdatabaseRed="$databaseRed" -DblueDbUser="$blueDbUser" -DblueDbPassword="$blueDbPassword" -DredDbUser="$redDbUser" -DredDbPassword="$redDbPassword"
