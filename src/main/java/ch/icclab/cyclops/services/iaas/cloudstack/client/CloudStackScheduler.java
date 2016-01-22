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

import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 22-Oct-15
 * Description: Implementation of scheduler for requesting Usage Records from CloudStack
 */
public class CloudStackScheduler {
    final static Logger logger = LogManager.getLogger(CloudStackScheduler.class.getName());

    // this class has to be a singleton
    private static CloudStackScheduler singleton = new CloudStackScheduler();

    // executor service (we only need one thread)
    private ScheduledExecutorService executor;

    /**
     * We need to hide constructor from public
     */
    private CloudStackScheduler() {
        this.executor = null;
    }

    /**
     * Simple implementation of Singleton class
     *
     * @return instance of scheduler object
     */
    public static CloudStackScheduler getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     */
    public void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();

            // start Usage records collection every full hour plus five minutes (13:05, 14:05, etc)
            // executor.scheduleAtFixedRate(new CloudStackClient(), getSecondsToFullHour() + 300000, 3600000, TimeUnit.MILLISECONDS);

            // start now
            executor.scheduleAtFixedRate(new CloudStackClient(), 0, 3600000, TimeUnit.MILLISECONDS);

            logEvent("start");
        }
    }

    /**
     * Stops execution run
     */
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;

            logEvent("stop");
        }
    }

    /**
     * Returns whether scheduler is running or not
     *
     * @return
     */
    public Boolean isRunning() {
        return (executor != null);
    }

    /**
     * Manually (on top of scheduler) update Usage records from CloudStack
     */
    public void force() {
        new CloudStackClient().run();
    }

    /**
     * Compute difference between now and closest full hour
     *
     * @return time in milliseconds
     */
    private long getSecondsToFullHour() {
        DateTime now = new DateTime(DateTimeZone.UTC);
        DateTime hour = now.hourOfDay().roundCeilingCopy();

        // return difference in milliseconds
        return new Duration(now, hour).getMillis();
    }

    /**
     * Log what have just happened
     *
     * @param command as typeEvent
     */
    private void logEvent(String command) {
        DateTime mark = new DateTime(DateTimeZone.UTC);
        new InfluxDBClient().saveLog(mark, command);

        logger.trace("CloudStackScheduler logging event: " + command);
    }
}
