#!/bin/bash
echo "deb http://www.rabbitmq.com/debian/ testing main" | tee -a /etc/apt/sources.list.d/rabbitmq.list
curl -L -o ~/rabbitmq-signing-key-public.asc http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add ~/rabbitmq-signing-key-public.asc
wget -q http://repos.sensuapp.org/apt/pubkey.gpg -O- | apt-key add -
echo "deb  http://repos.sensuapp.org/apt sensu main" | tee -a /etc/apt/sources.list.d/sensu.list
apt-get update
echo "---------------------------------------------------------------------------"
echo "| Installing the Java openjdk-7-jre"
echo "| Java 7 is the baseline Java version"
echo "---------------------------------------------------------------------------"
apt-get install -y openjdk-7-jre
apt-get install -y openjdk-7-jdk
echo "---------------------------------------------------------------------------"
echo "| Installing Ruby, ruby-dev and Ruby build-essential"
echo "---------------------------------------------------------------------------"
apt-get install -y ruby ruby-dev build-essential
echo "---------------------------------------------------------------------------"
echo "| Installing Sensu plugin to support the check scricpts ¦ sensu-plugin & rest_client"
echo "---------------------------------------------------------------------------"
gem install sensu-plugin
gem install rest_client
echo "---------------------------------------------------------------------------"
echo "| Installing Ant and Git"
echo "---------------------------------------------------------------------------"
apt-get install -y ant
apt-get install -y git-core
echo "---------------------------------------------------------------------------"
echo "| Installing curl"
echo "| Dependent packages - libc6 libcurl3 zlib1g "
echo "---------------------------------------------------------------------------"
apt-get install -y curl
echo "---------------------------------------------------------------------------"
echo "| Installing Apache Server"
echo "---------------------------------------------------------------------------"
apt-get install -y apache2
echo "---------------------------------------------------------------------------"
echo "| Installing Redis"
echo "---------------------------------------------------------------------------"
apt-get -y install redis-server
echo "---------------------------------------------------------------------------"
echo "| Installing tomcat7 and tomcat7-admin"
echo "---------------------------------------------------------------------------"
apt-get install -y tomcat7
apt-get install -y tomcat7-admin
echo "---------------------------------------------------------------------------"
echo "| Installing the latest release of InfluxDB"
echo "---------------------------------------------------------------------------"
mkdir -p /tmp/udrservice
wget http://s3.amazonaws.com/influxdb/influxdb_latest_amd64.deb -P /tmp/udrservice
echo "---------------------------------------------------------------------------"
echo "| Decompressing the package"
echo "---------------------------------------------------------------------------"
dpkg -i /tmp/udrservice/influxdb_latest_amd64.deb
echo "---------------------------------------------------------------------------"
echo "| Starting InfluxDB"
echo "---------------------------------------------------------------------------"
/etc/init.d/influxdb restart
echo "---------------------------------------------------------------------------"
echo "| Downloading Grafana package"
echo "---------------------------------------------------------------------------"
wget "http://grafanarel.s3.amazonaws.com/grafana-1.9.1.tar.gz" -P /tmp/udrservice
echo "---------------------------------------------------------------------------"
echo "| unzip Grafana package"
echo "---------------------------------------------------------------------------"
tar -xvzf /tmp/udrservice/grafana-1.9.1.tar.gz -C /tmp/udrservice
echo "---------------------------------------------------------------------------"
echo "| Creating Grafana directory at /usr/share/"
echo "| Creating v1.9.0_rc1 directory at /usr/share/grafana"
echo "---------------------------------------------------------------------------"
mkdir -p /usr/share/grafana/v1.9.1
echo "---------------------------------------------------------------------------"
echo "| Copying the contents of grafana package to /usr/share/grafana/v1.9.0_rc1"
echo "---------------------------------------------------------------------------"
cp -r /tmp/udrservice/grafana-1.9.1/* /usr/share/grafana/v1.9.1
echo "---------------------------------------------------------------------------"
echo "| Moving the Grafana dashboard JSON file"
echo "---------------------------------------------------------------------------"
rm /usr/share/grafana/v1.9.1/app/dashboards/default.json
cp ./config/default.json /usr/share/grafana/v1.9.1/app/dashboards/
echo "---------------------------------------------------------------------------"
echo "| Moving the Grafana config file at /usr/share/grafana"
echo "---------------------------------------------------------------------------"
cp ./config/config.js /usr/share/grafana/v1.9.1/config.js
sed -i s/localhost/$1/ /usr/share/grafana/v1.9.1/config.js
echo "---------------------------------------------------------------------------"
echo "| Creating an Apache config file to refer grafana at /etc/apache2/sites-enabled/"
echo "---------------------------------------------------------------------------"
cat > /etc/apache2/sites-enabled/grafana.conf << EOF
alias /grafana /usr/share/grafana/v1.9.1
EOF
echo "---------------------------------------------------------------------------"
echo "| Starting the process of installing Sensu"
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| Installing RabbitMQ"
echo "---------------------------------------------------------------------------"
apt-get install -y rabbitmq-server erlang-nox
echo "---------------------------------------------------------------------------"
echo "| Installing SSL certificates"
echo "---------------------------------------------------------------------------"
wget http://sensuapp.org/docs/0.13/tools/ssl_certs.tar -P /tmp/udrservice
tar -xvf /tmp/udrservice/ssl_certs.tar -C /tmp/udrservice
CURRDIR=`pwd`
cd /tmp/udrservice/ssl_certs && ./ssl_certs.sh generate
mkdir -p /etc/rabbitmq/ssl && cp /tmp/udrservice/ssl_certs/sensu_ca/cacert.pem /tmp/udrservice/ssl_certs/server/cert.pem /tmp/udrservice/ssl_certs/server/key.pem /etc/rabbitmq/ssl
echo "---------------------------------------------------------------------------"
echo "| Creating RabbitMQ Config file"
echo "---------------------------------------------------------------------------"
cat > /etc/rabbitmq/rabbitmq.config << EOF
[
    {rabbit, [
    {ssl_listeners, [5671]},
    {ssl_options, [{cacertfile,"/etc/rabbitmq/ssl/cacert.pem"},
                   {certfile,"/etc/rabbitmq/ssl/cert.pem"},
                   {keyfile,"/etc/rabbitmq/ssl/key.pem"},
                   {verify,verify_peer},
                   {fail_if_no_peer_cert,true}]}
  ]}
].
EOF
echo "---------------------------------------------------------------------------"
echo "| Restarting RabbitMQ"
echo "---------------------------------------------------------------------------"
service rabbitmq-server restart
echo "---------------------------------------------------------------------------"
echo "| Adding virtual hosts to RabbitMQ"
echo "---------------------------------------------------------------------------"
rabbitmqctl add_vhost /sensu
rabbitmqctl add_user sensu udrservice
rabbitmqctl set_permissions -p /sensu sensu ".*" ".*" ".*"
echo "---------------------------------------------------------------------------"
echo "| Installing Sensu and Uchiwa, copying the ssl certificates to /etc/sensu/ssl"
echo "---------------------------------------------------------------------------"
apt-get install -y sensu uchiwa
mkdir -p /etc/sensu/ssl && cp /tmp/udrservice/ssl_certs/client/cert.pem /tmp/udrservice/ssl_certs/client/key.pem /etc/sensu/ssl
echo "---------------------------------------------------------------------------"
echo "| Sensu configuration begins"
echo "| Creating the RabbitMQ config file - Port 5671"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/rabbitmq.json << EOF
{
  "rabbitmq": {
    "ssl": {
      "cert_chain_file": "/etc/sensu/ssl/cert.pem",
      "private_key_file": "/etc/sensu/ssl/key.pem"
    },
    "host": "localhost",
    "port": 5671,
    "vhost": "/sensu",
    "user": "sensu",
    "password": "udrservice"
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Redis config file - Port 6379"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/redis.json << EOF
{
  "redis": {
    "host": "localhost",
    "port": 6379
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the sensu API config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/api.json << EOF
{
  "api": {
    "host": "localhost",
    "port": 4567
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Uchiwa config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/uchiwa.json << EOF
{
    "sensu": [
        {
            "name": "Sensu",
            "host": "localhost",
            "ssl": false,
            "port": 4567,
            "path": "",
            "timeout": 5000
        }
    ],
    "uchiwa": {
        "port": 3000,
        "stats": 10,
        "refresh": 10000
    }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| [Temporary Fix] Creating the Uchiwa config file - Port 4567"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/uchiwa.json << EOF
{
    "sensu": [
        {
            "name": "Sensu",
            "host": "localhost",
            "ssl": false,
            "port": 4567,
            "path": "",
            "timeout": 5000
        }
    ],
    "uchiwa": {
        "port": 3000,
        "stats": 10,
        "refresh": 10000
    }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Sensu client config file"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/client.json << EOF
{
  "client": {
    "name": "server",
    "address": "localhost",
    "subscriptions": [ "ALL" ]
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the Ceilometer check config file - Interval 60 mins"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/conf.d/check_udrservice.json << EOF
{
  "checks": {
    "udrservice_check": {
      "command": "/etc/sensu/plugins/check-udrservice.rb",
      "interval": 60,
      "subscribers": [ "ALL" ]
    }
  }
}
EOF
echo "---------------------------------------------------------------------------"
echo "| Creating the UDR service ping script"
echo "---------------------------------------------------------------------------"
cat > /etc/sensu/plugins/check-udrservice.rb << EOF
#!/usr/bin/env ruby
#
# Checks etcd node self stats
# ===
#
# DESCRIPTION:
#   This script pings the UDR service to trigger the data collection API
#
# OUTPUT:
#   plain-text
#
# PLATFORMS:
#   all
#
# DEPENDENCIES:
#   sensu-plugin Ruby gem
#   rest_client Ruby gem
#

require 'rubygems' if RUBY_VERSION < '1.9.0'
require 'sensu-plugin/check/cli'
require 'rest-client'
require 'json'

class PingUDRService < Sensu::Plugin::Check::CLI
  def run
    begin
      r = RestClient::Resource.new("http://localhost:8080/udr/api", :timeout => 60 ).get
      if r.code == 200
        ok "UDR service ping was successfull"
      else
        critical "Oops!"
      end
    end
  end
end
EOF
echo "---------------------------------------------------------------------------"
echo "| Setting the chmod status to 755 on check-udrservice.rb"
echo "---------------------------------------------------------------------------"
sudo chmod 755 /etc/sensu/plugins/check-udrservice.rb
echo "---------------------------------------------------------------------------"
echo "| Updating sensu-server sensu-client sensu-api uchiwa"
echo "---------------------------------------------------------------------------"
update-rc.d sensu-server defaults
update-rc.d sensu-client defaults
update-rc.d sensu-api defaults
update-rc.d uchiwa defaults
echo "---------------------------------------------------------------------------"
echo "| Starting sensu-server sensu-client sensu-api uchiwa"
echo "---------------------------------------------------------------------------"
service sensu-server restart
service sensu-client restart
service sensu-api restart
service uchiwa restart
echo "---------------------------------------------------------------------------"
echo "| Adding a manager for Tomcat "
echo "| user : admin ¦ password: w<>150<^T*F~0B "
echo "---------------------------------------------------------------------------"
cat > /etc/tomcat7/tomcat-users.xml << EOF
<?xml version='1.0' encoding='utf-8'?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!--
  NOTE:  By default, no user is included in the "manager-gui" role required
  to operate the "/manager/html" web application.  If you wish to use this app,
  you must define such a user - the username and password are arbitrary.
-->
<!--
  NOTE:  The sample user and role entries below are wrapped in a comment
  and thus are ignored when reading this file. Do not forget to remove
  <!.. ..> that surrounds them.
-->
<!--
  <role rolename="tomcat"/>
  <role rolename="role1"/>
  <user username="tomcat" password="tomcat" roles="tomcat"/>
  <user username="both" password="tomcat" roles="tomcat,role1"/>
  <user username="role1" password="tomcat" roles="role1"/>
-->
<tomcat-users>
    <user username="admin" password="wO)150<^T*F~0B" roles="manager-gui"/>
</tomcat-users>
EOF
echo "---------------------------------------------------------------------------"
echo "| Triggering the build for UDR Service and creation of WAR file "
echo "---------------------------------------------------------------------------"
cd ${CURRDIR}
cd ..
ant
echo "---------------------------------------------------------------------------"
echo "| Deploying the WAR file to the Tomcat "
echo "---------------------------------------------------------------------------"
cd out
cp udr.war /var/lib/tomcat7/webapps
echo "---------------------------------------------------------------------------"
echo "| Restarting Tomcat "
echo "---------------------------------------------------------------------------"
service tomcat7 restart
echo "---------------------------------------------------------------------------"
echo "| Copying the bill generation project to Apache "
echo "---------------------------------------------------------------------------"
cd ..
mkdir -p /var/www/html/udr/bill
cp -r bill/* /var/www/html/udr/bill
sed -i s/localhost/$1/ /var/www/html/udr/bill/index.html
echo "---------------------------------------------------------------------------"
echo "| Starting the Apache Server"
echo "---------------------------------------------------------------------------"
service apache2 restart
echo "---------------------------------------------------------------------------"
echo "| Starting Redis"
echo "---------------------------------------------------------------------------"
service redis-server restart
echo "---------------------------------------------------------------------------"
echo "| Removing the temp/udrservice folder"
echo "---------------------------------------------------------------------------"
rm -fR /tmp/udrservice