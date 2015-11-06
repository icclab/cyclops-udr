package ch.icclab.cyclops.services.iaas.openstack.resource.impl;

import ch.icclab.cyclops.services.iaas.openstack.model.Event;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.JSONUtil;
import ch.icclab.cyclops.util.Load;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Manu
 *         Created on 20-Oct-2015
 */
public class UsageDataRecordResource extends ServerResource {
    final static Logger logger = LogManager.getLogger(UsageDataRecordResource.class.getName());
    private String endpoint = "/mcn/usage";
    private APICallCounter counter = APICallCounter.getInstance();

    public UsageDataRecordResource() {
        super();
        logger.trace("Object created");
    }


    @Get
    public Representation getUDR() {
        counter.increment(endpoint);
        try {
            Representation jsonOutput = null;
            String query = null;
            TSDBData[] tsdbData;
            InfluxDBClient dbClient = new InfluxDBClient();

            String fromDate = getQueryValue("from");
            String toDate = getQueryValue("to");
            logger.debug("fromDate: " + fromDate);
            logger.debug("toDate: " + toDate);
            if (fromDate == null)
                query = "SELECT * FROM UDR group by clientId";
            else {
                fromDate = fromDate.replace("T", " ");
                if (toDate == null) {
                    query = "SELECT * FROM UDR WHERE time > \'" + fromDate + "\' group by clientId";
                } else {
                    toDate = toDate.replace("T", " ");
                    query = "SELECT * FROM UDR WHERE time > \'" + fromDate + "\'" +
                            " AND time < \'" + toDate + "\' group by clientId";//changed toDate for fromDate so we just ask for the UDR started in within the time window
                }
            }
            tsdbData = dbClient.query(query, Load.configuration.get("dbName"));
            JSONUtil jutil = new JSONUtil();
            jsonOutput = jutil.toJson(tsdbData);

            String envelop = "{ \"udr_records\": ";
            JsonRepresentation result;
            if (tsdbData != null && tsdbData[0] != null && tsdbData[0].getName() != null) {
                result = new JsonRepresentation(envelop.concat(jsonOutput.getText()).concat("}"));
            } else {
                result = new JsonRepresentation(envelop.concat("[]}"));
            }
            return result;
        } catch (Exception e) {
            logger.error("Error while getting the UDR from Database: " + e.getMessage());
        }
        return null;
    }

    public void generateUDR(String clientId, String instanceId) {
        //TODO: we can't rely on the external systems calls for data consistency (regarding to the getRange called by the scheduler.
        MCNResource MCNResource = new MCNResource();
        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        String[] range = dateTimeUtil.getRange();
        long computeUDRFrom = dateTimeUtil.getEpoch(range[0]);
        Event lastEvent = fromTSDBtoEvents(MCNResource.captureLastEventBeforeDate(clientId, instanceId, computeUDRFrom)).get(0);

        computeUDR(lastEvent);
    }

    /**
     * This method is going to compute the UDR depending on the last Event
     * <br>
     * Pseudo code:<br>
     * 1. Check if the last Event was inside the time frame which we want the udr to be generated.<br>
     * 2. If it's outside a whole time period is used for generating the UDR.<br>
     * 3. Else the time is computed and the UDR generated for the needed time.
     *
     * @param event Last Event in the database.
     * @return
     */
    //TODO: getRange could be delayed here from the last call in the previous method
    public void computeUDR(Event event) {// (Representation entity) {
        try {
            TSDBResource tsdbResource = new TSDBResource();
            DateTimeUtil dateTimeUtil = new DateTimeUtil();
            String[] range = dateTimeUtil.getRange();
            long computeUDRFrom = dateTimeUtil.getEpoch(range[0]);
            long computeUDRTo = dateTimeUtil.getEpoch(range[1]);
            String instanceId = event.getInstanceId();
            String clientId = event.getClientId();
            MCNResource MCNResource = new MCNResource();
            //Get last event date.
            long lastEventDate = dateTimeUtil.getEpoch(event.getDateModified());
            double price = 0.0;
            long usedSeconds;
            logger.debug("Attempting to compute the Usage time and save it into the DB.");
            if (lastEventDate < computeUDRFrom) {
                usedSeconds = computeUsageNoEvents(event);
                tsdbResource.saveUDR(event, usedSeconds, computeUDRFrom, computeUDRTo, false);
            } else {
                //Check if i only have 1 event in the time period
                ArrayList<Event> events = fromTSDBtoEvents(MCNResource.captureEventsAfterDate(clientId, instanceId, new Date(computeUDRFrom)));
                if (events.size() > 1) {
                    computeAndSaveUsageMultipleEvents(events, dateTimeUtil, tsdbResource, computeUDRFrom, computeUDRTo);
                } else {
                    computeAndSaveOneEvent(events, dateTimeUtil, tsdbResource, computeUDRFrom, computeUDRTo, event);
                }
            }
        } catch (Exception e) {
            logger.error("Error while computing the UDR: " + e.getMessage());
        }
    }

