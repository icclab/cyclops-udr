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

package ch.icclab.cyclops.support.database.influxdb.client;

import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Author: Srikanta
 * Created on: 15-Oct-14
 * Upgraded by: Manu
 * Upgraded on: 23-Sep-15
 * Description: Client class for InfluxDB
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class InfluxDBClient extends ClientResource {
    final static Logger logger = LogManager.getLogger(InfluxDBClient.class.getName());

    Load load = new Load();
    String url = load.configuration.get("InfluxDBURL");
    String username = load.configuration.get("InfluxDBUsername");
    String password = load.configuration.get("InfluxDBPassword");
    String dbName = load.configuration.get("dbName");

    /**
     * Saves the data into InfluxDB via HTTP
     * <p/>
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the HTTP protocol, url and auth details
     * 3. Send the data
     *
     * @param data - a JSON representation of the data
     * @return boolean
     */

    public boolean saveData(String data) {
        InfluxDB influxDB = InfluxDBFactory.connect(load.configuration.get("InfluxDBURL"), "root", "root");
        Representation output;
        String[] columns = getColumns(data);
        ArrayList<String[]> points = getPoints(data);
        int meternameIndex = -1;
        int timeIndex = -1;
        int sourceIndex = -1;
        int metersourceIndex = -1;
        int metertypeIndex = -1;
        int statusIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals("metername"))
                meternameIndex = i;
            else if (columns[i].equals("time"))
                timeIndex = i;
            else if (columns[i].equals("source"))
                sourceIndex = i;
            else if (columns[i].equals("metersource"))
                metersourceIndex = i;
            else if (columns[i].equals("metertype"))
                metertypeIndex = i;
            else if (columns[i].equals("status"))
                statusIndex = i;
        }
        logger.debug("Obtained indexes from the columns: Metername: " + meternameIndex + " Time: " + timeIndex + " Source: " + sourceIndex + "Metersource: " + metersourceIndex + " Metertype: " + metersourceIndex + "Status: " + statusIndex);
        for (int i = 0; i < points.size(); i++) {
            logger.debug("Attempting to build the Point: " + points.get(i)[meternameIndex]);
            Point point = Point.measurement(data.split("name\":\"")[1].split("\"")[0])
                    .tag("time", points.get(i)[timeIndex])
                    .tag("source", points.get(i)[sourceIndex].substring(1, points.get(i)[sourceIndex].length() - 1))
                    .tag("metersource", points.get(i)[metersourceIndex].substring(1, points.get(i)[metersourceIndex].length() - 1))
                    .tag("metertype", points.get(i)[metertypeIndex].substring(1, points.get(i)[metertypeIndex].length() - 1))
                    .tag("metername", points.get(i)[meternameIndex].substring(1, points.get(i)[meternameIndex].length() - 1))
                    .field("status", points.get(i)[statusIndex])
                    .build();
            logger.debug("Attempting to write the Point (" + points.get(i)[meternameIndex] + ") in the db:" + dbName);
            influxDB.write(dbName, "default", point);
            logger.debug("Point successfully written.");
        }
        return true;
    }

    public TSDBData getData(String parameterQuery) {
        logger.debug("Attempting to get Data");
        InfluxDB influxDB = InfluxDBFactory.connect(load.configuration.get("InfluxDBURL"), "root", "root");
        JSONArray resultArray;
        JSONObject resultObj;
        TSDBData[] dataObj = null;
        Representation output;
        ObjectMapper mapper = new ObjectMapper();
        int timeIndex = -1;
        int usageIndex = -1;
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Query query = new Query(parameterQuery, dbName);
        try {
            logger.debug("Attempting to execute the query: " + parameterQuery + " into the db: " + dbName);
            resultArray = new JSONArray(influxDB.query(query).getResults());
            logger.debug("Obtained results: " + resultArray.toString());
            if (!resultArray.isNull(0)) {
                if (resultArray.toString().equals("[{}]")) {
                    TSDBData data = new TSDBData();
                    data.setColumns(new ArrayList<String>());
                    data.setPoints(new ArrayList<ArrayList<Object>>());
                    return data;
                } else {
                    JSONObject obj = (JSONObject) resultArray.get(0);
                    JSONArray series = (JSONArray) obj.get("series");
                    String response = series.get(0).toString();
                    response = response.split("values")[0] + "points" + response.split("values")[1];
                    series.put(0, new JSONObject(response));
                    dataObj = mapper.readValue(series.toString(), TSDBData[].class);
                    /**
                     *
                     */
                    for(int i = 0 ; i<dataObj.length ; i++){
                        for(int o = 0 ; o<dataObj[0].getColumns().size(); o++) {
                            if (dataObj[0].getColumns().get(o).equalsIgnoreCase("time"))
                                timeIndex = o;
                            if(dataObj[0].getColumns().get(o).equalsIgnoreCase("usage") || dataObj[0].getColumns().get(o).equalsIgnoreCase("avg"))
                                usageIndex = o;
                        }
                        if(usageIndex>-1) {
                            TreeMap<String, ArrayList> points = new TreeMap<String, ArrayList>();
                            for (ArrayList point : dataObj[0].getPoints()) {
                                if (points.containsKey(point.get(timeIndex))) {
                                    String time = (String) point.get(timeIndex);
                                    Double usage = Double.parseDouble(points.get(time).get(usageIndex).toString());
                                    usage = Double.parseDouble(point.get(usageIndex).toString()) + usage;
                                    point.set(usageIndex, usage);
                                }
                                points.put((String) point.get(timeIndex), point);
                            }
                            ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
                            for (String key : points.keySet()) {
                                result.add(points.get(key));
                            }
                            dataObj[0].setPoints(result);
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataObj[0];
    }

    private Long formatDate(String dateAndTime) {
        Date result = null;
        try {
            String date = dateAndTime.split("T")[0];
            String hour = dateAndTime.split("T")[1];
            hour = hour.substring(0, 8);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = formatter.parse(date + " " + hour);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.getTime();
    }

    private String[] getColumns(String json) {
        String[] result = json.split(":\\[")[1].split("]")[0].split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].substring(1, result[i].length() - 1);
        }
        return result;
    }

    private ArrayList<String[]> getPoints(String json) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] split = json.split(":\\[")[2].split("],\\[");
        split[0] = split[0].substring(1);
        split[split.length - 1] = split[split.length - 1].substring(0, split.length - 2);
        for (int i = 0; i < split.length; i++) {
            result.add(split[i].split(","));
        }
        return result;
    }
}
