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
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.ResponseUtil;
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
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class ExternalAppResource extends ServerResource implements ExternalDataResource {
    final static Logger logger = LogManager.getLogger(ExternalAppResource.class.getName());
    private String endpoint = "/ext/app";
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Receives the JSON data sent by an external application
     * <p/>
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
        logger.trace("BEGIN Representation receiveRequest(JsonRepresentation entity)");
        JSONArray jsonArr = null;
        boolean output = true;

        LocalDateTime currentDateTime = new LocalDateTime();
        Response response = new Response();
        Representation jsonResponse = new JsonRepresentation(response);
        ResponseUtil util = new ResponseUtil();

        try {
            jsonArr = entity.getJsonArray();
            output = saveData(jsonArr);
        } catch (JSONException e) {
            logger.error("EXCEPTION JSONEXCEPTION Representation receiveRequest(JsonRepresentation entity)");
            output = false;
            e.printStackTrace();
        }
        response.setTimestamp(currentDateTime.toDateTime().toString());
        if (output) {
            response.setStatus("Success");
            response.setMessage("Data saved into the DB");
        } else {
            response.setStatus("Failure");
            response.setMessage("Data could not be saved into the DB");
        }
        jsonResponse = util.toJson(response);
        logger.trace("END Representation receiveRequest(JsonRepresentation entity)");
        return jsonResponse;
    }

    /**
     * Receives the JSON array, transforms it and send it to the db resource to persist it into InfluxDB
     * <p/>
     * Pseudo Code<br/>
     * 1. Iterate through the JSONArray to get the JSON obj<br/>
     * 2. Iterate through the JSON Obj to get the usage details<br/>
     * 3. Build the TSDB POJO<br/>
     * 4. Pass this POJO obj to the TSDB resource for persisting it into the DB
     *
     * @param jsonArr An array containing the usage data as part of JSON Objects
     * @return String
     */
    public boolean saveData(JSONArray jsonArr) {
        logger.trace("BEGIN boolean saveData(JSONArray jsonArr)");
        JSONObject jsonObj, metadata, usageData;
        String metername = null;
        String source;
        TSDBResource dbResource = new TSDBResource();
        JSONArray dataArr;
        ArrayList<Object> objArrNode;
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        TSDBData dbData = new TSDBData();
        ArrayList<String> columnNameArr = new ArrayList<String>();
        columnNameArr.add("timestamp");
        columnNameArr.add("userid");
        columnNameArr.add("source");
        columnNameArr.add("usage");

        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonObj = (JSONObject) jsonArr.get(i);
                metadata = (JSONObject) jsonObj.get("metadata");
                source = (String) metadata.get("source");
                dataArr = (JSONArray) jsonObj.get("usage");

                for (int j = 0; j < dataArr.length(); j++) {
                    objArrNode = new ArrayList<Object>();
                    usageData = (JSONObject) dataArr.get(j);
                    metername = (String) usageData.get("metername");
                    objArrNode.add(usageData.get("timestamp"));
                    objArrNode.add(usageData.get("userid"));
                    objArrNode.add(source);
                    objArrNode.add(usageData.get("usage"));
                    objArr.add(objArrNode);
                }
                dbData.setName(metername);
                dbData.setColumns(columnNameArr);
                dbData.setPoints(objArr);

                dbResource.saveExtData(dbData);
            }
        } catch (JSONException e) {
            logger.error("EXCEPTION JSONEXCEPTION boolean saveData(JSONArray jsonArr)");
            e.printStackTrace();
            return false;
        }
        logger.trace("END boolean saveData(JSONArray jsonArr)");
        return true;
    }
}
