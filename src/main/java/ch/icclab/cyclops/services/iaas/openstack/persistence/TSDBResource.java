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
 * <p/>
 * Change Log
 * Name        Date     Comments
 */

import ch.icclab.cyclops.services.iaas.openstack.model.CumulativeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.GaugeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.DatabaseResource;
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

    private InfluxDB influxDB = InfluxDBFactory.connect(Load.configuration.get("InfluxDBURL"), "root", "root");
    private String dbname;

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     * <p/>
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
        dbname = Load.configuration.get("dbName");
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
                        .tag("userid", gMeterData.getGroupby().getUser_id())
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
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * Receives the usage data array and the gauge meter name. The usage data is transformed
     * to a json data and saved into the the InfluxDB
     * <p/>
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
        dbname = Load.configuration.get("dbName");
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
                        .tag("userid", cMeterData.getUser_id())
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
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * Receives the transformed usage data from an external application in terms of an TSDBData POJO.
     * POJO is converted into a json object and the InfluxDB client is invoked to persist the data.
     * <p/>
     * Pseudo Code
     * 1. Convert the TSDB POJO consisting of the usage data into a JSON Obj
     * 2. Invoke the InfluxDB client
     * 3. Save the data in to the DB
     *
     * @param dbData
     * @return result A boolean output as a result of saving the meter data into the db
     */
    public boolean saveExtData(TSDBData dbData) {
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData;
        boolean result = true;

        try {
            jsonData = mapper.writeValueAsString(dbData);
            dbClient.saveData(jsonData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = false;
            return result;
        }
        return result;
    }

    public TSDBData getUsageData(String from, String to, String userId, Object meterName, String source, String type) {
        String query = null;
        InfluxDBClient dbClient = new InfluxDBClient();
        String formatedFrom = reformatDate(from);
        String formatedTo = reformatDate(to);
        if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("cumulative")) {
            query = "SELECT usage,unit,type FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userid='" + userId + "' ";
        } else if (source.equalsIgnoreCase("openstack") && type.equalsIgnoreCase("gauge")) {
            query = "SELECT avg,unit,type FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userid='" + userId + "' ";
        } else {
            query = "SELECT timestamp,usage FROM \"" + meterName + "\" WHERE time > '" + formatedFrom + "' AND time < '" + formatedTo + "' AND userid='" + userId + "' ";
        }

        return dbClient.getData(query);
    }

    public TSDBData getMeterList() {
        InfluxDBClient dbClient = new InfluxDBClient();
        TSDBData tsdbData = null;
        Long epoch;

        //Get the first entry
        tsdbData = dbClient.getData("select * from meterselection limit 1");
        // Extract the time of the first entry
        if (tsdbData.getPoints().size() != 0) {
            Date date = this.formatDate((String) tsdbData.getPoints().get(0).get(0));
            epoch = date.getTime();
            // Use the extracted epoch time to get all the data entry
            tsdbData = dbClient.getData("select * from meterselection " + "where time > " + epoch + "ms");
        }
        return tsdbData;
    }

    private Date formatDate(String dateAndTime) {
        Date result = null;
        try {
            String date = dateAndTime.split("T")[0];
            String hour = dateAndTime.split("T")[1];
            hour = hour.substring(0, 8);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            result = formatter.parse(date + " " + hour);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String reformatDate(String date) {
        String day = date.split(" ")[0];
        String hour = date.split(" ")[1];
        return day + "T" + hour + ":00Z";
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
        tsdbData = dbClient.getData("SELECT volume FROM \"" + metername + "\" WHERE resourceid='" + resource_id + "' AND userid='" + user_id + "' order by time desc limit 1");
        if (tsdbData.getPoints().size() == 0)
            return 0;
        else {
            int volumeIndex = tsdbData.getColumns().indexOf("volume");
            String volume = tsdbData.getPoints().get(0).get(volumeIndex).toString();
            return new BigDecimal(volume).longValue();
        }
    }
}