#!/bin/bash
echo "---------------------------------------------------------------------------"
echo "| Setting up the database "
echo "---------------------------------------------------------------------------"
curl -X POST 'http://localhost:8086/cluster_admins?u=root&p=root' -d '{"name": "clusteradmin", "password": "changeit"}'
curl -X POST 'http://localhost:8086/db?u=clusteradmin&p=changeit' -d '{"name": "udr_service"}'
curl -X POST 'http://localhost:8086/db/udr_service/users?u=clusteradmin&p=changeit' -d '{"name": "dbadmin", "password": "changeit"}'
curl -X POST 'http://localhost:8086/db/udr_service/users/dbadmin?u=clusteradmin&p=changeit' -d '{"admin": true}'
curl -X POST -d '[{"name":"meterselection","columns":["time","source","metersource","metertype","metername","status"],"points":[[1427101641000,"cyclops-ui","openstack","gauge","cpu_util",1]]}]' 'http://localhost:8086/db/udr_service/series?u=dbadmin&p=changeit'
##curl -i -XPOST 'http://160.85.4.157:8086/write?db=udr_service' --data-binary 'meterselection,time=1427101641000,source=cyclops-ui,metersource=openstack,metertype=gauge,metername=cpu_util,status=1'
# to execute on cli
# insert meterselection,time=1427101641000,source=cyclops-ui,metersource=openstack,metertype=gauge,metername=cpu_util,status=1 value=0
#
echo "---------------------------------------------------------------------------"
echo "| Installation process is complete "
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| if(all_Installations_Were_Successful_then){"
echo "|         Ready to Rock 'n Roll ! "
echo "|  } "
echo "---------------------------------------------------------------------------"
