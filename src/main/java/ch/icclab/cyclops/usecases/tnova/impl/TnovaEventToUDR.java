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
package ch.icclab.cyclops.usecases.tnova.impl;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Transform Tnova Event record to Usage Data Record entry
 */
public class TnovaEventToUDR extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(TnovaEventToUDR.class.getName());

    // connection to Database
    private static InfluxDBClient dbClient;

    /**
     * Simple constructor that will create required connections
     */
    public TnovaEventToUDR() {
        dbClient = new InfluxDBClient();
    }

    /**
     * Run the thread and process events
     */
    @Override
    public void run() {
        transformEventsToUDRs();
    }

    /**
     * Transform Tnova Events to UDR records
     */
    private void transformEventsToUDRs() {
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData[] tsdbData = null;

        // query for now TODO: what about fromDate?---Manu: we have to use it. SELECT * FROM events LIMIT 1 ORDER BY TIME desc GROUP BY instanceId
        // parameterQuery = "SELECT * FROM events WHERE time >=\""+fromDate+"\"";
        String parameterQuery = "SELECT * FROM events";
        ;

        //Load all events
        String dbname = Loader.getSettings().gettNovaSettings().getTNovaDBEventsName();
        tsdbData = dbClient.query(parameterQuery, dbname);

        //Store clientIDs and instance IDs in map
        HashMap<String, ArrayList<String>> clientInstanceMap = getInstanceIdsPerClientId(tsdbData);

        //Creates UDR records
        createUDRRecords(clientInstanceMap);
    }

    private void createUDRRecords(HashMap<String, ArrayList<String>> clientInstanceMap) {
        TnovaUsageDataRecordResource udrClient = new TnovaUsageDataRecordResource();
        Iterator it = clientInstanceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String clientId = pair.getKey().toString();
            ArrayList<String> instanceIds = (ArrayList<String>) pair.getValue();
            for (String instanceId : instanceIds) {
                udrClient.generateUDR(clientId, instanceId);
            }
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove();
        }
    }

    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     * and maps instanceIds to them which are saved to a HashMap.
     *
     * @param tsdbData
     * @return
     */
    private HashMap<String, ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData) {
        logger.trace("BEGIN HashMap<String,ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData)");
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        for (TSDBData obj : tsdbData) {
            ArrayList<String> columns = obj.getColumns();
            ArrayList<ArrayList<Object>> points = obj.getPoints();
            int clientidIndex = -1;
            int instanceidIndex = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals("clientId"))
                    clientidIndex = i;
                else if (columns.get(i).equals("instanceId"))
                    instanceidIndex = i;
            }
            for (int i = 0; i < points.size(); i++) {
                String clientId = points.get(i).get(clientidIndex).toString();
                String InstanceId = points.get(i).get(instanceidIndex).toString();
                if (!(map.containsKey(clientId))) {
                    map.put(clientId, new ArrayList<String>());
                    if (!(map.get(clientId).contains(InstanceId))) {
                        map.get(clientId).add(InstanceId);
                    }
                } else {
                    if (!(map.get(clientId).contains(InstanceId))) {
                        map.get(clientId).add(InstanceId);
                    }

                }
            }
        }
        return map;
    }

}
