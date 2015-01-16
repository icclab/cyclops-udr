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
     * @param dataArr
     * @param meterName
     * @return
     */
    public boolean saveGaugeMeterData(ArrayList<GaugeMeterData> dataArr, String meterName){

        System.out.println("Saving " +meterName+ " Gauge Meter Data into InfluxDB");
        String jsonData = null;
        TSDBData dbData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ArrayList<Object> objArrNode;
        GaugeMeterData gMeterData;
        boolean result = true;

        strArr.add("userid");
        strArr.add("resourceid");
        strArr.add("projectid");
        strArr.add("min");
        strArr.add("max");
        strArr.add("sum");
        strArr.add("avg");
        strArr.add("unit");
        strArr.add("count");

        for (int i=0; i<dataArr.size(); i++){
            gMeterData = dataArr.get(i);
            objArrNode = new ArrayList<Object>();
            objArrNode.add(gMeterData.getGroupby().getUser_id());
            objArrNode.add(gMeterData.getGroupby().getResource_id());
            objArrNode.add(gMeterData.getGroupby().getProject_id());
            objArrNode.add(gMeterData.getMin());
            objArrNode.add(gMeterData.getMax());
            objArrNode.add(gMeterData.getSum());
            objArrNode.add(gMeterData.getAvg());
            objArrNode.add(gMeterData.getUnit());
            objArrNode.add(gMeterData.getCount());

            objArr.add(objArrNode);
        }
        dbData.setName(meterName);
        dbData.setColumns(strArr);
        dbData.setPoints(objArr);

        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonData = mapper.writeValueAsString(dbData);
            System.out.println(jsonData.toString());
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
     * @param dataArr
     * @param meterName
     * @return
     */
    public boolean saveCumulativeMeterData(ArrayList<CumulativeMeterData> dataArr, String meterName){

        System.out.println("Saving " +meterName+ " Cumulative Meter Data into InfluxDB");
        String jsonData = null;
        TSDBData dbData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ArrayList<Object> objArrNode;
        CumulativeMeterData cMeterData;
        boolean result = true;

        strArr.add("userid");
        strArr.add("resourceid");
        strArr.add("volume");
        strArr.add("usage");
        strArr.add("source");
        strArr.add("project_id");
        strArr.add("type");
        strArr.add("id");
        strArr.add("unit");
        strArr.add("instance_id");
        strArr.add("instance_type");
        strArr.add("mac");
        strArr.add("fref");
        strArr.add("name");

        for (int i=0; i<dataArr.size(); i++){
            cMeterData = dataArr.get(i);
            objArrNode = new ArrayList<Object>();
            objArrNode.add(cMeterData.getUser_id());
            objArrNode.add(cMeterData.getResource_id());
            objArrNode.add(cMeterData.getVolume());
            objArrNode.add(cMeterData.getUsage());
            objArrNode.add(cMeterData.getSource());
            objArrNode.add(cMeterData.getProject_id());
            objArrNode.add(cMeterData.getType());
            objArrNode.add(cMeterData.getId());
            objArrNode.add(cMeterData.getUnit());
            objArrNode.add(cMeterData.getMetadata().getInstance_id());
            objArrNode.add(cMeterData.getMetadata().getInstance_type());
            objArrNode.add(cMeterData.getMetadata().getMac());
            objArrNode.add(cMeterData.getMetadata().getFref());
            objArrNode.add(cMeterData.getMetadata().getName());

            objArr.add(objArrNode);
        }

        dbData.setName(meterName);
        dbData.setColumns(strArr);
        dbData.setPoints(objArr);

        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonData = mapper.writeValueAsString(dbData);
            System.out.println(jsonData.toString());
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
     * @return
     */
    public boolean saveExtData(TSDBData dbData) {
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;

        try {
            jsonData = mapper.writeValueAsString(dbData);
            System.out.println(jsonData.toString());
            dbClient.saveData(jsonData);
        } catch (JsonProcessingException e) {
            System.out.println("Saved to TSDB : False");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void getData(){

    }

}