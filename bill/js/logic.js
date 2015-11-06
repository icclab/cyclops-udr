/*
 * Copyright (c) 2014. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

/**
 * Created by Srikanta on 28-Nov-14.
 */

function stepOneNextBtn(){
    $('#step1').hide();
    $('#step2').show();
}

function stepTwoBackBtn(){
    $('#step1').show();
    $('#step2').hide();
}

function stepTwoNextBtn(){
    $('#step3').show();
    $('#step2').hide();
}

function stepThreeBackBtn(){
    $('#step2').show();
    $('#step3').hide();
}

function stepThreeNextBtn(){
    $('#step3').hide();
    $('#step4').show();
    var text = "CPU, Bandwidth & Memory from " + $('#fromDate').val() + " to " + $('#toDate').val() ;

    $('#resourceDetails').html(text);
    $('#resourceCost').html(parseFloat($('#chargeTotal').val()));
    $('#totalCost').html(parseFloat($('#chargeTotal').val()));
}

//Does the AJAX query to get the data from the InfluxDB
function getMeterData(meterName){
    var id = meterName;
    var dbUserName = $('#dbUsername').val();
    var dbPassword = $('#dbPassword').val();
    var dbName = $('#dbName').val();
    var dbPort = $('#dbPort').val();
    var dbHost = $('#dbHost').val();
    var tenantid = $('#tenantid').val();
    var fromDate = $('#fromDate').val();
    var toDate = $('#toDate').val();

    $('#'+meterName+'Status').html("Fetching..");

    if(meterName === "dataOut"){
        meterName = "network.outgoing.bytes.rate";
    }else if(meterName === "dataIn"){
        meterName = "network.incoming.bytes.rate";
    }

    var url = dbHost+":"+dbPort+"/db/"+dbName+"/series?u="+dbUserName+"&q=SELECT MEAN(avg) FROM "+meterName+" where time > '"+fromDate+"' and time < '"+toDate+"' and projectid='"+tenantid+"'&p="+dbPassword;

    $.get(url, function(data){
        var name = data[0].name;
        var value = data[0].points[0];

        if(name === "network.outgoing.bytes.rate"){
            value = getAbsoluteRate(value[1],fromDate,toDate);
            name = "dataOut";
        }else if(name === "network.incoming.bytes.rate"){
            value = getAbsoluteRate(value[1],fromDate,toDate);
            name = "dataIn";
        }

        $('#'+name).html(value[1]);
        $('#'+name+'Status').html("Successful");
        $('#'+name+'StatusIndicator').toggleClass('bg-success');
        calculateRate();
    },"json");
}

//Calculates the Charge for a given tenant
function calculateRate(){
    var total = $('#chargeTotal').val();
    var cpuUtilCharge = parseFloat($('#cpuUtilCharge').val()) * parseFloat($('#cpu_util').text());
    var dataInCharge = parseFloat($('#dataInCharge').val()) * parseFloat($('#dataIn').text());
    var dataOutCharge = parseFloat($('#dataOutCharge').val()) * parseFloat($('#dataOut').text());
    var memoryCharge = parseFloat($('#memoryCharge').val()) * parseFloat($('#memory').text());

    if(!isNaN(total)){
        total = 0;
    }
    if(!isNaN(total) && !isNaN(cpuUtilCharge)){
        total = parseFloat(cpuUtilCharge);
    }
    if(!isNaN(total) && !isNaN(cpuUtilCharge) && !isNaN(dataInCharge)){
        total = parseFloat(cpuUtilCharge) + parseFloat(dataInCharge);
    }
    if(!isNaN(total) && !isNaN(cpuUtilCharge) && !isNaN(dataInCharge) && !isNaN(dataOutCharge)){
        total = parseFloat(cpuUtilCharge) + parseFloat(dataInCharge) + parseFloat(dataOutCharge);
    }
    if(!isNaN(total) && !isNaN(cpuUtilCharge) && !isNaN(dataInCharge) && !isNaN(dataOutCharge) && !isNaN(memoryCharge)){
        total = parseFloat(cpuUtilCharge) + parseFloat(dataInCharge) + parseFloat(dataOutCharge) + parseFloat(memoryCharge);
    }

    $('#chargeTotal').val(parseFloat(total));
}

//Calculates the Absolute value of the incoming and outgoing data
function getAbsoluteRate(value,fromDate,toDate) {
    var fromDateUTC = new Date(fromDate);
    var toDateUTC = new Date(toDate);

    var absoluteValue = parseFloat(value) * ( parseFloat((toDateUTC.getTime() / 1000)) - parseFloat((fromDateUTC.getTime() / 1000)));
    var returnArray = [0, parseFloat(absoluteValue)];

    return returnArray;
}

//Calculates the total cost in the Invoicing section
function calculateTotalCost(){
    if(!isNaN($('#cpuCost').val()) && !isNaN($('#bandwidthCost').val()) && !isNaN($('#memoryCost').val())){
        $('#totalCost').val(parseFloat($('#cpuCost').val()) + parseFloat($('#bandwidthCost').val()) + parseFloat($('#memoryCost').val()));
    }
    if(!isNaN($('#cpuCost').val()) && !isNaN($('#bandwidthCost').val())){
        $('#totalCost').val(parseFloat($('#cpuCost').val()) + parseFloat($('#bandwidthCost').val()));
    }
}