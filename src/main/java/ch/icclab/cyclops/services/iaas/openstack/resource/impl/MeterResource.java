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

package ch.icclab.cyclops.services.iaas.openstack.resource.impl;

import ch.icclab.cyclops.services.iaas.openstack.model.Response;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.UDRResource;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.Flag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 02-Mar-15
 * Description: Services the GET and POST request regarding the information of meters that are selected
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class MeterResource extends ServerResource implements UDRResource {

    final static Logger logger = LogManager.getLogger(MeterResource.class.getName());
    private String endpoint = "/meters";
    private APICallCounter counter = APICallCounter.getInstance();
    /**
     * Receives the JSON data consisting of meter selection status
     * <p/>
     * Pseudo Code<br>
     * 1. Receive the data<br>
     * 2. Extract the JSON array<br>
     * 3. Send the JSON array to saveData() for persistence into the DB
     *
     * @param entity The body of the POST request
     * @return Representation A JSON response containing the status of the request serviced
     */
    @Post("json:json")
    public Representation setMeterList(Representation entity) {
        counter.increment(endpoint);
        logger.trace("BEGIN Representation setMeterList(Representation entity)");
        boolean output = true;
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;
        JsonRepresentation request = null;
        JsonRepresentation responseJson = null;
        LocalDateTime currentDateTime = new LocalDateTime();
        Response response = new Response();

        // Get the JSON representation of the incoming POST request
        try {
            request = new JsonRepresentation(entity);
        } catch (IOException e) {
            logger.error("EXCEPTION IOEXCEPTION Representation setMeterList(Representation entity)");
            e.printStackTrace();
        }
        //Tells to UDR that need to reload the meter list in Load
        Flag.setMeterListReset(true);
        // Process the incoming request
        try {
            output = saveData(request.getJsonObject());
        } catch (JSONException e) {
            logger.error("EXCEPTION JSONGEXCEPTION Representation setMeterList(Representation entity)");
            output = false;
            e.printStackTrace();
        }
        // Set the time stamp
        response.setTimestamp(currentDateTime.toDateTime().toString());
        // Set the status and message
        if (output) {
            response.setStatus("Success");
            response.setMessage("Data saved into the DB");
        } else {
            logger.debug("DEBUG Representation setMeterList(Representation entity): Data could not be saved into the DB");
            response.setStatus("Failure");
            response.setMessage("Data could not be saved into the DB");
        }
        //TODO: jsonMapper method. reusable in all the classes.
        // Convert the Java object to a JSON string
        try {
            jsonData = mapper.writeValueAsString(response);
            responseJson = new JsonRepresentation(jsonData);
        } catch (JsonProcessingException e) {
            logger.error("EXCEPTION JSONPROCESSINGEXCEPTION Representation setMeterList(Representation entity)");
            e.printStackTrace();
        }
        logger.trace("END Representation setMeterList(Representation entity)");
        return responseJson;
    }

    /**
     * Invoke the db client to persist the data
     * <p/>
     * Pseudo Code<br>
     * 1. Receive the data<br>
     * 2. Save the data using the InfluxDB client
     *
     * @param jsonObj The JSON object that needs to be persisted into the DB
     * @return boolean
     */
    private boolean saveData(JSONObject jsonObj) {
        logger.trace("BEGIN boolean saveData(JSONObject jsonObj)");
        InfluxDBClient dbClient = new InfluxDBClient();
        boolean status = false;

        status = dbClient.saveData(jsonObj.toString());
        logger.trace("END boolean saveData(JSONObject jsonObj)");
        return status;
    }

    /**
     * Returns the last persisted list of meters
     * <p/>
     * Pseudo Code<br>
     * 1. Receive the request for the list of meters<br>
     * 2. Query the DB to get the list<br>
     * 3. Return the list of meters
     *
     * @return Representation A JSON response containing the list of meters
     */
    @Get
    public Representation getMeterList() {
        counter.increment(endpoint);

        JsonRepresentation responseJson = null;
        TSDBData responseObj;
        ObjectMapper mapper = new ObjectMapper();
        TSDBResource tsdbResource = new TSDBResource();
        responseObj = tsdbResource.getMeterList();

        try {
            String jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            logger.error("Could not parse JSON when getting meterList: " + e.getMessage());
            e.printStackTrace();
        }

        return responseJson;
    }
}
