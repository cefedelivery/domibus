#!/bin/bash -ex

export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -q

######### Install MySql ##########

sudo debconf-set-selections <<< 'mysql-server-5.6 mysql-server/root_password password root' 
sudo debconf-set-selections <<< 'mysql-server-5.6 mysql-server/root_password_again password root' 
sudo apt-get -y install mysql-server-5.6

########## Create domibus db and user ########
mysql -h localhost -u root --password=root -e "drop schema if exists domibus; create schema domibus; alter database domibus charset=utf8; create user edelivery identified by 'edelivery'; grant all on domibus.* to edelivery;"


########## Install Domibus ########
#FROM java:7-jre

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
sudo wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && sudo tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

sudo wget https://dev.mysql.com/get/Downloads/Connector-J/$MYSQL_CONNECTOR.zip \
    && sudo unzip -o -d $DOMIBUS_DIST $MYSQL_CONNECTOR.zip

sudo cp $DOMIBUS_DIST/$MYSQL_CONNECTOR/$MYSQL_CONNECTOR-bin.jar $CATALINA_HOME/lib
sudo cp -R $DOMIBUS_DIST/conf/domibus/keystores $CATALINA_HOME/conf/domibus/

sudo chmod 777 $CATALINA_HOME/bin/*.sh
sudo sed -i 's/\r$//' $CATALINA_HOME/bin/setenv.sh
sudo sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh

########## Create domibus tables ########
mysql -h localhost -u root --password=root domibus < $TOMCAT_FULL_DISTRIBUTION/sql-scripts/mysql5innoDb-3.2.0.ddl

sudo sed -i 's/gateway_truststore.jks/ceftestparty8gwtruststore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/gateway_keystore.jks/ceftestparty8gwkeystore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/blue_gw/ceftestparty8gw/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo tar xzf data.tgz -C$CATALINA_HOME/conf/domibus/keystores/
sudo mv $CATALINA_HOME/conf/domibus/keystores/domibus-ceftestparty8gw-pmode.xml $DOMIBUS_DIST/conf/pmodes/
	
cd $CATALINA_HOME
sudo ./bin/catalina.sh start 

while ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus/home; do sleep 1 && echo -n .; done;

echo "[DEBUG] pMode ~~~~~~~~~~~~~~~~~~~ start ~~~~~~~~~"
sudo cat $DOMIBUS_DIST/conf/pmodes/domibus-gw-sample-pmode-blue.xml
echo "[DEBUG] pMode ~~~~~~~~~~~~~~~~~~~ end ~~~~~~~~~"


curl -X POST -F pmode=@"$DOMIBUS_DIST/conf/pmodes/domibus-ceftestparty8gw-pmode.xml" http://localhost:8080/domibus/home/uploadPmodeFile

#EXPOSE 8080

############ USEFUL COMANDS ###########

### Connectivity test machine IP: 40.115.23.114 ####
# sudo sed -i 's/<red_hostname>/40.115.23.114/g' $DOMIBUS_DIST/conf/pmodes/domibus-gw-sample-pmode-blue.xml 
# sudo sed -i 's/<blue_hostname>/localhost/g' $DOMIBUS_DIST/conf/pmodes/domibus-gw-sample-pmode-blue.xml 
#### Change pMode parties ####
# sudo sed -i 's/red_gw/cefsupportgw/g' $DOMIBUS_DIST/conf/pmodes/domibus-gw-sample-pmode-blue.xml 
# sudo sed -i 's/blue_gw/ceftestparty8gw/g' $DOMIBUS_DIST/conf/pmodes/domibus-gw-sample-pmode-blue.xml 

# sudo wget https://github.com/cefedelivery/domibus/blob/master/Domibus-MSH/src/test/resources/keystores/cefsupportgwtruststore.jks \
# 	&& sudo cp cefsupportgwtruststore.jks $CATALINA_HOME/conf/domibus/keystores/
 
# sudo wget https://github.com/cefedelivery/domibus/blob/master/Domibus-MSH/src/test/resources/keystores/ceftestparty8gw.jks \
# 	&& sudo cp ceftestparty8gw.jks $CATALINA_HOME/conf/domibus/keystores/

# sudo wget https://ec.europa.eu/cefdigital/code/projects/EDELIVERY/repos/domibus/browse/Domibus-MSH/src/main/conf/domibus/policies/signOnly.xml \
# 	&& sudo cp signOnly.xml $CATALINA_HOME/conf/domibus/policies/

# sudo wget https://ec.europa.eu/cefdigital/code/projects/EDELIVERY/repos/domibus/browse/Domibus-MSH/src/main/conf/domibus/policies/doNothingPolicy.xml \
# 	&& sudo cp doNothingPolicy.xml $CATALINA_HOME/conf/domibus/policies/
