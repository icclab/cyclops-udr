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
package ch.icclab.cyclops.usecases.openstack.model;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.InfluxDBSettings;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.EventProcessingResource;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.JSONUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Serhiienko Oleksii
 * Created on: 5-April-16
 */
public class OpenstackCollectorResource extends ServerResource implements EventProcessingResource {
    final static Logger logger = LogManager.getLogger(OpenstackCollectorResource.class.getName());
    InfluxDBSettings settings = Loader.getSettings().getInfluxDBSettings();
    private InfluxDB influxDB = InfluxDBFactory.connect(settings.getInfluxDBURL(), settings.getInfluxDBUsername(), settings.getInfluxDBPassword());
    String dbname;
    private String endpoint = "/mcn/refresh";
    private APICallCounter counter = APICallCounter.getInstance();
    /**
     * This method reads all events in the polling frame, processes them and stores the UDRs in InfluxDB.
     *
     * @return stores events in UDRs
     */

    @Get
    public Representation processEvents() {
        counter.increment(endpoint);
        TSDBData result = null;
        Representation jsonOutput = null;
        JSONUtil jutil = new JSONUtil();

        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData[] tsdbData = null;

        String fromDate = "";
        try {
            fromDate = getQueryValue("from");
        } catch(NullPointerException ex){
            fromDate = "";
        }
        String parameterQuery = "";

        if (fromDate == null){
            fromDate = "";
        }
        if (fromDate.equals("")){
            parameterQuery = "SELECT * FROM events";
        }else{
            fromDate = fromDate.replaceAll("\"","");
            parameterQuery = "SELECT * FROM events WHERE time >=\""+fromDate+"\"";
        }

        //Load all events
        dbname = Loader.getSettings().getMcnSettings().getMCNDBEventsName();
        tsdbData = dbClient.query(parameterQuery, dbname);

        //Store clientIDs and instance IDs in map
        HashMap<String,ArrayList<String>> clientInstanceMap = getInstanceIdsPerClientId(tsdbData);

        //Save lastevent to InfluxDB. Manu: not saving anything
        //result = captureLastEvent(clientInstanceMap, dbClient);

        //Creates UDR records
        createUDRRecords(clientInstanceMap);
        return jsonOutput;
    }

    private void createUDRRecords(HashMap<String,ArrayList<String>> clientInstanceMap){
        OpenstackCollectorUsageDataRecordResource udrClient = new OpenstackCollectorUsageDataRecordResource();
        Iterator it = clientInstanceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String clientId = pair.getKey().toString();
            ArrayList<String> instanceIds = (ArrayList<String>) pair.getValue();
            for(String instanceId : instanceIds){
                udrClient.generateUDR(clientId, instanceId);
            }
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove();
        }
    }

    /**
     * Gets last event before Date.
     *
     * @param clientId
     * @param instanceID
     * @param date
     * @return
     */
    public TSDBData captureLastEventBeforeDate(String clientId, String instanceID, long date){// Date date){

        DateTimeUtil util = new DateTimeUtil();
        InfluxDBClient dbClient = new InfluxDBClient();
        dbname = Loader.getSettings().getOpenstackCollectorSettings().getOpenstackCollectorDBEventsName();

        String parameterQuery = "SELECT * FROM events WHERE clientId='" +
                clientId + "' AND instanceId='" + instanceID +
                "' AND time<='"+ date +
                "' ORDER BY time DESC LIMIT 1";
        TSDBData[] tsdbData = dbClient.query(parameterQuery,dbname);
        logger.debug("Last Event: "+tsdbData[0].toString());
        return tsdbData[0];
    }

    public TSDBData captureEventsAfterDate(String clientId, String instanceID, Date date){
        ArrayList<TSDBData> result = new ArrayList<TSDBData>();

        InfluxDBClient dbClient = new InfluxDBClient();
        dbname = Loader.getSettings().getOpenstackCollectorSettings().getOpenstackCollectorDBEventsName();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        String parameterQuery = "SELECT * FROM events WHERE clientId='" +
                clientId + "' AND instanceId='" + instanceID +
                "' AND time>='"+ format.format(date) +
                "' ORDER BY time DESC";
        TSDBData[] tsdbData = dbClient.query(parameterQuery, dbname);
//        for(int i = 0; i<tsdbData.length; i++){
//            result.add(tsdbData[i]);
//        }

        return tsdbData[0];
    }
    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     * and maps instanceIds to them which are saved to a HashMap.
     *
     * @param tsdbData
     * @return
     */
    private HashMap<String,ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData){
        logger.trace("BEGIN HashMap<String,ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData)");
        HashMap<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
        for(TSDBData obj : tsdbData){
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
                if (!(map.containsKey(clientId))){
                    map.put(clientId, new ArrayList<String>());
                    if (!(map.get(clientId).contains(InstanceId))){
                        map.get(clientId).add(InstanceId);
                    }
                } else {
                    if (!(map.get(clientId).contains(InstanceId))){
                        map.get(clientId).add(InstanceId);
                    }

                }
            }
        }
        logger.trace("END HashMap<String,ArrayList<String>> getInstanceIdsPerClientId(TSDBData[] tsdbData)");
        return map;
    }

}
