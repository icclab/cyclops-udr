package ch.icclab.cyclops.services.iaas.cloudstack.resource.impl;

import ch.icclab.cyclops.services.iaas.cloudstack.model.StandardMeter;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.MeterList;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.QueryResult;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 24/02/16.
 */
public class CloudStackMeterV2 extends ServerResource {
    final static Logger logger = LogManager.getLogger(CloudStackMeterV2.class.getName());

    // some required variables
    private final APICallCounter counter;
    private final static String endpoint = "/meters";
    private final InfluxDBClient dbClient;

    public CloudStackMeterV2() {
        counter = APICallCounter.getInstance();
        dbClient = new InfluxDBClient();
    }

    @Get
    public String getMeterList() {
        counter.increment(endpoint);
        logger.trace("Meterlist v2 Request");

        StandardMeter[] meterList = retrieveMeterList();

        String json = new Gson().toJson(meterList);
        // return json
        logger.trace("Serving CloudStack meterList v2 back");
        return json;
    }

    /**
     * Ask for meterList from database
     *
     * @return MeterList object
     */
    protected StandardMeter[] retrieveMeterList() {
        String query = new MeterList().createDBQuery();
        // run query
        QueryResult result = dbClient.runQuery(query);
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<List<String>> points = new ArrayList<List<String>>();
        Boolean onlyOnce = true;
        int nameIndex, sourceIndex;
        ArrayList<StandardMeter> meters = new ArrayList<StandardMeter>();
        try {
            // go over all results
            for (QueryResult.Result data : result.getResults()) {
                // every series
                for (QueryResult.Series serie : data.getSeries()) {
                    Map<String, String> tags = serie.getTags();
                    // add all column names
                    if (onlyOnce) {
                        if (tags != null) {
                            columns.addAll(tags.keySet());
                        }
                        columns.addAll(serie.getColumns());
                        onlyOnce = false;
                    }
                    // iterate over values
                    for (List<Object> values : serie.getValues()) {
                        List<String> line = new ArrayList<String>();
                        // don't forget to manually add tags here
                        if (tags != null) {
                            line.addAll(tags.values());
                        }
                        // cast them to String
                        for (Object value : values) {
                            if (value == null) {
                                line.add("");
                            } else {
                                line.add(String.valueOf(value));
                            }
                        }
                        // and lastly, add them
                        points.add(line);
                    }
                }
            }
            nameIndex = columns.indexOf("metername");
            sourceIndex = columns.indexOf("metersource");
            for(List<String> point : points){
                meters.add(new StandardMeter(point.get(nameIndex), point.get(sourceIndex)));
            }
        } catch (Exception ignored) {
            // in case of empty QueryResult body do nothing
        }
        StandardMeter[] ret = new StandardMeter[meters.size()];
        ret = meters.toArray(ret);
        return ret;
    }
}
