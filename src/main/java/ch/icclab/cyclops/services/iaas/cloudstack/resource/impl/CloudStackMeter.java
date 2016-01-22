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
package ch.icclab.cyclops.services.iaas.cloudstack.resource.impl;

import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.Meter;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.MeterList;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.QueryResult;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 17-Nov-15
 * Description: get/set selection of meters
 */
public class CloudStackMeter extends ServerResource {
    final static Logger logger = LogManager.getLogger(CloudStackMeter.class.getName());

    // some required variables
    private final APICallCounter counter;
    private final static String endpoint = "/meters";
    private final InfluxDBClient dbClient;

    public CloudStackMeter() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }

    @Get
    public String getMeterList() {
        counter.increment(endpoint);
        logger.trace("Someone is asking for CloudStack meterList");

        MeterList meterList = retrieveMeterList();

        String json = new Gson().toJson(meterList);

        // return json
        logger.trace("Serving CloudStack meterList back");
        return json;
    }

    /**
     * Ask for meterList from database
     *
     * @return MeterList object
     */
    protected MeterList retrieveMeterList() {
        String query = new MeterList().createDBQuery();

        // run query
        QueryResult result = dbClient.runQuery(query);

        return new MeterList(result);
    }

    /**
     * Add default values for meter list
     */
    public void initializeWithList(List<String> defaults) {
        List<String> list = new ArrayList<String>();

        try {
            // try to retrieve list
            MeterList meterList = retrieveMeterList();

            // get meter names
            List<String> names = meterList.getMeterNames();

            // add only those we don't have yet
            for (String name : defaults) {
                if (!names.contains(name)) {
                    list.add(name);
                }
            }

        } catch (Exception e) {
            // nothing exists, we have to add whole list of defaults
            list.addAll(defaults);
        }

        // only if there is something to save
        if (!list.isEmpty()) {
            BatchPoints container = dbClient.giveMeEmptyContainer();
            for (String name : list) {
                Meter meter = new Meter();
                meter.setMetername(name);
                container.point(meter.toDBPoint());
            }

            dbClient.saveContainerToDB(container);
        }
    }

    @Post
    public String setMeterList(Representation entity) {
        counter.increment(endpoint);
        logger.trace("Received CloudStack meterList selection, saving it into DB");

        try {
            // construct object from JSON based on POJO template
            MeterList meterList = new Gson().fromJson(entity.getText(), MeterList.class);

            // get empty container
            BatchPoints container = dbClient.giveMeEmptyContainer();

            // now fill it with data
            for (Meter meter : meterList.getData()) {

                // add point to container
                container.point(meter.toDBPoint());
            }

            // save it to database
            dbClient.saveContainerToDB(container);

        } catch (IOException e) {
            logger.error("Could not parse received JSON when setting meterList: " + e.getMessage());
            throw new ResourceException(500);
        }
        return "Success";
    }
}
