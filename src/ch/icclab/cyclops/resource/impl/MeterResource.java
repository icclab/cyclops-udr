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

package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.udr.Response;
import ch.icclab.cyclops.model.udr.TSDBData;
import ch.icclab.cyclops.persistence.client.InfluxDBClient;
import ch.icclab.cyclops.persistence.impl.TSDBResource;
import ch.icclab.cyclops.resource.interfc.UDRResource;
import ch.icclab.cyclops.util.Flag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Author: Srikanta
 * Created on: 02-Mar-15
 * Description: Services the GET and POST request regarding the information of meters that are selected 
 * 
 * Change Log
 * Name        Date     Comments
 */
public class MeterResource extends ServerResource implements UDRResource {
    
    /**
     * Receives the JSON data consisting of meter selection status
     *
     * Pseudo Code
     * 1. Receive the data
     * 2. Extract the JSON array
     * 3. Send the JSON array to saveData() for persistence into the DB
     *
     * @param entity
     * @return Representation A JSON response is returned
     */
    @Post("json:json")
    public String saveMeterSelectionStatus(JsonRepresentation entity){
        JSONObject jsonObj;
        JSONArray jsonArr = null;
        boolean output = true;
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;
        LocalDateTime currentDateTime = new LocalDateTime();
        Response response = new Response();

        //Set the isMeterListReset to TRUE
        Flag.setMeterListReset(true);
        
        // Process the incoming request
        try {
            jsonArr = entity.getJsonArray();
            output = saveData(jsonArr.getJSONObject(0));
        } catch (JSONException e) {
            output = false;
            e.printStackTrace();
        }
        response.setTimestamp(currentDateTime.toDateTime().toString());
        if(output){
            response.setStatus("Success");
            response.setMessage("Data saved into the DB");
        }else {
            response.setStatus("Failure");
            response.setMessage("Data could not be saved into the DB");
        }

        try {
            jsonData = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonData;
    }

    /**
     * Invoke the db client to persist the data
     *
     * Pseudo Code
     * 1. Receive the data
     * 2. Save the data using the InfluxDB client
     *
     * @param jsonArr
     * @return boolean
     */
    private boolean saveData(JSONObject jsonArr) {
        InfluxDBClient dbClient = new InfluxDBClient();
        boolean status = false;
        
        status = dbClient.saveData(jsonArr.toString());
        return status;
    }

    @Get
    public Representation getMeterList(){
        String jsonStr;
        JsonRepresentation responseJson = null;
        TSDBData responseObj = new TSDBData();
        ObjectMapper mapper = new ObjectMapper();
        TSDBResource tsdbResource = new TSDBResource();
        responseObj = tsdbResource.getMeterList();

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }
}
