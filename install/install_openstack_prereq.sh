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
# Author: Martin Skoviera

echo "Installing OpenStack prerequisites"
cd openstack
sudo ./package-installation.sh
#./db-setup.sh

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
cd ../../src/main/webapp/WEB-INF
mv configuration_openstack.txt configuration.txt
vi configuration.txt
