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
import ch.icclab.cyclops.services.iaas.openstack.persistence.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.engine.local.Entity;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 01-Apr-15
 * Description:
 *
 */
public class ResourceUsage extends ServerResource{
    private String resourceId;
    public void doInit() {
        resourceId = (String) getRequestAttributes().get("resourceid");
    }
    @Get
    public Representation getResourceUsage(Entity entity){
        String query = null;
        String jsonStr;
        JsonRepresentation responseJson = null;
        TSDBData tsdbData;
        InfluxDBClient dbClient = new InfluxDBClient();
        ResourceUsageResponse resourceUsageResponse = new ResourceUsageResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();

        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");
        time.put("from",fromDate);
        time.put("to",toDate);

        if(Load.openStackCumulativeMeterList.contains(resourceId)){
            query = "SELECT SUM(usage) FROM "+resourceId+" WHERE time > '"+fromDate+"' AND time < '"+toDate+"' GROUP BY userid";
        }else if (Load.openStackGaugeMeterList.contains(resourceId)){
            query = "SELECT MEAN(avg) FROM "+resourceId+" WHERE time > '"+fromDate+"' AND time < '"+toDate+"' GROUP BY userid";
        }else if(Load.externalMeterList.contains(resourceId)){
            query = "SELECT SUM(usage) FROM "+resourceId+" WHERE time > '"+fromDate+"' AND time < '"+toDate+"' GROUP BY userid";
        }else{
            // Fall back response TODO
        }
        tsdbData = dbClient.getData(query);
        resourceUsageResponse.setResourceid(resourceId);
        resourceUsageResponse.setTime(time);
        if(tsdbData != null){
            resourceUsageResponse.setColumn(tsdbData.getColumns());
            resourceUsageResponse.setUsage(tsdbData.getPoints());
        }else{
            resourceUsageResponse.setColumn(null);
            resourceUsageResponse.setUsage(null);
        }

        try {
            jsonStr = mapper.writeValueAsString(resourceUsageResponse);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return responseJson;
    }
}
