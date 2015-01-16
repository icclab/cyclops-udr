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
 * Description: Handles the incoming API request be routing it the appropriate resource class.
 * Also loads the configuration file at the start of the application
 *
 * Change Log
 * Name        Date     Comments
 */

import ch.icclab.cyclops.resource.impl.ExternalAppResource;
import ch.icclab.cyclops.resource.impl.RootResource;
import ch.icclab.cyclops.resource.impl.TelemetryResource;
import ch.icclab.cyclops.util.LoadConfiguration;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import java.io.IOException;

public class UDRApplication extends Application{

    /**
     * This method handles the incoming request and routes it to the appropriate resource class
     *
     * Pseudo code
     * 1. Create an instance of Router
     * 2. Attach the api end points and their respective resource class for request handling
     * 3. Return the router
     *
     * @return router
     */
    public Restlet createInboundRoot()
    {
        System.out.println("Entered the router");

        loadConfiguration(getContext());
        Router router = new Router(getContext());
        router.attach("/", RootResource.class);
        router.attach("/api", TelemetryResource.class);
        router.attach("/ext/app", ExternalAppResource.class);

        System.out.println("Finished routing");
        return router;
    }

    /**
     * Loads the configuration file at the beginning of the application startup
     *
     * Pseudo Code
     * 1. Create the LoadConfiguration class
     * 2. Load the file if the the existing instance of the class is empty
     *
     * @param context
     */
    private void loadConfiguration(Context context) {
        LoadConfiguration loadConfig = new LoadConfiguration();
        if(loadConfig.configuration == null){
            try {
                loadConfig.run(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}