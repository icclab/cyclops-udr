#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No arguments supplied, expected public IP of this node"
    echo "Usage sudo ./install.sh [public IP]"
    exit 1
fi
sudo ./package-installation.sh $1
./db-setup.sh
