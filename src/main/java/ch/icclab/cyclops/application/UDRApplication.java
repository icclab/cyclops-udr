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

package ch.icclab.cyclops.application;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Upgraded by: Manu
 * Upgraded on: 23-Sep-15
 * Description: Handles the incoming API request be routing it the appropriate resource class.
 * Also loads the configuration file at the start of the application
 * <p>
 * Change Log
 * Name          Date               Comments
 * Srikanta     02-Mar-2015     Added the api to save and return info regarding selected meters *
 */

import ch.icclab.cyclops.rabbitMQClient.RabbitMQClient;
import ch.icclab.cyclops.rabbitMQClient.ThreadExecutor;
import ch.icclab.cyclops.schedule.Endpoint;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.*;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.APICallEndpoint;
import ch.icclab.cyclops.util.Load;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UDRApplication extends Application {
    final static Logger logger = LogManager.getLogger(UDRApplication.class.getName());

    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     * <p>
     * Pseudo code
     * 1. Create an instance of Router
     * 2. Attach the api end points and their respective resource class for request handling
     * 3. Return the router
     *
     * @return Restlet
     */
    public Restlet createInboundRoot() {
        logger.trace("BEGIN Restlet createInboundRoot()");
        //Load the configuration files and flags
        loadConfiguration();

        APICallCounter counter = APICallCounter.getInstance();
//        InfluxDBClient influxDBClient = new InfluxDBClient();
//        influxDBClient.generateFakeEvent();

        Router router = new Router(getContext());

        // API endpoint counter for requesting status
        router.attach("/status", APICallEndpoint.class);
        counter.registerEndpoint("/status");

        router.attach("/", RootResource.class);
        counter.registerEndpoint("/");

        router.attach("/api", TelemetryResource.class); //API used internally to trigger the data collection
        counter.registerEndpoint("/api");

        router.attach("/ext/app", ExternalAppResource.class); // API used for data insertion from external PaaS/IaaS
        counter.registerEndpoint("/ext/app");

        router.attach("/usage/users/{userid}", UserUsageResource.class); //API used for fetching the usage info for a user
        counter.registerEndpoint("/usage/users");

        router.attach("/usage/resources/{resourceid}", ResourceUsage.class);
        counter.registerEndpoint("/usage/resources");

        router.attach("/mcn/usage", UsageDataRecordResource.class); //API to query time-based service usage per user (required by RC for T-Nova)
        counter.registerEndpoint("/mcn/usage");

        router.attach("/meters", MeterResource.class); //API used for saving and returning the information on selected meters for usage metrics collection
        counter.registerEndpoint("/meters");

        // internal scheduler with start/stop/restart/force/status commands
        router.attach("/scheduler/{command}", Endpoint.class);
        counter.registerEndpoint("/scheduler");

        // but also start scheduler immediately
        startInternalScheduler();

        // and start rabbitmq thread
        startRabbitMQThread();

        logger.trace("END Restlet createInboundRoot()");
        return router;
    }

    /**
     * Method that Initialize the Event handler thread.
     */
    private void startRabbitMQThread() {
        ThreadExecutor.getInstance().start();
    }

    /**
     * Simply start internal scheduler for Event -> UDR
     */
    private void startInternalScheduler() {
        Scheduler.getInstance().start();
    }

    /**
     * Loads the configuration file at the beginning of the application startup
     * <p>
     * Pseudo Code
     * 1. Create the LoadConfiguration class (derived from cyclops.util)
     * 2. Load the file if the the existing instance of the class is empty
     *
     * @param
     */
    private void loadConfiguration() {
        Load.getInstance(getContext());
    }
}