    private long computeUsageNoEvents(Event event) {
        logger.debug("Computing the time without any event on the time frame.");
        if (event.getStatus().equals("start"))
            //Generate the UDR with the whole time period as used time
            return Integer.parseInt(Load.configuration.get("sensuFrequency"));
        else
            //Generate the UDR with the 0 as used time
            return 0;
    }

    private void computeAndSaveOneEvent(ArrayList<Event> events, DateTimeUtil dateTimeUtil, TSDBResource tsdbResource, long computeUDRFrom, long computeUDRTo, Event event) {
        logger.debug("Computing the time with 1 event on the time frame.");
        long usedSeconds;
        long eventTime = dateTimeUtil.getEpoch(events.get(0).getDateModified());
        boolean flagSetupCost = false;
        if (event.getStatus().equals("running")) {
            //generate UDR from event.time to "to"
            usedSeconds = (computeUDRTo - eventTime);
            if (event.getDateModified().equals(event.getDateCreated())) {
                flagSetupCost = true;
            }
        } else {
            //generate UDR from "from" to event.time.
            usedSeconds = (eventTime - computeUDRFrom);
        }
        tsdbResource.saveUDR(events.get(0), usedSeconds, computeUDRFrom, computeUDRTo, flagSetupCost);
    }

    private void computeAndSaveUsageMultipleEvents(ArrayList<Event> events, DateTimeUtil dateTimeUtil, TSDBResource tsdbResource, long computeUDRFrom, long computeUDRTo) {
        logger.debug("Computing the time with " + events.size() + " events on the time frame.");
        long usedSeconds;
        boolean flagSetupCost = false;
        for (int i = 0; i < events.size(); i++) {
            if (!events.get(i).getStatus().equals("running")) {
                //generate udr from UDR FROM time to events.get(i).time
                long eventTime = dateTimeUtil.getEpoch(events.get(i).getDateModified());
                usedSeconds = (eventTime - computeUDRFrom);
            } else {
                if (i < events.size() - 1) {
                    //generate udr FROM event(i).time to event(i+1).time
                    long timeFrom = dateTimeUtil.getEpoch(events.get(i).getDateModified());
                    long timeTo = dateTimeUtil.getEpoch(events.get(i + 1).getDateModified());
                    usedSeconds = (timeTo - timeFrom);
                } else {
                    //generate udr FROM event(i).time to TO
                    long timeFrom = dateTimeUtil.getEpoch(events.get(i).getDateModified());
                    usedSeconds = (computeUDRTo - timeFrom);
                    if (events.get(i).getDateModified().equals(events.get(i).getDateCreated()))
                        flagSetupCost = true;
                }
            }
            //Generate UDR
            tsdbResource.saveUDR(events.get(i), usedSeconds, computeUDRFrom, computeUDRTo, flagSetupCost);
        }
    }

    private ArrayList<Event> fromTSDBtoEvents(TSDBData tsdbData) {
        logger.debug("TSDBData: " + Arrays.toString(tsdbData.getColumns().toArray()));
        logger.debug("TSDBData: " + Arrays.toString(tsdbData.getPoints().toArray()));
        ArrayList<String> columns = tsdbData.getColumns();
        ArrayList<ArrayList<Object>> points = tsdbData.getPoints();
        ArrayList<Event> events = new ArrayList<Event>();
        HashMap<String, Integer> columnMap = new HashMap<String, Integer>();
        int i = 0;
        for (String column : columns) {
            columnMap.put(column, i);
            i++;
        }
        for (ArrayList<Object> point : points) {
            Event event = new Event();
            event.setClientId((String) point.get(columnMap.get("clientid")));
            event.setInstanceId((String) point.get(columnMap.get("instanceid")));
            event.setProductType((String) point.get(columnMap.get("producttype")));
            event.setStatus((String) point.get(columnMap.get("status")));
            event.setDateModified((String) point.get(columnMap.get("time")));

            events.add(event);
        }
        return events;
    }
}
