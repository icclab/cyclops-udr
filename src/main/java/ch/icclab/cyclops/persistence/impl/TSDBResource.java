/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

package ch.icclab.cyclops.persistence.impl;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Description: A RESTLET resource class for handling usage data transformation and
 * persisting into InfluxDB
 *
 * Change Log
 * Name        Date     Comments
 */

import ch.icclab.cyclops.model.udr.CumulativeMeterData;
import ch.icclab.cyclops.model.udr.GaugeMeterData;
import ch.icclab.cyclops.model.udr.TSDBData;
import ch.icclab.cyclops.persistence.client.InfluxDBClient;
import ch.icclab.cyclops.resource.interfc.DatabaseResource;
import ch.icclab.cyclops.util.DateTimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class TSDBResource implements DatabaseResource{

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     *
     * Pseudo Code
     * 1. Iterate through the data array to save the data into an ArraList of objects
     * 2. Save the data into the TSDB POJO class
     * 3. Convert the POJO class to a JSON obj
     * 4. Invoke the InfluxDb client to save the data
     *
     * @param dataArr An array list consisting of usage data
     * @param meterName Name of the Gauge Meter
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveGaugeMeterData(ArrayList<GaugeMeterData> dataArr, String meterName){
        String jsonData;
        TSDBData dbData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        ArrayList<ArrayList<Object>> samplesConsolidatedArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        DateTimeUtil time = new DateTimeUtil();
        ArrayList<Object> objArrNode;
        GaugeMeterData gMeterData;
        boolean result = true;

        strArr.add("time");
        strArr.add("userid");
        strArr.add("resourceid");
        strArr.add("projectid");
        strArr.add("type");
        strArr.add("min");
        strArr.add("max");
        strArr.add("sum");
        strArr.add("avg");
        strArr.add("unit");
        strArr.add("count");

        for (int i=0; i<dataArr.size(); i++){
            gMeterData = dataArr.get(i);
            objArrNode = new ArrayList<Object>();
            objArrNode.add(time.getEpoch(gMeterData.getPeriod_end()));
            objArrNode.add(gMeterData.getGroupby().getUser_id());
            objArrNode.add(gMeterData.getGroupby().getResource_id());
            objArrNode.add(gMeterData.getGroupby().getProject_id());
            objArrNode.add("gauge");
            objArrNode.add(gMeterData.getMin());
            objArrNode.add(gMeterData.getMax());
            objArrNode.add(gMeterData.getSum());
            objArrNode.add(gMeterData.getAvg());
            objArrNode.add(gMeterData.getUnit());
            objArrNode.add(gMeterData.getCount());

            samplesConsolidatedArr.add(objArrNode);
        }
        dbData.setName(meterName);
        dbData.setColumns(strArr);
        dbData.setPoints(samplesConsolidatedArr);

        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonData = mapper.writeValueAsString(dbData);
            dbClient.saveData(jsonData);
        } catch (JsonProcessingException e) {
            System.out.println("Saved to TSDB : False");
            e.printStackTrace();
            return false;
        }

        System.out.println("Saved to TSDB " + result);
        return result;
    }

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     *
     * Pseudo Code
     * 1. Iterate through the data array to save the data into an ArraList of objects
     * 2. Save the data into the TSDB POJO class
     * 3. Convert the POJO class to a JSON obj
     * 4. Invoke the InfluxDb client to save the data
     *
     * @param dataArr An array list consisting of usage data
     * @param meterName Name of the Gauge Meter
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveCumulativeMeterData(ArrayList<CumulativeMeterData> dataArr, String meterName){
        String jsonData;
        boolean result = true;
        ArrayList<Object> samplesArr;
        CumulativeMeterData cMeterData;
        DateTimeUtil time = new DateTimeUtil();
        TSDBData dbData = new TSDBData();
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<String> columnNameArr = new ArrayList<String>();
        ArrayList<ArrayList<Object>> samplesConsolidatedArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        
        // Build the array with column names for the time series
        columnNameArr.add("time");
        columnNameArr.add("userid");
        columnNameArr.add("resourceid");
        columnNameArr.add("volume");
        columnNameArr.add("usage");
        columnNameArr.add("source");
        columnNameArr.add("project_id");
        columnNameArr.add("type");
        columnNameArr.add("id");
        columnNameArr.add("unit");
        columnNameArr.add("instance_id");
        columnNameArr.add("instance_type");
        columnNameArr.add("mac");
        columnNameArr.add("fref");
        columnNameArr.add("name");
        //Build an array consisting of samples
        for (int i=0; i<dataArr.size(); i++){
            cMeterData = dataArr.get(i);
            samplesArr = new ArrayList<Object>();
            samplesArr.add(time.getEpoch(cMeterData.getRecorded_at()));
            samplesArr.add(cMeterData.getUser_id());
            samplesArr.add(cMeterData.getResource_id());
            samplesArr.add(cMeterData.getVolume());
            samplesArr.add(cMeterData.getUsage());
            samplesArr.add(cMeterData.getSource());
            samplesArr.add(cMeterData.getProject_id());
            samplesArr.add(cMeterData.getType());
            samplesArr.add(cMeterData.getId());
            samplesArr.add(cMeterData.getUnit());
            samplesArr.add(cMeterData.getMetadata().getInstance_id());
            samplesArr.add(cMeterData.getMetadata().getInstance_type());
            samplesArr.add(cMeterData.getMetadata().getMac());
            samplesArr.add(cMeterData.getMetadata().getFref());
            samplesArr.add(cMeterData.getMetadata().getName());
            // Build an array which contains all the sample arrays.
            samplesConsolidatedArr.add(samplesArr);
        }
        // Set the data object to be converted into a JSON request string
        dbData.setName(meterName);
        dbData.setColumns(columnNameArr);
        dbData.setPoints(samplesConsolidatedArr);
        // Convert the data object into a JSON string
        try {
            jsonData = mapper.writeValueAsString(dbData);
            // Write the JSON string to the DB
            dbClient.saveData(jsonData);
        } catch (JsonProcessingException e) {
            System.out.println("Saved to TSDB : False");
            e.printStackTrace();
            return false;
        }

        System.out.println("Saved to TSDB " + result);
        return result;
    }

    /**
     * Receives the transformed usage data from an external application in terms of an TSDBData POJO.
     * POJO is converted into a json object and the InfluxDB client is invoked to persist the data.
     *
     * Pseudo Code
     * 1. Convert the TSDB POJO consisting of the usage data into a JSON Obj
     * 2. Invoke the InfluxDB client
     * 3. Save the data in to the DB
     *
     * @param dbData 
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveExtData(TSDBData dbData) {
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData;
        boolean result = true;

        try {
            jsonData = mapper.writeValueAsString(dbData);
            dbClient.saveData(jsonData);
        } catch (JsonProcessingException e) {
            System.out.println("Saved to TSDB : False");
            e.printStackTrace();
            result = false;
            return result;
        }
        return result;
    }

    public TSDBData getUsageData(String from, String to, String userId, Object meterName, String source, String type){
        String query = null;
        InfluxDBClient dbClient = new InfluxDBClient();
        
        if(source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("cumulative")){
            query = "SELECT usage,unit,type FROM "+meterName+" WHERE time > '"+from+"' AND time < '"+to+"' AND userid='"+userId+"' ";
        }else if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("gauge")){
            query = "SELECT avg,unit,type FROM "+meterName+" WHERE time > '"+from+"' AND time < '"+to+"' AND userid='"+userId+"' ";
        }
        
        return dbClient.getData(query);
    }

    public TSDBData getMeterList(){
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData tsdbData = null;
        Long epoch;
        
        //Get the first entry
        tsdbData = dbClient.getData("select * from meterselection limit 1");
        // Extract the time of the first entry
        epoch = (Long) tsdbData.getPoints().get(0).get(0);
        // Use the extracted epoch time to get all the data entry
        tsdbData = dbClient.getData("select * from meterselection "+"where time > "+epoch+"ms");
        return tsdbData;
    }
}