#!/bin/bash -ex

: ${3?"Usage: $0 SOURCE_CODE_PATH DOMIBUS_VERSION SQLSCRIPTS_VERSION"}

SOURCE_CODE=$1
DOMIBUS_VERSION=$2
SQLSCRIPTS_VERSION=$3

echo $SOURCE_CODE
echo $DOMIBUS_VERSION
echo $SQLSCRIPTS_VERSION

cp $SOURCE_CODE/target/domibus-MSH-$DOMIBUS_VERSION-sql-scripts.zip ./mysql/
cp $SOURCE_CODE/target/domibus-MSH-$DOMIBUS_VERSION-tomcat-full.zip ./tomcat/
cp $SOURCE_CODE/target/domibus-MSH-$DOMIBUS_VERSION-sample-configuration-and-testing.zip ./tomcat/
cp -R $SOURCE_CODE/src/main/conf/domibus/policies/ ./tomcat/policies

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/mysql
sudo docker build --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION --build-arg SQLSCRIPTS_VERSION=$SQLSCRIPTS_VERSION -t domibus/mysql:development .

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/tomcat
sudo docker build --build-arg PARTY=blue --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION -t domibus/tomcat/blue:development .
sudo docker build --build-arg PARTY=red --build-arg DOMIBUS_VERSION=$DOMIBUS_VERSION -t domibus/tomcat/red:development .

cd $SOURCE_CODE/../Domibus-MSH-docker/src/main/docker/tomcat-mysql-c2-c3-compose
sudo docker-compose up -d

cd $SOURCE_CODE/
i=0; until [ $i -eq 4 ] || [ curl --output /dev/null --silent --head --fail http://localhost:8180/domibus/home ]; do sleep $((i++)) && echo -n .; done;
i=0; until [ $i -eq 4 ] || [ curl --output /dev/null --silent --head --fail http://localhost:9080/domibus/home ]; do sleep $((i++)) && echo -n .; done;
mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test

cd $SOURCE_CODE/../Domibus-MSH-soapui-tests
cp src/main/soapui/domibus-gw-sample-pmode-blue.xml .
cp src/main/soapui/domibus-gw-sample-pmode-red.xml .
sed -i -e "s/localhost:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/localhost:8180/domibusred:8080/g" ./domibus-gw-sample-pmode-blue.xml
sed -i -e "s/localhost:8080/domibusblue:8080/g" ./domibus-gw-sample-pmode-red.xml
sed -i -e "s/localhost:8180/domibusred:8080/g" ./domibus-gw-sample-pmode-red.xml

curl -X POST -F pmode=@./domibus-gw-sample-pmode-blue.xml http://localhost:8180/domibus/home/uploadPmodeFile
curl -X POST -F pmode=@./domibus-gw-sample-pmode-red.xml http://localhost:9080/domibus/home/uploadPmodeFile

export MYSQL_CONNECTOR="mysql-connector-java-5.1.40"

sudo apt-get install -y wget
sudo apt-get install -y unzip
sudo wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && sudo unzip -o $MYSQL_CONNECTOR.zip

sudo cp ./$MYSQL_CONNECTOR-bin.jar ./src/main/soapui/lib

mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test
