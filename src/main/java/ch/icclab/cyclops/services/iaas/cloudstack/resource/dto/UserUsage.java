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

import ch.icclab.cyclops.services.iaas.cloudstack.util.Time;
import org.influxdb.dto.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 24-Nov-15
 * Description: POJO object that holds an Usage for selected user
 */
public class UserUsage extends DTOList {

    // let parent parse everything
    public UserUsage(QueryResult data) {
        super(data);
    }

    /**
     * We need to transform strings to longs, as well as "rawusage" to "usage"
     */
    public Boolean normaliseForDashboard() {
        try {
            Integer timeIndex = columns.indexOf("time");
            Integer rawusageIndex = columns.indexOf("rawusage");
            Integer usage = columns.indexOf("usage");

            // iterate over all entries
            for (List<String> row : points) {
                // convert date to milliseconds
                Long timestamp = Time.getMilisForTime(row.get(timeIndex));

                // save it back as a string
                row.set(timeIndex, timestamp.toString());
            }

            if (usage == -1 && rawusageIndex != -1) {
                // and finally rename rawusage to usage
                columns.set(rawusageIndex, "usage");
                return true;
            } else if (usage != -1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignored) {
            // if we are missing required columns
            return false;
        }
    }

    // we are not using these parent methods
    @Override
    public String createDBQuery(Map<String, String>... varargs) {
        return null;
    }

    @Override
    public List getData() {
        return null;
    }
}
