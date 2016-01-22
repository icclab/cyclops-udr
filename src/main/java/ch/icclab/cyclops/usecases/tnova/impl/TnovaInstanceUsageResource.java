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
package ch.icclab.cyclops.usecases.tnova.impl;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.services.iaas.cloudstack.util.Time;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.usecases.tnova.model.TnovaInstanceUsage;
import ch.icclab.cyclops.usecases.tnova.model.TnovaTSDBData;
import ch.icclab.cyclops.usecases.tnova.model.TnovaUDRList;
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
 * Created by Manu on 04/01/16.
 */
public class TnovaInstanceUsageResource extends ServerResource {
    final static Logger logger = LogManager.getLogger(TnovaInstanceUsageResource.class.getName());

    private String instanceId;
    private final String endpoint = "/usage/instance";
    private final APICallCounter counter;
    private final InfluxDBClient dbClient;

    public TnovaInstanceUsageResource() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }


    public void doInit() {
        instanceId = (String) getRequestAttributes().get("instanceId");
    }

    @Get
    public String getInstanceUsage() {
        counter.increment(endpoint);
        if (instanceId == null || instanceId.isEmpty()) {
            logger.error("Trying to get Usage Records for the instance, but instanceId is not provided");
            throw new ResourceException(500);
        } else {
            logger.trace("Accessing usage records for instanceId: " + instanceId);
        }
        String result = retrieveUsageRecordsForInstance(instanceId, getQueryValue("from"), getQueryValue("to")).toString();
        return result;
    }

    private String retrieveUsageRecordsForInstance(String instanceId, String from, String to) {
        String fromDate = Time.normaliseString(from);
        String toDate = Time.normaliseString(to);
        String query = "SELECT usage FROM UDR WHERE instanceId='" + instanceId + "' AND time>='" + fromDate + "' AND time<='" + toDate;

        logger.debug("Connecting to influxDB to execute query: "+ query);
        InfluxDB influxDB = InfluxDBFactory.connect(Loader.getSettings().getInfluxDBSettings().getInfluxDBURL(), Loader.getSettings().getInfluxDBSettings().getInfluxDBUsername(), Loader.getSettings().getInfluxDBSettings().getInfluxDBPassword());
        Query queryObject = new Query(query, Loader.getSettings().getInfluxDBSettings().getInfluxDBDatabaseName());
        JSONArray jsonArray = new JSONArray(influxDB.query(queryObject).getResults());

        TnovaTSDBData dataObj = null;
        ArrayList<TnovaUDRResponse> tnovaUDRResponses = new ArrayList<TnovaUDRResponse>();
        TnovaUDRResponse tnovaUDRResponse = new TnovaUDRResponse();
        TnovaInstanceUsage tnovaInstanceUsage = new TnovaInstanceUsage();
        Gson gson = new Gson();

        try {
            logger.debug("Transforming into UDR the retrieved json: "+jsonArray.toString());
            if (jsonArray != null) {
                if (jsonArray.toString().equals("[{}]")) {
                    return jsonArray.toString();
                } else {
                    JSONObject resultObject = (JSONObject) jsonArray.get(0);
                    JSONArray series = (JSONArray) resultObject.get("series");
                    dataObj = gson.fromJson(series.get(0).toString(), TnovaTSDBData.class);//TSDBData
                }
            }
            ArrayList<String> columns = dataObj.getColumns();
            tnovaInstanceUsage.setName(dataObj.getName());
            tnovaInstanceUsage.setInstanceId((String) dataObj.getTags().get("instanceId"));
            for (ArrayList<Object> value : dataObj.getValues()) {
                tnovaUDRResponse.setFields(value, columns);
                tnovaUDRResponses.add(tnovaUDRResponse);
            }
        } catch (Exception e) {
            logger.error("Error while parsing the Json: " + e.getMessage());
        }

        tnovaInstanceUsage.setFrom(from);
        tnovaInstanceUsage.setTo(to);
        tnovaInstanceUsage.setUsages(tnovaUDRResponses);

        return gson.toJson(tnovaInstanceUsage);

    }

}

