#!/bin/bash -ex

######## base image: base_ubuntu_14.04 ########

export JRE_HOME="/usr/java/jre1.7.0_79/"
sudo sed -i 's/127.0.0.1 localhost/127.0.0.1 localhost.localdomain $HOSTNAME/g' /etc/hosts

export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -q

######## Install MySql ########
sudo debconf-set-selections <<< 'mysql-server-5.6 mysql-server/root_password password root' 
sudo debconf-set-selections <<< 'mysql-server-5.6 mysql-server/root_password_again password root' 
sudo apt-get -y install mysql-server-5.6

########## Create domibus db and user ########
mysql -h localhost -u root --password=root -e "drop schema if exists domibus; create schema domibus; alter database domibus charset=utf8; create user edelivery identified by 'edelivery'; grant all on domibus.* to edelivery;"

########## Install Domibus ########
export DOMIBUS_VERSION="3.2"
export TOMCAT_MAJOR="8"
export TOMCAT_VERSION="8.0.24"
export MYSQL_CONNECTOR="mysql-connector-java-5.1.40"
export DOMIBUS_DIST="/usr/local/domibusDist"
export TOMCAT_FULL_DISTRIBUTION="/usr/local/tomcat"
export CATALINA_HOME="$TOMCAT_FULL_DISTRIBUTION/domibus"

sudo apt-get install -y wget
sudo apt-get install -y unzip

sudo wget https://ec.europa.eu/cefdigital/artifact/service/local/repositories/eDelivery/content/eu/domibus/domibus-MSH/$DOMIBUS_VERSION/domibus-MSH-$DOMIBUS_VERSION-tomcat-full.zip \
    && sudo unzip -o -d $TOMCAT_FULL_DISTRIBUTION domibus-MSH-$DOMIBUS_VERSION-tomcat-full.zip
sudo wget https://ec.europa.eu/cefdigital/artifact/service/local/repositories/eDelivery/content/eu/domibus/domibus-MSH/$DOMIBUS_VERSION/domibus-MSH-$DOMIBUS_VERSION-sample-configuration-and-testing.zip \
    && sudo unzip -o -d $DOMIBUS_DIST domibus-MSH-$DOMIBUS_VERSION-sample-configuration-and-testing.zip

sudo wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && sudo unzip -o -d $DOMIBUS_DIST $MYSQL_CONNECTOR.zip

sudo cp $DOMIBUS_DIST/$MYSQL_CONNECTOR/$MYSQL_CONNECTOR-bin.jar $CATALINA_HOME/lib
sudo cp -R $DOMIBUS_DIST/conf/domibus/keystores $CATALINA_HOME/conf/domibus/

sudo chmod 777 $CATALINA_HOME/bin/*.sh
sudo sed -i 's/\r$//' $CATALINA_HOME/bin/setenv.sh
sudo sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh

######## Create Domibus tables ########
mysql -h localhost -u root --password=root domibus < $TOMCAT_FULL_DISTRIBUTION/sql-scripts/mysql5innoDb-3.2.0.ddl

######## Set Test Platform certificates ########
sudo sed -i 's/gateway_truststore.jks/ceftestparty8gwtruststore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/gateway_keystore.jks/ceftestparty8gwkeystore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/blue_gw/ceftestparty8gw/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo tar xzf data.tgz -C$CATALINA_HOME/conf/domibus/keystores/
sudo mv $CATALINA_HOME/conf/domibus/keystores/domibus-ceftestparty8gw-pmode.xml $DOMIBUS_DIST/conf/pmodes/

######## Start Domibus ########
cd $CATALINA_HOME
sudo ./bin/catalina.sh start 

######## Wait for service to become available ########
while ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus/home; do sleep 1 && echo -n .; done;

######## Upload pMode ########
curl -X POST -F pmode=@"$DOMIBUS_DIST/conf/pmodes/domibus-ceftestparty8gw-pmode.xml" http://localhost:8080/domibus/home/uploadPmodeFile

######## Connectivity test machine IP: 40.115.23.114 ########
