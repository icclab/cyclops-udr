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

package ch.icclab.cyclops.services.iaas.openstack.resource.impl;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.services.iaas.openstack.client.KeystoneClient;
import ch.icclab.cyclops.services.iaas.openstack.model.InfluxDBMeterSelection;
import ch.icclab.cyclops.services.iaas.openstack.model.OpenstackMeter;
import ch.icclab.cyclops.services.iaas.openstack.model.Response;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.UDRResource;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.Flag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.*;
import org.restlet.util.Series;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 02-Mar-15
 * Description: Services the GET and POST request regarding the information of meters that are selected
 * <p>
 * Change Log
 * Name        Date     Comments
 */
public class OpenstackMeterResource extends ServerResource implements UDRResource {

    final static Logger logger = LogManager.getLogger(OpenstackMeterResource.class.getName());
    private String endpoint = "/meters";
    private APICallCounter counter = APICallCounter.getInstance();


    /**
     * Returns the last persisted list of meters
     * <p>
     * Pseudo Code<br>
     * 1. Receive the request for the list of meters<br>
     * 2. Query the DB to get the list<br>
     * 3. Return the list of meters
     *
     * @return Representation A JSON response containing the list of meters
     */
    @Get
    public String getMeterList() {
        counter.increment(endpoint);

        Gson gson = new Gson();

        try {
            String meterUrl = Loader.getSettings().getKeyStoneSettings().getCeilometerURL()+"/meters";
            ClientResource meterResource = new ClientResource(meterUrl);

            Series<Header> requestHeaders =
                    (Series<Header>) meterResource.getRequestAttributes().get("org.restlet.http.headers");

            if (requestHeaders == null) {
                requestHeaders = new Series<Header>(Header.class);
                meterResource.getRequestAttributes().put("org.restlet.http.headers", requestHeaders);
            }

            KeystoneClient keystoneClient = new KeystoneClient();
            logger.trace("Attempting to create the token");
            String subjectToken = keystoneClient.generateToken();

            requestHeaders.set("X-Auth-Token", subjectToken);

            OpenstackMeter[] meters = gson.fromJson(meterResource.get(MediaType.APPLICATION_JSON).getText(), OpenstackMeter[].class);

            return gson.toJson(meters);

        } catch (Exception e) {
            logger.error("Error while getting the Keystone Meters: "+e.getMessage());
            return null;
        }
    }
}
