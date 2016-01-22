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
package ch.icclab.cyclops.services.iaas.cloudstack.resource.dto;

import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 18-Nov-15
 * Description: POJO template for DTO lists
 */
public abstract class DTOList {
    protected String name;
    protected List<String> columns;
    protected List<List<String>> points;

    /**
     * This constructor will parse influxDB QueryResult and represent it as an object
     *
     * @param data
     */
    public DTOList(QueryResult data) {
        columns = new ArrayList<String>();
        points = new ArrayList<List<String>>();

        Boolean onlyOnce = true;

        try {
            // go over all results
            for (QueryResult.Result result : data.getResults()) {
                // every series
                for (QueryResult.Series serie : result.getSeries()) {
                    name = serie.getName();

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
                                line.add(String.valueOf(value));//TODO: Martin Manu, changed to String.valueOf from (String).
                            }
                        }

                        // and lastly, add them
                        points.add(line);
                    }
                }
            }
        } catch (Exception ignored) {
            // in case of empty QueryResult body do nothing
        }
    }

    // we have to provide also empty constructor
    public DTOList() {
    }

    /**
     * Request proper query for listing all meters
     *
     * @param varargs optional hashmap of arguments
     * @return string
     */
    public abstract String createDBQuery(Map<String, String>... varargs);

    /**
     * Ask for list of objects
     *
     * @return list
     */
    public abstract List getData();
}
