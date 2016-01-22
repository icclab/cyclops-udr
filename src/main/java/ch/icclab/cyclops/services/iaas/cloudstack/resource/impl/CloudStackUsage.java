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

import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.UsageList;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.QueryResult;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 23-Nov-15
 * Description: Endpoint for getting CloudStack usage for particular user
 */
public class CloudStackUsage extends ServerResource {
    final static Logger logger = LogManager.getLogger(CloudStackUsage.class.getName());

    // some required variables
    private String userId;
    private final String endpoint = "/usage/users";
    private final APICallCounter counter;
    private final InfluxDBClient dbClient;

    public CloudStackUsage() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }

    public void doInit() {
        userId = (String) getRequestAttributes().get("userid");
    }

    @Get
    public String getUsageRecordsForUser() {
        counter.increment(endpoint);

        // check whether we have userid
        if (userId == null || userId.isEmpty()) {
            logger.error("Trying to get Usage Records for user, but userid is not provided");
            throw new ResourceException(500);
        } else {
            logger.trace("Accessing usage records for user: " + userId);
        }

        // first step is to know which meters are selected
        List<String> meterSelection = getEnabledMeterList();

        // now load all usage records for selected user
        UsageList data = retrieveUsageRecordsForUser(userId, getQueryValue("from"), getQueryValue("to"), meterSelection);

        // finally, return JSON representation of data object
        return new Gson().toJson(data);
    }

    /**
     * Return the list of enabled meters
     *
     * @return list
     */
    private List getEnabledMeterList() {
        return new CloudStackMeter().retrieveMeterList().getEnabledMeterNames();
    }

    /**
     * Retrieve Usage Records for specified User, enabled meters and provided time window
     *
     * @param user
     * @param from
     * @param to
     * @param meters
     * @return UsageList object
     */
    private UsageList retrieveUsageRecordsForUser(String user, String from, String to, List<String> meters) {
        UsageList list = new UsageList();

        // populate header
        list.setUserid(user);
        list.setTime(from, to);

        // go over all enabled meters
        for (String meter : meters) {

            // first construct required query
            String query = list.createQuery(user, from, to, meter);
            QueryResult result = dbClient.runQuery(query);
            list.addUsageFromMeter(result);
        }

        return list;
    }
}
