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
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.ExternalDataResource;
import ch.icclab.cyclops.usecases.external.model.ExternalDataPoint;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.ResponseUtil;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;

/**
 * Author: Srikanta
 * Created on: 13-Jan-15
 * Description: Receives the data sent by an external application
 * <p>
 * Change Log
 * Name        Date     Comments
 */
public class ExternalAppResource extends ServerResource {
    final static Logger logger = LogManager.getLogger(ExternalAppResource.class.getName());
    private String endpoint = "/ext/app";
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Receives the JSON data sent by an external application
     * <p>
     * Pseudo Code<br/>
     * 1. Receive the data<br/>
     * 2. Extract the JSON array<br/>
     * 3. Send the JSON array to saveData() for further processing<br/>
     *
     * @param entity
     * @return Representation A JSON response is returned
     */
    @Post("json:json")
    public Representation receiveRequest(JsonRepresentation entity) {
        counter.increment(endpoint);
        logger.debug("Received request for posting a new data point.");
        JSONArray jsonArr = null;

        LocalDateTime currentDateTime = new LocalDateTime();
        Response response = new Response();
        Representation jsonResponse = new JsonRepresentation(response);
        ResponseUtil util = new ResponseUtil();

        try {
            jsonArr = entity.getJsonArray();
        } catch (JSONException e) {
            logger.error("Error while trying to cast the data point: " + e.getMessage());
        }
        response = saveData(jsonArr, response);
        response.setTimestamp(currentDateTime.toDateTime().toString());
        jsonResponse = util.toJson(response);
        return jsonResponse;
    }

    /**
     * Receives a JSONArray with the data points that we want to save and saves them one by one into the db.
     *
     * @param jsonArr
     * @param response
     * @return Response
     *///TODO: non eficient, we need batchpoints but it can overflow the memory. ask martin
    public Response saveData(JSONArray jsonArr, Response response) {
        logger.debug("Attempting to save the datapoint into the DB.");
        TSDBResource dbResource = new TSDBResource();

        Gson gson = new Gson();
        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                ExternalDataPoint externalDataPoint = gson.fromJson(jsonArr.get(i).toString(), ExternalDataPoint.class);
                dbResource.saveExtData(externalDataPoint);
            }
        } catch (JSONException e) {
            logger.error("Error while saving the data in the db: " + e.getMessage());
            response.setStatus("Failure");
            response.setMessage("Error while saving the data in the DB: " + e.getMessage());
            return response;
        }
        logger.debug("Data successfully saved into the DB.");
        response.setStatus("Success");
        response.setMessage("Data successfully saved into the DB.");
        return response;
    }
}
