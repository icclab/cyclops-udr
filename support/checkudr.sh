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

# After installing gatekeeper, simply run this script to test if all API endpoints 
# are working properly or not. Make sure that Gatekeeper has been srated before
# runnig this test script. Change APIPATH to appropriate value.

APIPATH="http://160.85.4.237:8080/udr/api"
function jsonValue() {
	KEY=$1
	num=$2
	awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}
RESPONSE=`curl -X GET "$APIPATH" --header "Content-Type:application/json"`
RESULT=`echo $RESPONSE | jsonValue status`
MSG=`echo $RESPONSE | jsonValue message`

if [ "$RESULT" == "Success" ]; then
	echo "$MSG"
	exit 0
else
	echo "UDR-Error: $MSG"
	exit 2
fi
