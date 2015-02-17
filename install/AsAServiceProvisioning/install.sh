#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No arguments supplied, expected public IP of this node"
    echo "Usage sudo ./install.sh [public IP]"
    exit 1
fi
chmod +x /var/www/html/udr/bill/index.html
chmod +x /usr/share/grafana/v1.9.0_rc1/config.js
sed -i s/localhost/$1/ /var/www/html/udr/bill/index.html
sed -i s/localhost/$1/ /usr/share/grafana/v1.9.0_rc1/config.js
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
service apache2 restart
service sensu-server restart
service sensu-client restart
service sensu-api restart
service uchiwa restart
service tomcat7 restart
rm -fR /tmp/udrservice