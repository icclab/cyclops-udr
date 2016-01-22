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

import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.ResourceList;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.DateTimeUtil;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.QueryResult;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 17-Nov-15
 * Description: Endpoint for getting CloudStack resources
 */
public class CloudStackResources extends ServerResource {
    final static Logger logger = LogManager.getLogger(CloudStackResources.class.getName());

    // some required variables
    private String resourceid;
    private final String endpoint = "/usage/resources";
    private final APICallCounter counter;
    private final InfluxDBClient dbClient;

    public CloudStackResources() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }

    public void doInit() {
        resourceid = (String) getRequestAttributes().get("metername");
    }

    @Get
    public String getResources() {
        counter.increment(endpoint);
        logger.trace("Someone is asking for CloudStack resources: " + resourceid);

        ResourceList resourceList = retrieveResources();

        String json = new Gson().toJson(resourceList);

        logger.trace("Serving CloudStack resources: " + resourceid);
        return json;
    }

    /**
     * Look into db and retrieve UDR records for selected resource
     *
     * @return resourceList
     */
    private ResourceList retrieveResources() {

        // when someone forgot to provide metername
        if (resourceid == null) {
            throw new ResourceException(500);
        }

        // create parameters
        Map<String, String> args = new HashMap<String, String>();
        args.put("from", resourceid);

        // determine from and to
        DateTimeUtil dateUtil = new DateTimeUtil();
        try {
            args.put("start", getQueryValue("from"));

            args.put("end", getQueryValue("to"));
        } catch (Exception ignored) {
            logger.debug("Retrieving resources, start or end date not provided");
        }

        // now construct query
        String query = new ResourceList().createDBQuery(args);

        // run it
        QueryResult result = dbClient.runQuery(query);

        // return parsed object
        return new ResourceList(result);
    }
}
