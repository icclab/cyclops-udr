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
 * Name          Date               Comments
 * Srikanta     02-Mar-2015     Added the api to save and return info regarding selected meters *
 */

import ch.icclab.cyclops.resource.impl.*;
import ch.icclab.cyclops.util.Load;
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
    public Restlet createInboundRoot(){
        //Load the configuration files and flags 
        loadConfiguration(getContext());
        
        Router router = new Router(getContext());
        router.attach("/", RootResource.class);
        router.attach("/api", TelemetryResource.class); //API used internally to trigger the data collection
        router.attach("/ext/app", ExternalAppResource.class); // API used for data insertion from external PaaS/IaaS
        router.attach("/usage/users/{userid}", UserUsageResource.class); //API used for fetching the usage info for a user
        router.attach("/usage/resources/{resourceid}", ResourceUsage.class);
        router.attach("/meters", MeterResource.class); //API used for saving and returning the information on selected meters for usage metrics collection
        
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
    private void loadConfiguration(Context context){
        Load load = new Load();
        if(load.configuration == null){
            try {
                load.configuration(getContext());
                load.meterList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}