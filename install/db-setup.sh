#!/bin/bash
echo "---------------------------------------------------------------------------"
echo "| Setting up the database "
echo "---------------------------------------------------------------------------"
curl -X POST 'http://localhost:8086/db?u=root&p=root' -d '{"name": "udr_service"}'
curl -X POST 'http://localhost:8086/db/udr_service/users?u=root&p=root' -d '{"name": "admin", "password": "<Yh9h-vmhGe"v~+"}'
curl -X POST 'http://localhost:8086/db?u=root&p=root' -d '{"name": "grafana"}'
curl -X POST 'http://localhost:8086/db/grafana/users?u=root&p=root' -d '{"name": "admin", "password": "<Yh9h-vmhGe"v~+"}'
echo "---------------------------------------------------------------------------"
echo "| Installation process is complete "
echo "---------------------------------------------------------------------------"
echo "---------------------------------------------------------------------------"
echo "| if(all_Installations_Were_Successful_then){"
echo "|         Ready to Rock 'n Roll ! "
echo "|  } "
echo "---------------------------------------------------------------------------"