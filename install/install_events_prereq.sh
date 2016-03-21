#!/bin/bash
# Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
# All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# Author: Piyush Harsh,
# URL: piyush-harsh.info

echo "Installing Prerequisites"
### Installing Sensu and Uchiwa ###
wget http://packages.erlang-solutions.com/erlang-solutions_1.0_all.deb
sudo dpkg -i erlang-solutions_1.0_all.deb

sudo apt-get update
sudo apt-get install -y erlang
sudo apt-get -y install rabbitmq-server
sudo update-rc.d rabbitmq-server defaults
sudo /etc/init.d/rabbitmq-server restart
wget http://localhost:15672/cli/rabbitmqadmin
chmod +x rabbitmqadmin
sudo mv rabbitmqadmin /usr/bin/
##########################################
# please change the rabbitmq administrator account password
##########################################
#sudo rabbitmqctl change_password guest pass1234

### You can test uchiwa dashboard at IP:3000/ ###
### Installing InfluxDB ###
wget http://influxdb.s3.amazonaws.com/influxdb_0.9.4.1_amd64.deb
sudo dpkg -i influxdb_0.9.4.1_amd64.deb
sudo /etc/init.d/influxdb restart

### Installing Java ###
sudo apt-get -y install python-software-properties
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo apt-get -y install oracle-java7-installer

cat << EOF | sudo tee -a /etc/environment
JAVA_HOME="/usr/lib/jvm/java-7-oracle"
EOF

sudo apt-get -y install maven

### Installing tomcat7 and admin settings ###
### based on the guide from digitalocean.com ###
sudo apt-get -y install tomcat7
sudo apt-get -y install tomcat7-docs tomcat7-admin tomcat7-examples
sudo apt-get -y install ant git

cat << EOF | sudo tee -a /etc/default/tomcat7
JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC -Djava.security.egd=file:/dev/./urandom"
EOF

cat << EOF | sudo tee /etc/tomcat7/tomcat-users.xml
<tomcat-users>
    <user username="admin" password="pass1234" roles="manager-gui,admin-gui"/>
</tomcat-users>
EOF

sudo service tomcat7 restart

### installing ruby for sensu scripts for microservices ###
# sudo apt-get install -y ruby ruby-dev build-essential
# sudo gem install sensu-plugin
# sudo /opt/sensu/embedded/bin/gem install sensu-plugin

sudo apt-get -y install curl libcurl3 libcurl3-dev zip unzip sqlite

### Cleaning up temp files ###
rm uchiwa_0.10.4-1_amd64.deb
rm erlang-solutions_1.0_all.deb
rm influxdb_0.9.4.1_amd64.deb

source /etc/environment

echo "Installing InfluxDB Java client"
git clone https://github.com/influxdb/influxdb-java.git
cd influxdb-java
rm -fR src/test
mvn clean install -DskipTests=true
cd ..
rm -fR influxdb-java

echo "Configuring logging folder structure"
sudo mkdir -p /var/log/cyclops/
sudo chmod 777 /var/log/cyclops/

touch /var/log/cyclops/udr.log
sudo chmod 777 /var/log/cyclops/udr.log

echo "Preparing configuration file"
cd ../src/main/webapp/WEB-INF
mv configuration_events.txt configuration.txt
vi configuration.txt

sudo -k 