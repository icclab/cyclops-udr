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

package ch.icclab.cyclops.services.iaas.openstack.client;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Upgraded by: Manu
 * Upgraded on: 23-Sep-15
 * Description: Client class for Telemetry. Connects to the telemetry to get the usage data
 */

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.Load;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;

public class TelemetryClient extends ClientResource {
    final static Logger logger = LogManager.getLogger(TelemetryClient.class.getName());
    //TODO: consider Singleton connection.
    /**
     * Queries the ceilometer to get the usage data
     *
     * @param token     Keystone token
     * @param meterName Name of the meter in the Ceilometer
     * @param meterType Cumulative or Gauge meter
     * @return jsonOutput A JSON string consisting of data from a meter
     * @throws IOException
     */
    public String getData(String token, String meterName, String meterType) throws IOException {
        logger.trace("BEGIN String getData(String token, String meterName, String meterType) throws IOException");
        String jsonOutput;
        String from, to, url;
        String[] range;
        ClientResource cr;
        Request req;
        Series<Header> headerValue;
        Client client;

        DateTimeUtil dateTime = new DateTimeUtil();
        range = dateTime.getRange();
        from = range[1];
        logger.debug("Obtained \"FROM\" time for the request: " + from);
        to = range[0];
        logger.debug("Obtained \"TO\" time for the request: " + to);
        url = generateTelemetryQuery(meterType, meterName, to, from);
        logger.debug("Ceilometer URL " + url);
        client = new Client(Protocol.HTTP);
        cr = new ClientResource(url);
        req = cr.getRequest();
        headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");
        headerValue.add("X-Auth-Token", token);

        cr.get(MediaType.APPLICATION_JSON);

        Representation output = cr.getResponseEntity();
        jsonOutput = output.getText();
        logger.debug("Ceilometer response: " + jsonOutput);
        logger.trace("END String getData(String token, String meterName, String meterType) throws IOException");
        return jsonOutput;
    }

    /**
     * Generates the query parameters for a particular meter
     *
     * @param meterType Cumulative or gauge
     * @param meterName Name of the meter in the Ceilometer
     * @param to        Timestamp from where data needs to be collected
     * @param from      Timestamp till when data needs to be collected
     * @return url A String consisting of a URL and query parameters
     */
    private String generateTelemetryQuery(String meterType, String meterName, String to, String from) {
        logger.trace("BEGIN generateTelemetryQuery(String meterType, String meterName, String to, String from)");
        String url = Loader.getSettings().getKeyStoneSettings().getCeilometerURL();

        if (meterType.equals("gauge")) {
            url = url + "meters/" + meterName + "/statistics?q.field=timestamp&q.op=gt&q.value=" + from + "&q.field=timestamp&q.op=lt&q.value=" + to + "&groupby=user_id&groupby=project_id&groupby=resource_id";
        } else {
            url = url + "samples" + "?q.field=timestamp&q.op=ge&q.value=" + from + "&q.field=timestamp&q.op=le&q.value=" + to + "&q.field=meter&q.op=eq&q.value=" + meterName;
        }
        logger.trace("END generateTelemetryQuery(String meterType, String meterName, String to, String from)");
        return url;
    }
}
