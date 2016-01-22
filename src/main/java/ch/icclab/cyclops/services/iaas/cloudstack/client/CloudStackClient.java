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
package ch.icclab.cyclops.services.iaas.cloudstack.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.CloudStackSettings;
import ch.icclab.cyclops.services.iaas.cloudstack.model.CloudStackUsageTypes;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.impl.CloudStackMeter;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Author: Martin Skoviera
 * Created on: 16-Oct-15
 * Description: Client class for Telemetry. Asks underlying classes for CloudStack data and saves it
 */
public class CloudStackClient implements Runnable {
    final static Logger logger = LogManager.getLogger(CloudStackClient.class.getName());

    // will be used as object to pull data from CloudStack
    private static CloudStackPuller cloudStackPuller;

    // connection to Database
    private static InfluxDBClient dbClient;

    // connection to Load
    private static CloudStackSettings settings;

    /**
     * Simple constructor that will create CloudStack Puller object
     */
    protected CloudStackClient() {
        cloudStackPuller = new CloudStackPuller();
        dbClient = new InfluxDBClient();
        settings = Loader.getSettings().getCloudStackSettings();
    }

    /**
     * This method gets called from outside in order to get data from CloudStack and store it into database
     *
     * @return
     */
    private void updateRecords() {
        logger.debug("Started with updating Usage Records from CloudStack");

        // mark the date (we have to do it here, not in DB client, as request can be before midnight and saving to DB on the next day)
        DateTime mark = new DateTime(DateTimeZone.UTC);

        // get data from CloudStack
        Boolean status = cloudStackPuller.pullUsageRecords();

        if (status) {
            // everything went fine, save timestamp to database
            dbClient.saveLog(mark, "pull");

        } else {
            logger.error("Couldn't update CloudStack Usage records");
        }
    }

    @Override
    public void run() {
        updateRecords();
    }
}
