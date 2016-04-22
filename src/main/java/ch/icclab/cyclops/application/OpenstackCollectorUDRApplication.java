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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.Endpoint;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.ExternalAppResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.MeterResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.ResourceUsage;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.UserUsageResource;
import ch.icclab.cyclops.usecases.openstack.impl.OpenstackCollectorEventToUDR;
import ch.icclab.cyclops.usecases.openstack.model.OpenstackCollectorUsageDataRecordResource;
import ch.icclab.cyclops.usecases.openstack.rabbitMQClient.OpenstackCollectorThreadExecutor;

import java.util.concurrent.TimeUnit;


/**
 * Author: Serhiienko Oleksii
 * Created on: 5-April-16
 */
public class OpenstackCollectorUDRApplication extends AbstractApplication {

    @Override
    public void createRoutes() {
        router.attach("/ext/app", ExternalAppResource.class); // API used for data insertion from external PaaS/IaaS
        counter.registerEndpoint("/ext/app");

        router.attach("/usage/users/{userid}", UserUsageResource.class); //API used for fetching the usage info for a user
        counter.registerEndpoint("/usage/users");

        router.attach("/usage/resources/{resourceid}", ResourceUsage.class);
        counter.registerEndpoint("/usage/resources");

        router.attach("/openstack/usage", OpenstackCollectorUsageDataRecordResource.class); //API to query time-based service usage per user (required by RC for T-Nova)
        counter.registerEndpoint("/openstack/usage");

        router.attach("/meters", MeterResource.class); //API used for saving and returning the information on selected meters for usage metrics collection
        counter.registerEndpoint("/meters");

        // internal scheduler with start/stop/restart/force/status commands
        router.attach("/scheduler/{command}", Endpoint.class);
        counter.registerEndpoint("/scheduler");

        // but also start scheduler immediately
        startInternalScheduler();

        // and start rabbitmq thread
        startRabbitMQThread();

    }

    @Override
    public void initialiseDatabases() {
        //TODO which databases have to be created
        dbClient.createDatabases(settings.getInfluxDBSettings().getInfluxDBDatabaseName(), settings.getOpenstackCollectorSettings().getOpenstackCollectorDBEventsName());
    }

    /**
     * Method that Initialize the Event handler thread.
     */
    private void startRabbitMQThread() { //TODO: Martin create addNewRabbitMQ so listener can shut them down
        OpenstackCollectorThreadExecutor.getInstance().start(Loader.getSettings().getOpenstackCollectorSettings().getOpenstackCollectorEventQueue());  // TODO based on environment TNOVA or MCN
    }

    /**
     * Simply start internal scheduler for Event -> UDR
     */
    private void startInternalScheduler() {
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.addRunner(new OpenstackCollectorEventToUDR(), 0, Loader.getSettings().getSchedulerSettings().getSchedulerFrequency(), TimeUnit.SECONDS);
        scheduler.start();
    }

}

