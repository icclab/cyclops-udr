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

import ch.icclab.cyclops.services.iaas.openstack.model.ResourceUsageResponse;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.restlet.engine.local.Entity;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This Class is going to ask to the DB for the Usage Data and give back the CDR.<p/>
 * <p>
 * Author: Srikanta
 * Created on: 01-Apr-15
 * Description:
 */
public class ResourceUsage extends ServerResource {
    final static Logger logger = LogManager.getLogger(ResourceUsage.class.getName());
    private String resourceId;
    private String endpoint = "/usage/resources";
    private APICallCounter counter = APICallCounter.getInstance();

    public void doInit() {
        resourceId = (String) getRequestAttributes().get("resourceid");
    }

    @Get
    public Representation getResourceUsage(Entity entity) {
        counter.increment(endpoint);
        logger.trace("BEGIN Representation getResourceUsage(Entity entity)");
        String query = null;
        String jsonStr;
        JsonRepresentation responseJson = null;
        TSDBData[] tsdbData;
        InfluxDBClient dbClient = new InfluxDBClient();
        DateTimeUtil dateUtil = new DateTimeUtil();
        ResourceUsageResponse resourceUsageResponse = new ResourceUsageResponse();
        ArrayList<ResourceUsageResponse> resourcesArray = new ArrayList<ResourceUsageResponse>();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();
        boolean cumulative = false, gauge = false, sum = false;

        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");
        fromDate = dateUtil.formatDate(fromDate);
        toDate = dateUtil.formatDate(toDate);
        time.put("from", fromDate);
        time.put("to", toDate);

        if (Load.getOpenStackCumulativeMeterList().contains(resourceId)) {
            query = "SELECT usage FROM \"" + resourceId + "\" WHERE time > '" + fromDate + "' AND time < '" + toDate + "' GROUP BY userid";
            sum = true;
        } else if (Load.getOpenStackGaugeMeterList().contains(resourceId)) {
            query = "SELECT MEAN(avg) FROM \"" + resourceId + "\" WHERE time > '" + fromDate + "' AND time < '" + toDate + "' GROUP BY userid";
        } else if (Load.getExternalMeterList().contains(resourceId)) {
            //query = "SELECT SUM(usage) FROM \"" + resourceId + "\" WHERE time > '" + fromDate + "' AND time < '" + toDate + "' GROUP BY userid";
            query = "SELECT usage FROM \"" + resourceId + "\" WHERE time > '" + fromDate + "' AND time < '" + toDate + "' GROUP BY userid";
            sum = true;
        } else {
            // Fall back response TODO
            logger.debug("DEBUG Representation getResourceUsage(Entity entity): No Meter List specified");
        }
        tsdbData = dbClient.getCDRData(query);
        if (sum)
            tsdbData = sumExternalMeterData(tsdbData);
        if (tsdbData != null) {
            for (int i = 0; i < tsdbData.length; i++) {
                //Create a new ResourceUsageResponse
                resourceUsageResponse = new ResourceUsageResponse();
                resourceUsageResponse.setResourceid(resourceId);
                resourceUsageResponse.setTime(time);
                //Add the ResourceUsageResponse to the resourcesArray (initially empty ArrayList)
                resourcesArray.add(resourceUsageResponse);
                resourcesArray.get(i).setColumn(tsdbData[i].getColumns());
                resourcesArray.get(i).setUsage(tsdbData[i].getPoints());
                resourcesArray.get(i).setTags(tsdbData[i].getTags());

            }
        } else {
            logger.debug("DEBUG Representation getResourceUsage(Entity entity): tsdbData is null");
            //TODO: 2 field constructor to set null columns and usage.
            resourceUsageResponse.setResourceid(resourceId);
            resourceUsageResponse.setTime(time);
            resourceUsageResponse.setColumn(null);
            resourceUsageResponse.setUsage(null);
        }

        try {
            jsonStr = mapper.writeValueAsString(resourcesArray);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            logger.error("EXCEPTION JSONPROCESSINGEXCEPTION Representation getResourceUsage(Entity entity)");
            e.printStackTrace();
        }
        return responseJson;
    }

    /**
     * This method sums all the values of the gotten points from the external meter data and sums their values.
     *<br/>
     * Pseudo Code:
     * <br/>
     * 1. Get the column indexes <br/>
     * 2. Aggregate all the points usage<br/>
     * 3. Add the last point time with the aggregation of usages and return it.
     *
     * @param tsdbData
     * @return
     */
    private TSDBData[] sumExternalMeterData(TSDBData[] tsdbData) {
        TSDBData[] result = tsdbData;
        for (int i = 0; i < tsdbData.length; i++) {
            ArrayList<Object> finalPoint = new ArrayList<Object>();
            int usage = 0;
            int usageIndex = tsdbData[i].getColumns().indexOf("usage");
            int timeIndex = tsdbData[i].getColumns().indexOf("time");

            for (int o = 0; o<tsdbData[i].getPoints().size(); o++){
                usage = usage + Integer.parseInt((String)tsdbData[i].getPoints().get(o).get(usageIndex));
                if(o == tsdbData[i].getPoints().size() -1) {
                    String time = (String)tsdbData[i].getPoints().get(o).get(timeIndex);
                    finalPoint.add(timeIndex, time);
                    finalPoint.add(usageIndex, String.valueOf(usage));
                }
            }
            result[i].getPoints().clear();
            result[i].getPoints().add(0, finalPoint);
        }
        return result;
    }

}
