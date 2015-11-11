package ch.icclab.cyclops.mcn.impl;

import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.UsageDataRecordResource;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Transform MCN Event record to Usage Data Record entry
 */
public class EventToUDR implements Runnable {
    final static Logger logger = LogManager.getLogger(EventToUDR.class.getName());

    // connection to Database
    private static InfluxDBClient dbClient;

    /**
     * Simple constructor that will create required connections
     */
    public EventToUDR() {
        dbClient = new InfluxDBClient();
    }

    /**
     * Run the thread and process events
     */
    public void run() {
        transformEventsToUDRs();
    }

    /**
     * Transform MCN Events to UDR records
     */
    private void transformEventsToUDRs() {
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData[] tsdbData = null;

        // query for now TODO: what about fromDate?---Manu: we have to use it. SELECT * FROM events LIMIT 1 ORDER BY TIME desc GROUP BY instanceId
        // parameterQuery = "SELECT * FROM events WHERE time >=\""+fromDate+"\"";
        String parameterQuery = "SELECT * FROM events";;

        //Load all events
        String dbname = Load.getInstance().getEnvironment().getEvents_dbname();
        tsdbData = dbClient.query(parameterQuery, dbname);

        //Store clientIDs and instance IDs in map
        HashMap<String,ArrayList<String>> clientInstanceMap = getInstanceIdsPerClientId(tsdbData);

        //Creates UDR records
        createUDRRecords(clientInstanceMap);
    }

    private void createUDRRecords(HashMap<String,ArrayList<String>> clientInstanceMap){
        UsageDataRecordResource udrClient = new UsageDataRecordResource();
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
}
