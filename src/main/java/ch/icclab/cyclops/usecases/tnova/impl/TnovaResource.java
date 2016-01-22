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
import ch.icclab.cyclops.load.model.InfluxDBSettings;
import ch.icclab.cyclops.services.iaas.openstack.model.Response;
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
import org.influxdb.dto.Point;
import org.joda.time.LocalDateTime;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TnovaResource extends ServerResource implements EventProcessingResource {
    final static Logger logger = LogManager.getLogger(TnovaResource.class.getName());
    InfluxDBSettings settings = Loader.getSettings().getInfluxDBSettings();
    private InfluxDB influxDB = InfluxDBFactory.connect(settings.getInfluxDBURL(), settings.getInfluxDBUsername(), settings.getInfluxDBPassword());
    String dbname;
    private String endpoint = "/tnova/refresh";
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
        dbname = Loader.getSettings().gettNovaSettings().getTNovaDBEventsName();
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
        TnovaUsageDataRecordResource udrClient = new TnovaUsageDataRecordResource();
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
     * Logs TSDBData arrays
     *
     * @param tsdbData
     */
    private void logProcessedData(TSDBData[] tsdbData){
        JSONUtil jutil = new JSONUtil();
        try {
            String data = jutil.toJson(tsdbData).getText();
            logger.trace("DATA Representation processEvents(): data="+data);
        } catch (IOException e) {
            logger.error("EXCEPTION IOEXCEPTION Representation processEvents()");
            e.printStackTrace();
        }
    }

    /**
     * Gets last event of clientID/instanceID mappings and returns ArrayList of last events.
     *
     * @param clientInstanceMap
     * @param dbClient
     */
    private TSDBData captureLastEvent(HashMap<String, ArrayList<String>> clientInstanceMap, InfluxDBClient dbClient){
        logger.debug("Attempting to get the Last Event for the customer: ");
        ArrayList<TSDBData> lastEvents = new ArrayList<TSDBData>();
        Iterator it = clientInstanceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String clientId = pair.getKey().toString();
            ArrayList<String> instances = (ArrayList<String>) pair.getValue();
            for(String instance : instances){
                String queryString = "SELECT * FROM events WHERE clientId='" +
                        clientId + "' AND instanceId='" + instance +
                        "' ORDER BY time DESC LIMIT 1";
                logger.trace("private void captureLastEvent(...): query="+queryString);
                TSDBData[] lastEvent = dbClient.query(queryString);
                //sends the event to array
                lastEvents.add(lastEvent[0]);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        return lastEvents.get(0);
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
        dbname = Loader.getSettings().gettNovaSettings().getTNovaDBEventsName();

        String parameterQuery = "SELECT * FROM events WHERE clientId='" +
                clientId + "' AND instanceId='" + instanceID +
                "' AND time<='"+ date +
                "' ORDER BY time DESC LIMIT 1";
        TSDBData [] tsdbData = dbClient.query(parameterQuery,dbname);
        logger.debug("Last Event: "+tsdbData[0].toString());
        return tsdbData[0];
    }

    /**
     * Gets last event of all events after date
     *
     * @param clientId
     * @param instanceID
     * @param date
     * @return
     */
    public TSDBData captureLastEventAfterDate(String clientId, String instanceID, Date date){

        InfluxDBClient dbClient = new InfluxDBClient();
        dbname = Loader.getSettings().gettNovaSettings().getTNovaDBEventsName();

        String parameterQuery = "SELECT * FROM events WHERE clientId='" +
                clientId + "' AND instanceId='" + instanceID +
                "' AND time>='"+ date +
                "' ORDER BY time DESC LIMIT 1";
        TSDBData [] tsdbData = dbClient.query(parameterQuery, dbname);

        return tsdbData[0];
    }

    public TSDBData captureEventsAfterDate(String clientId, String instanceID, Date date){
        ArrayList<TSDBData> result = new ArrayList<TSDBData>();

        InfluxDBClient dbClient = new InfluxDBClient();
        dbname = Loader.getSettings().gettNovaSettings().getTNovaDBEventsName();

        String parameterQuery = "SELECT * FROM events WHERE clientId='" +
                clientId + "' AND instanceId='" + instanceID +
                "' AND time>='"+ date +
                "' ORDER BY time DESC";
        TSDBData [] tsdbData = dbClient.query(parameterQuery, dbname);
//        for(int i = 0; i<tsdbData.length; i++){
//            result.add(tsdbData[i]);
//        }

        return tsdbData[0];
    }

    /**
     * This method creates the response message of the API call
     *
     * @return
     */
    //TODO: remove method
    private Response constructResponse() {
        logger.trace("BEGIN constructResponse(TSDBData data)");
        Response responseObj = new Response();
        LocalDateTime currentDateTime = new LocalDateTime();

        responseObj.setTimestamp(currentDateTime.toDateTime().toString());
        responseObj.setStatus("Success");
        responseObj.setMessage("Event Data retrieved.");
        logger.trace("BEGIN constructResponse(TSDBData data)");
        return responseObj;
    }

    /**
     * This method takes the POJOobject that contains all events, extracts all clientIDs
     * and saves it to an ArrayList.
     *
     * @param tsdbData
     * @return
     */
    private ArrayList<String> getClientIds(TSDBData[] tsdbData){
        logger.trace("BEGIN ArrayList<String> getClientIds(TSDBData[] tsdbData)");
        ArrayList<String> result = new ArrayList<String>();
        for(TSDBData obj : tsdbData){
            ArrayList<String> columns = obj.getColumns();
            ArrayList<ArrayList<Object>> points = obj.getPoints();
            int clientidIndex = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals("clientid"))
                    clientidIndex = i;
            }
            for (int i = 0; i < points.size(); i++) {
                String clientId = points.get(i).get(clientidIndex).toString();
                if (!(result.contains(clientId))){
                    result.add(clientId);
                }
            }
        }
        logger.trace("END ArrayList<String> getClientIds(TSDBData[] tsdbData)");
        return result;
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
                if (columns.get(i).equals("clientid"))
                    clientidIndex = i;
                else if (columns.get(i).equals("instanceid"))
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

    /**
     * Saves an event POJO to InfluxDB. only for debugging
     *
     * @param tsdbData
     */
    //TODO: only for debugging, move to InfluxDBClient
    private void saveCaughtEvents(TSDBData[] tsdbData){
        logger.trace("BEGIN void saveCaughtEvents(TSDBData[] tsdbData)");
        for(TSDBData obj : tsdbData){
            ArrayList<String> columns = obj.getColumns();
            ArrayList<ArrayList<Object>> points = obj.getPoints();
            int timeIndex = -1;
            int statusIndex = -1;
            int clientidIndex = -1;
            int instanceidIndex = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals("time"))
                    timeIndex = i;
                else if (columns.get(i).equals("clientid"))
                    clientidIndex = i;
                else if (columns.get(i).equals("instanceid"))
                    instanceidIndex = i;
                else if (columns.get(i).equals("status"))
                    statusIndex = i;
            }
            for (int i = 0; i < points.size(); i++) {
                String tablename = "caught_events";

                String clientId = points.get(i).get(clientidIndex).toString();
                String instanceId = points.get(i).get(instanceidIndex).toString();
                dbname = Loader.getSettings().getInfluxDBSettings().getInfluxDBDatabaseName();

                String lastevent = points.get(i).get(statusIndex).toString();
                if (lastevent.equals(Loader.getSettings().gettNovaSettings().getTNovaEventStart())){
                    lastevent = "start";
                }
                else if ((lastevent.equals("stopped")) || (lastevent.equals("STOP"))){
                    lastevent = "stop";
                }
                try {

                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(points.get(i).get(timeIndex).toString());
                    long timeMillisec = date.getTime();
                    System.out.println(timeMillisec);
                    System.out.println(lastevent);
                    Point point = Point.measurement(tablename)
                            .field("time", timeMillisec)
                            .tag("userid", clientId)
                            .tag("instanceid", instanceId)
                            .field("lastevent", lastevent)
                            .build();
                    logger.debug("Attempting to write the Point (" + points.get(i).get(timeIndex) + ") in the db:" + dbname);
                    influxDB.write(Loader.getSettings().gettNovaSettings().getTNovaDBEventsName(), "default", point);
                    logger.debug("Point successfully written.");
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        logger.trace("END void saveCaughtEvents(TSDBData[] tsdbData)");
    }
}
