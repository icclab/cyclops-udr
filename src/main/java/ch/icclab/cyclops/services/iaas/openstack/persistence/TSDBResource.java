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

package ch.icclab.cyclops.services.iaas.openstack.persistence;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Upgraded by: Manu
 * Upgraded on: 23-Sep-15
 * Description: A RESTLET resource class for handling usage data transformation and
 * persisting into InfluxDB
 * <p>
 * Change Log
 * Name        Date     Comments
 */

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.InfluxDBSettings;
import ch.icclab.cyclops.services.iaas.openstack.model.CumulativeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.Event;
import ch.icclab.cyclops.services.iaas.openstack.model.GaugeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.DatabaseResource;
import ch.icclab.cyclops.usecases.external.model.ExternalDataPoint;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TSDBResource implements DatabaseResource {
    final static Logger logger = LogManager.getLogger(TSDBResource.class.getName());

    InfluxDBSettings settings;
    private InfluxDB influxDB;
    private String dbname;

    public TSDBResource() {
        settings = Loader.getSettings().getInfluxDBSettings();
        influxDB = InfluxDBFactory.connect(settings.getInfluxDBURL(), settings.getInfluxDBUsername(), settings.getInfluxDBPassword());

    }

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     * <p>
     * Pseudo Code
     * 1. Iterate through the data array to save the data into an ArraList of objects
     * 2. Save the data into the TSDB POJO class
     * 3. Convert the POJO class to a JSON obj
     * 4. Invoke the InfluxDb client to save the data
     *
     * @param dataArr   An array list consisting of usage data
     * @param meterName Name of the Gauge Meter
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveGaugeMeterData(ArrayList<GaugeMeterData> dataArr, String meterName) {
        GaugeMeterData gMeterData;
        boolean result = true;
        dbname = settings.getInfluxDBDatabaseName();
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            logger.debug("Data Array length before writing points: " + dataArr.size());
            for (int i = 0; i < dataArr.size(); i++) {
                gMeterData = dataArr.get(i);
                Date date = format.parse(gMeterData.getPeriod_end());
                long timeMillisec = date.getTime();
                logger.debug("Attempting to build a Point for: " + meterName);
                Point point = Point.measurement(meterName)
                        .time(timeMillisec, TimeUnit.MILLISECONDS)
                        .tag("userId", gMeterData.getGroupby().getUser_id())
                        .tag("resourceid", gMeterData.getGroupby().getResource_id())
                        .tag("projectid", gMeterData.getGroupby().getProject_id())
                        .tag("type", "gauge")
                        .field("min", gMeterData.getMin())
                        .field("max", gMeterData.getMax())
                        .field("sum", gMeterData.getSum())
                        .field("avg", gMeterData.getAvg())
                        .tag("unit", gMeterData.getUnit())
                        .field("count", gMeterData.getCount())
                        .build();
                logger.debug("Attempting to write the Point (" + meterName + ")");
                influxDB.write(dbname, "default", point);
                logger.debug("Point successfully written.");
            }
        } catch (Exception e) {
            logger.error("Error while trying to save Gauge meter data into the DB: " + e.getMessage());
            return false;
        }
        return result;
    }

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     * <p>
     * Pseudo Code
     * 1. Iterate through the data array to save the data into an ArraList of objects
     * 2. Save the data into the TSDB POJO class
     * 3. Convert the POJO class to a JSON obj
     * 4. Invoke the InfluxDb client to save the data
     *
     * @param dataArr   An array list consisting of usage data
     * @param meterName Name of the Gauge Meter
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveCumulativeMeterData(ArrayList<CumulativeMeterData> dataArr, String meterName) {
        CumulativeMeterData cMeterData;
        boolean result = true;
        dbname = settings.getInfluxDBDatabaseName();
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            logger.debug("Data Array length before writing points: " + dataArr.size());
            for (int i = 0; i < dataArr.size(); i++) {
                cMeterData = dataArr.get(i);
                Date date = format.parse(cMeterData.getRecorded_at());
                long timeMillisec = date.getTime();
                logger.debug("Attempting to build a Point for: " + meterName);
                Point point = Point.measurement(meterName)
                        .time(timeMillisec, TimeUnit.MILLISECONDS)
                        .tag("userId", cMeterData.getUser_id())
                        .tag("resourceid", cMeterData.getResource_id())
                        .field("volume", cMeterData.getVolume())
                        .field("usage", cMeterData.getUsage())
                        .tag("source", cMeterData.getSource())
                        .tag("project_id", cMeterData.getProject_id())
                        .tag("type", cMeterData.getType())
                        .tag("id", cMeterData.getId())
                        .field("unit", cMeterData.getUnit())
                        .tag("instance_id", cMeterData.getMetadata().getInstance_id())
                        .tag("instance_type", cMeterData.getMetadata().getInstance_type())
                        .tag("mac", cMeterData.getMetadata().getMac())
                        .tag("fref", cMeterData.getMetadata().getFref())
                        .tag("name", cMeterData.getMetadata().getName())
                        .build();
                logger.debug("Attempting to write the Point (" + meterName + ")");
                influxDB.write(dbname, "default", point);
                logger.debug("Point successfully written.");
            }
        } catch (Exception e) {
            logger.error("Error while trying to save Cumulative meter data into the DB: " + e.getMessage());
            return false;
        }
        return result;
    }

    //TODO: save UDR into database.

    public boolean saveUDR(Event event, long usedSeconds, long computeUDRFrom, long computeUDRTo, boolean flagSetupCost) {
        boolean result = false;
        dbname = settings.getInfluxDBDatabaseName();
        DateTimeUtil util = new DateTimeUtil();
        String toDate = util.getDate(computeUDRTo);
        try {
            logger.debug("Attempting to save UDR into the DB.");
            Point point = Point.measurement("UDR")
                    .time(computeUDRFrom, TimeUnit.MILLISECONDS)
                    .field("to", toDate)
                    .tag("clientId", event.getClientId())
                    .tag("providerId", event.getProviderId())
                    .tag("instanceId", event.getInstanceId())
                    .field("usage", usedSeconds)
                    .tag("productType", event.getProductType())
                    .field("status", event.getStatus())
                    .field("flagSetupCost", flagSetupCost)
                    .field("relatives", event.getRelatives())
                    .build();
            logger.debug("Attempting to write UDR point.");
            influxDB.write(dbname, "default", point);
            result = true;
        } catch (Exception e) {
            logger.error("Error while trying to save the UDR into the DB: " + e.getMessage());
            return false;
        }
        return result;
    }

    /**
     * Receives an External data point to save into the DB.
     * Gets it's parameters and saves it.
     *
     * @param externalDataPoint
     * @return
     */
    public boolean saveExtData(ExternalDataPoint externalDataPoint) {
        boolean result = false;
        dbname = settings.getInfluxDBDatabaseName();
        DateTimeUtil util = new DateTimeUtil();
        try {
            logger.debug("Attempting to save UDR into the DB.");
            Point point = Point.measurement(externalDataPoint.getMeterName())
                    .time(externalDataPoint.getTimestamp(), TimeUnit.SECONDS)
                    .tag("userId", externalDataPoint.getUserId())
                    .tag("source", externalDataPoint.getSource())
                    .field("usage", externalDataPoint.getUsage())
                    .build();
            logger.debug("Attempting to write UDR point.");
            influxDB.write(dbname, "default", point);
            result = true;
        } catch (Exception e) {
            logger.error("Error while trying to save the UDR into the DB: " + e.getMessage());
            return false;
        }
        return result;
    }

    /**
     * Ask for data into the DB by a SQL SELECT Query
     *
     * @param from
     * @param to
     * @param userId
     * @param meterName
     * @param source
     * @param type
     * @return
     */
    public TSDBData getUsageData(String from, String to, String userId, Object meterName, String source, String type) {
        String query = null;
        InfluxDBClient dbClient = new InfluxDBClient();
        if (from != null && to != null) {
            String formatedFrom = reformatDate(from);
            String formatedTo = reformatDate(to);
            if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("cumulative")) {
                query = "SELECT usage,unit,type FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userId='" + userId + "' ";
            } else if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("gauge")) {
                query = "SELECT avg,unit,type FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userId='" + userId + "' ";
            } else {
                query = "SELECT time,usage FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userId='" + userId + "' ";
                TSDBData tsdbData = dbClient.getData(query);
                if (tsdbData.getPoints().size() < 1) {
                    query = "SELECT time,usage FROM UDR WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND clientId='" + userId + "' AND productType='" + meterName + "'";
                    tsdbData = dbClient.getData(query);
                    tsdbData.setName((String) meterName);
                    return tsdbData;
                }
            }
        } else {
            if (from == null)
                if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("cumulative")) {
                    query = "SELECT usage,unit,type FROM \"" + meterName + "\" WHERE userId='" + userId + "' ";
                } else if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("gauge")) {
                    query = "SELECT avg,unit,type FROM \"" + meterName + "\" WHERE userId='" + userId + "' ";
                } else {
                    query = "SELECT time,usage FROM \"" + meterName + "\" WHERE userId='" + userId + "' ";
                    TSDBData tsdbData = dbClient.getData(query);
                    if (tsdbData.getPoints().size() < 1) {
                        query = "SELECT time,usage FROM UDR WHERE  clientId='" + userId + "' AND productType='" + meterName + "'";
                        tsdbData = dbClient.getData(query);
                        tsdbData.setName((String) meterName);
                        return tsdbData;
                    }
                }
            else {
                String formatedFrom = reformatDate(from);

                if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("cumulative")) {
                    query = "SELECT usage,unit,type FROM \"" + meterName + "\" WHERE userId='" + userId + "' AND time > '" + formatedFrom + "'";
                } else if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("gauge")) {
                    query = "SELECT avg,unit,type FROM \"" + meterName + "\" WHERE userId='" + userId + "' AND time > '" + formatedFrom + "'";
                } else {
                    query = "SELECT time,usage FROM \"" + meterName + "\" WHERE userId='" + userId + "' AND time > '" + formatedFrom + "'";
                    TSDBData tsdbData = dbClient.getData(query);
                    if (tsdbData.getPoints().size() < 1) {
                        query = "SELECT time,usage FROM UDR WHERE  clientId='" + userId + "' AND productType='" + meterName + "' AND time > '" + formatedFrom + "'";
                        tsdbData = dbClient.getData(query);
                        tsdbData.setName((String) meterName);
                        return tsdbData;
                    }
                }
            }
        }
        return dbClient.getData(query);
    }

    public TSDBData getMeterList() {
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData tsdbData = null;
        Long epoch;

        //TODO: does this have sense? check if we can use select * from meterselection
        tsdbData = dbClient.getData("select * from meterselection");
        return tsdbData;
    }

    private String reformatDate(String date) {
        if (date.contains(" ")) {
            String day = date.split(" ")[0];
            String hour = date.split(" ")[1];
            return day + "T" + hour + ":00Z";
        } else return date;
    }

    /**
     * This method retrieves the last volume value inserted in the DB for a user and a resource.
     *
     * @param metername
     * @param resource_id
     * @param user_id
     * @return
     * @author Manu
     */
    public long getLastVolume(String metername, String resource_id, String user_id) {
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData tsdbData = null;
        ObjectMapper mapper = new ObjectMapper();

        //Get the last entry
        tsdbData = dbClient.getData("SELECT volume FROM \"" + metername + "\" WHERE resourceid='" + resource_id + "' AND userId='" + user_id + "' order by time desc limit 1");
        if (tsdbData.getPoints().size() == 0)
            return 0;
        else {
            int volumeIndex = tsdbData.getColumns().indexOf("volume");
            String volume = tsdbData.getPoints().get(0).get(volumeIndex).toString();
            return new BigDecimal(volume).longValue();
        }
    }

    public boolean saveMcnUDR(Event event, long usedSeconds, long computeUDRFrom, long computeUDRTo, boolean b) {
        boolean result = false;
        dbname = settings.getInfluxDBDatabaseName();
        DateTimeUtil util = new DateTimeUtil();
        String toDate = util.getDate(computeUDRTo);
        try {
            logger.debug("Attempting to save UDR into the DB.");
            Point point = Point.measurement("UDR")
                    .time(computeUDRFrom, TimeUnit.MILLISECONDS)
                    .field("to", toDate)
                    .tag("clientId", event.getClientId())
                    .tag("instanceId", event.getInstanceId())
                    .field("usage", usedSeconds)
                    .tag("productType", event.getProductType())
                    .field("status", event.getStatus())
                    .build();
            logger.debug("Attempting to write UDR point.");
            influxDB.write(dbname, "default", point);
            result = true;
        } catch (Exception e) {
            logger.error("Error while trying to save the UDR into the DB: " + e.getMessage());
            return false;
        }
        return result;
    }
}