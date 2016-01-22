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
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.ExternalAppResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.RootResource;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.APICallEndpoint;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * @author Manu
 *         Created by root on 16.11.15.
 */
public abstract class AbstractApplication extends Application {

    // start by providing context
    Context context;

    // router for registering api endpoints
    Router router;

    // get environment settings
    Settings settings;

    // create the api counter
    APICallCounter counter;

    // database connection
    InfluxDBClient dbClient;

    public AbstractApplication() {
        context = getContext();
        router = new Router(context);
        dbClient = new InfluxDBClient();
        counter = APICallCounter.getInstance();

        // and access settings
        settings = Loader.getSettings();

        // create necessary databases
        initialiseDatabases();
    }

    public Restlet createInboundRoot() {
        // API endpoint counter for requesting status
        router.attach("/status", APICallEndpoint.class);
        counter.registerEndpoint("/status");

        router.attach("/", RootResource.class);
        counter.registerEndpoint("/");
        // API used for data insertion from external PaaS/IaaS
        router.attach("/ext/app", ExternalAppResource.class);
        counter.registerEndpoint("/ext/app");

        createRoutes();

        return router;
    }

    public abstract void createRoutes();

    public abstract void initialiseDatabases();
}
