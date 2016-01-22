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

import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.services.iaas.openstack.model.UserUsageResponse;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.UsageResource;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 21-Jan-15
 * Description:  Services the API GET request coming in for usage data for a given user ID.
 */
public class UserUsageResource extends ServerResource implements UsageResource {
    final static Logger logger = LogManager.getLogger(UserUsageResource.class.getName());
    private String userId;
    private String endpoint = "/usage/users";
    private APICallCounter counter = APICallCounter.getInstance();

    public void doInit() {
        userId = (String) getRequestAttributes().get("userid");
    }

    /**
     * Get the usage data for the userID mentioned in the URL string
     * <p>
     * Pseudo Code<br/>
     * 1. Extract the QueryValues from the URL<br/>
     * 2. Add the meters name to the meter list<br/>
     * 3. Query the DB to get the usage data and add the response to an array list<br/>
     * 4. Add the array list with its source name into a HashMap<br/>
     * 5. Construct the response and return it in the JSON format
     *
     * @return userUsageResponse Response consisting of the usage data for the request userID
     */
    @Get
    public Representation getData() {
        counter.increment(endpoint);
        logger.trace("BEGIN Representation getData()");
        Representation userUsageResponse;
        TSDBData usageData = null;
        HashMap usageArr = new HashMap();
        ArrayList<TSDBData> meterDataArrList;
        TSDBResource dbResource = new TSDBResource();

        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");

        if (Load.getOpenStackCumulativeMeterList().size() != 0 || Load.getOpenStackGaugeMeterList().size() != 0) {
            meterDataArrList = new ArrayList<TSDBData>();
            //Get the data for the OpenStack Cumulative Meters from the DB and create the arraylist consisting of hashmaps of meter name and usage value
            for (int i = 0; i < Load.getOpenStackCumulativeMeterList().size(); i++) {
                usageData = dbResource.getUsageData(fromDate, toDate, userId, Load.getOpenStackCumulativeMeterList().get(i), "openstack", "cumulative");
                if (usageData != null && usageData.getPoints().size() != 0) {
                    meterDataArrList.add(usageData);
                }
            }
            //Get the data for the OpenStack Gauge Meters from the DB and create the arraylist consisting of hashmaps of meter name and usage value
            for (int i = 0; i < Load.getOpenStackGaugeMeterList().size(); i++) {
                usageData = dbResource.getUsageData(fromDate, toDate, userId, Load.getOpenStackGaugeMeterList().get(i), "openstack", "gauge");
                if (usageData != null && usageData.getPoints().size() != 0) {
                    meterDataArrList.add(usageData);
                }
            }
            if (meterDataArrList.size() != 0) {
                usageArr.put("OpenStack", meterDataArrList);
            }

        }

        if (Load.getExternalMeterList().size() != 0) {
            meterDataArrList = new ArrayList<TSDBData>();
            for (int i = 0; i < Load.getExternalMeterList().size(); i++) {
                usageData = dbResource.getUsageData(fromDate, toDate, userId, Load.getExternalMeterList().get(i), "", "");
                if (usageData != null && usageData.getPoints().size() != 0) {
                    meterDataArrList.add(usageData);
                }
            }
            if (meterDataArrList.size() != 0) {
                usageArr.put("External", meterDataArrList);
            }
        }

        //Construct the response in JSON string
        userUsageResponse = constructResponse(usageArr, userId, fromDate, toDate);
        logger.trace("END Representation getData()");
        return userUsageResponse;
    }

    /**
     * * Construct the JSON response consisting of the meter and the usage values
     * *
     * * Pseudo Code<br/>
     * * 1. Create the HasMap consisting of time range<br/>
     * * 2. Create the response POJO<br/>
     * * 3. Convert the POJO to JSON<br/>
     * * 4. Return the JSON string
     *
     * @param usageArr An arraylist consisting of metername and corresponding usage
     * @param userId   UserID for which the usage details is to be returned.
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate   DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructResponse(HashMap usageArr, String userId, String fromDate, String toDate) {
        String jsonStr;
        JsonRepresentation responseJson = null;

        UserUsageResponse responseObj = new UserUsageResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();

        time.put("from", fromDate);
        time.put("to", toDate);

        //Build the response POJO
        responseObj.setUserid(userId);
        responseObj.setTime(time);
        responseObj.setUsage(usageArr);

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }

}
