#!/usr/bin/env bash

# general setup and tools
sudo apt-get update
sudo apt-get install -qy unzip git

# CERMINE dependencies
sudo apt-get install -qy default-jdk
sudo apt-get install -qy maven

sudo apt-get install -qy tomcat7 tomcat7-admin
sudo chgrp -R tomcat7 /etc/tomcat7
sudo chmod -R g+w /etc/tomcat7

# increase java heap size for Tomcat to enable CERMINE to run
sudo echo 'CATALINA_OPTS="-Xms512M  -Xmx1024M"' > /usr/share/tomcat7/bin/setenv.sh

# build the current version with maven
cd /vagrant
mvn install

# deploy to tomcat
cp /vagrant/cermine-web/target/cermine-web-1.5-SNAPSHOT.war /var/lib/tomcat7/webapps/cermine.war
sudo chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/cermine.war

# create user for tomcat manager interface
sudo chmod 777 /etc/tomcat7/tomcat-users.xml
sudo echo '<?xml version="1.0" encoding="utf-8"?>
<tomcat-users>
  <user username="tomcat" password="tomcat" roles="manager-gui,admin-gui"/>
</tomcat-users>' > /etc/tomcat7/tomcat-users.xml
sudo chmod 755 /etc/tomcat7/tomcat-users.xml
sudo service tomcat7 restart
