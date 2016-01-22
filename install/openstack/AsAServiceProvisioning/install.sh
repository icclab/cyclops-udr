#!/bin/bash
rm -fR /etc/rabbitmq/ssl
rm -fR /etc/sensu/ssl
echo "---------------------------------------------------------------------------"
echo "| Installing SSL certificates"
echo "---------------------------------------------------------------------------"
wget http://sensuapp.org/docs/0.13/tools/ssl_certs.tar -P /tmp/udrservice
tar -xvf /tmp/udrservice/ssl_certs.tar -C /tmp/udrservice
cd /tmp/udrservice/ssl_certs && ./ssl_certs.sh generate
mkdir -p /etc/rabbitmq/ssl && cp /tmp/udrservice/ssl_certs/sensu_ca/cacert.pem /tmp/udrservice/ssl_certs/server/cert.pem /tmp/udrservice/ssl_certs/server/key.pem /etc/rabbitmq/ssl
mkdir /etc/sensu/ssl
cp /tmp/udrservice/ssl_certs/client/cert.pem /tmp/udrservice/ssl_certs/client/key.pem /etc/sensu/ssl
service rabbitmq-server restart
rabbitmqctl add_vhost /sensu
rabbitmqctl add_user sensu udrservice
rabbitmqctl set_permissions -p /sensu sensu ".*" ".*" ".*"
service sensu-server restart
service sensu-client restart
service sensu-api restart
service uchiwa restart
echo -e "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts
service tomcat7 restart
rm -fR /tmp/udrservice