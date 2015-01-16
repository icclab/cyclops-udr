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

package ch.icclab.cyclops.persistence.client;

import ch.icclab.cyclops.util.LoadConfiguration;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Author: Srikanta
 * Created on: 15-Oct-14
 * Description: Client class for InfluxDB
 *
 * Change Log
 * Name        Date     Comments
 */
public class InfluxDBClient extends ClientResource {

    /**
     * Saves the data into InfluxDB via HTTP
     *
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the HTTP protocol, url and auth details
     * 3. Send the data
     *
     * @param data
     * @return boolean
     */
    public boolean saveData(String data){
        System.out.println("Entered the InfluxDBClient");
        LoadConfiguration load = new LoadConfiguration();
        String url = load.configuration.get("InfluxDBURL");
        String username = load.configuration.get("InfluxDBUsername");
        String password = load.configuration.get("InfluxDBPassword");

        data = "["+data+"]";
        Representation output;
        System.out.println(data);

        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);

        cr.post(data);
        output = cr.getResponseEntity();

        System.out.println(output);
        System.out.println("Exit InfluxDBClient");

        return true;
    }
}
