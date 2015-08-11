#!/bin/bash
echo "---------------------------------------------------------------------------"
echo "| Setting up the database "
echo "---------------------------------------------------------------------------"
curl -X POST 'http://localhost:8086/cluster_admins?u=root&p=root' -d '{"name": "clusteradmin", "password": "changeit"}'
curl -X POST 'http://localhost:8086/db?u=clusteradmin&p=changeit' -d '{"name": "udr_service"}'
curl -X POST 'http://localhost:8086/db/udr_service/users?u=clusteradmin&p=changeit' -d '{"name": "dbadmin", "password": "changeit"}'
curl -X POST 'http://localhost:8086/db/udr_service/users/dbadmin?u=clusteradmin&p=changeit' -d '{"admin": true}'
curl -X POST 'http://localhost:8086/db/udr_service/users?u=dbadmin&p=changeit' -d '{"name":"meterselection","columns":["time","source","metersource","metertype","metername","status"],"points":[[1427101641000,"cyclops-ui","openstack","gauge","cpu_util",1]]}'
echo "---------------------------------------------------------------------------"
echo "| Installation process is complete "
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| if(all_Installations_Were_Successful_then){"
echo "|         Ready to Rock 'n Roll ! "
echo "|  } "
echo "---------------------------------------------------------------------------"