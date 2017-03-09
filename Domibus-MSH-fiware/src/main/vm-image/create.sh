#!/bin/bash -ex

export DEBIAN_FRONTEND=noninteractive
sudo sed -i "s/127.0.0.1 localhost/127.0.0.1 localhost $(hostname)/g" /etc/hosts
sudo apt-get update -q
sudo apt-get install dialog

######### Install & configure JRE ########
sudo apt-get -y install default-jre

JAVA_HOME=$(sudo update-alternatives --config java | awk '{print $12}')

sudo echo >> /home/ubuntu/.bashrc
sudo echo "JAVA_HOME=$JAVA_HOME" >> /home/ubuntu/.bashrc

source /home/ubuntu/.bashrc

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

export ADMIN_USER="admin"
export ADMIN_PASSW="123456"

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
sudo sed -i 's/#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>/sleep 120;/g' $CATALINA_HOME/bin/setenv.sh
sudo sed -i 's/#JAVA_OPTS/JAVA_OPTS/g' $CATALINA_HOME/bin/setenv.sh

########## Create domibus tables ########
mysql -h localhost -u root --password=root domibus < $TOMCAT_FULL_DISTRIBUTION/sql-scripts/mysql5innoDb-3.2.0.ddl

sudo sed -i 's/gateway_truststore.jks/ceftestparty8gwtruststore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/gateway_keystore.jks/ceftestparty8gwkeystore.jks/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo sed -i 's/blue_gw/ceftestparty8gw/g' $CATALINA_HOME/conf/domibus/domibus-security.xml
sudo tar xzf data.tgz -C$CATALINA_HOME/conf/domibus/keystores/
sudo mv $CATALINA_HOME/conf/domibus/keystores/domibus-ceftestparty8gw-pmode.xml $DOMIBUS_DIST/conf/pmodes/

########## Configure Tomcat as a service ###########
sudo bash -c 'cat << EOF > /etc/init.d/domibus
# Domibus auto-start
#
# description: Auto-starts domibus
# processname: domibus(tomcat)
# pidfile: /var/run/tomcat.pid

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre

case \$1 in
        start)
            sh /usr/local/tomcat/domibus/bin/startup.sh
            ;;
        stop)
            sh /usr/local/tomcat/domibus/bin/shutdown.sh
            ;;
        restart)
            sh /usr/local/tomcat/domibus/bin/shutdown.sh
            sh /usr/local/tomcat/domibus/bin/startup.sh
            ;;
        esac
            exit 0
EOF'

sudo chmod 755 /etc/init.d/domibus

sudo ln -s /etc/init.d/domibus /etc/rc1.d/K99domibus
sudo ln -s /etc/init.d/domibus /etc/rc2.d/S99domibus

######### Start the service #########
cd $CATALINA_HOME
sudo service domibus start

while ! curl --output /dev/null --silent --head --fail http://localhost:8080/domibus/home; do sleep 1 && echo -n .; done;

curl --user $ADMIN_USER:$ADMIN_PASSW -X POST -F pmode=@"$DOMIBUS_DIST/conf/pmodes/domibus-ceftestparty8gw-pmode.xml" http://localhost:8080/domibus/home/uploadPmodeFile

