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

import ch.icclab.cyclops.services.iaas.cloudstack.model.UsageData;
import org.influxdb.dto.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by skoviera on 18/11/15.
 */
public class ResourceList extends DTOList {

    // we are using parent constructors
    public ResourceList(QueryResult data) {
        super(data);
    }

    public ResourceList() {
    }

    @Override
    public String createDBQuery(Map<String, String>... varargs) {

        String query = "select " + getColumnNames() + " from ";

        if (varargs.length > 0) {
            Map<String, String> map = varargs[0];

            // add from parameter
            query = query.concat('"' + map.get("from") + '"');

            // and optionally also start/end dates
            if (map.containsKey("start")) {
                query = query.concat(" WHERE time > '" + map.get("start") + "'");

                if (map.containsKey("end")) {
                    query = query.concat(" AND time < '" + map.get("end") + "'");
                }
            }
        }

        return query;
    }

    @Override
    public List getData() {
        //TODO not implemented yet, because I am not sure if we need to save it or access it - as of now ResourceList is just a proxy
        return null;
    }

    private String getColumnNames() {
        return UsageData.getUsageColumnName() + "," + UsageData.getProjectIdColumnName() + "," + UsageData.getAccountColumnName();
    }
}
