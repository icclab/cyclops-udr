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

package ch.icclab.cyclops.usecases.mcn.impl;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.services.iaas.cloudstack.util.Time;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.usecases.mcn.model.McnTSDBData;
import ch.icclab.cyclops.usecases.mcn.model.McnUDRList;
import ch.icclab.cyclops.usecases.mcn.model.McnUDRResponse;
import ch.icclab.cyclops.util.APICallCounter;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;

/**
 * @author Manu
 *         Created on 12.01.16.
 */
public class McnUsage extends ServerResource{
    final static Logger logger = LogManager.getLogger(McnUsage.class.getName());

    private String userId;
    private final String endpoint = "/usage/users";
    private final APICallCounter counter;
    private final InfluxDBClient dbClient;

    public McnUsage() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }


    public void doInit() {
        userId = (String) getRequestAttributes().get("userId");
    }

    @Get
    public String getUsage() {

        counter.increment(endpoint);
        if (userId == null || userId.isEmpty()) {
            logger.error("Trying to get Usage Records for user, but userId is not provided");
            throw new ResourceException(500);
        } else {
            logger.trace("Accessing usage records for user: " + userId);
        }
        String result = retrieveUsageRecordsForUser(userId, getQueryValue("from"), getQueryValue("to")).toString();
        return result;
    }

    private String retrieveUsageRecordsForUser(String userId, String from, String to) {
        String fromDate = Time.normaliseString(from);
        String toDate = Time.normaliseString(to);
        String query = "SELECT usage, productType FROM UDR WHERE clientId='" + userId + "' AND time>='" + fromDate + "' AND time<='" + toDate + "' GROUP BY instanceId";

        logger.debug("Connecting to influxDB to execute query: "+ query);
        InfluxDB influxDB = InfluxDBFactory.connect(Loader.getSettings().getInfluxDBSettings().getInfluxDBURL(), Loader.getSettings().getInfluxDBSettings().getInfluxDBUsername(), Loader.getSettings().getInfluxDBSettings().getInfluxDBPassword());
        Query queryObject = new Query(query, Loader.getSettings().getInfluxDBSettings().getInfluxDBDatabaseName());
        JSONArray jsonArray = new JSONArray(influxDB.query(queryObject).getResults());

        McnTSDBData dataObj = null;
        ArrayList<McnUDRResponse> mcnUDRResponses = new ArrayList<McnUDRResponse>();
        McnUDRList mcnUDRList = new McnUDRList();
        Gson gson = new Gson();

        try {
            logger.debug("Transforming into UDR the retrieved json: "+jsonArray.toString());
            if (jsonArray != null) {
                if (jsonArray.toString().equals("[{}]")) {
                    return jsonArray.toString();
                } else {
                    JSONObject resultObject = (JSONObject) jsonArray.get(0);
                    JSONArray series = (JSONArray) resultObject.get("series");
                    dataObj = gson.fromJson(series.get(0).toString(), McnTSDBData.class);//TSDBData
                }
            }
            ArrayList<String> columns = dataObj.getColumns();
            mcnUDRList.setName(dataObj.getName());
            mcnUDRList.setInstanceId((String) dataObj.getTags().get("instanceId"));
            for (ArrayList<Object> value : dataObj.getValues()) {
                McnUDRResponse mcnUDRResponse = new McnUDRResponse();
                mcnUDRResponse.setFields(value, columns);
                mcnUDRResponses.add(mcnUDRResponse);
            }
        } catch (Exception e) {
            logger.error("Error while parsing the Json: " + e.getMessage());
        }

        mcnUDRList.setFrom(from);
        mcnUDRList.setTo(to);
        mcnUDRList.setUsages(mcnUDRResponses);

        return gson.toJson(mcnUDRList);

    }

}
