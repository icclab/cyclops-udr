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
 * Created on: 17-Nov-15
 * Description: POJO object that holds meter selection
 */
public class MeterList extends DTOList {

    /**
     * Let parent parse everything
     *
     * @param query result
     */
    public MeterList(QueryResult query) {
        super(query);
    }

    public MeterList() {
        super();
    }

    @Override
    public List<Meter> getData() {
        List<Meter> meterList = new ArrayList<Meter>();

        // get indexes
        Integer time = columns.indexOf("time");
        Integer metersource = columns.indexOf("metersource");
        Integer metertype = columns.indexOf("metertype");
        Integer source = columns.indexOf("source");
        Integer status = columns.indexOf("status");
        Integer metername = columns.indexOf("metername");

        // iterate over all points
        for (List<String> row : points) {
            Meter meter = new Meter();

            // populate everything
            meter.setTime(row.get(time));
            meter.setMetersource(row.get(metersource));
            meter.setMetertype(row.get(metertype));
            meter.setSource(row.get(source));
            meter.setStatus(row.get(status));
            meter.setMetername(row.get(metername));

            // add it to the list
            meterList.add(meter);
        }

        return meterList;
    }

    /**
     * Extract list of meter names
     *
     * @return list
     */
    public List<String> getMeterNames() {
        List<String> nameList = new ArrayList<String>();

        Integer column = columns.indexOf("metername");

        for (List<String> row : points) {
            nameList.add(row.get(column));
        }

        return nameList;
    }

    /**
     * Access only meters that are enabled
     *
     * @return list
     */
    public List<String> getEnabledMeterNames() {
        List<String> nameList = new ArrayList<String>();

        Integer column = columns.indexOf("metername");
        Integer status = columns.indexOf("status");

        for (List<String> row : points) {
            if (row.get(status).equalsIgnoreCase("1")) {
                nameList.add(row.get(column));
            }
        }

        return nameList;
    }

    @Override
    public String createDBQuery(Map<String, String>... varargs) {
        return "select * from " + new Meter().getMeasurementName() + " group by " + Meter.getGroupByName() + " order by desc limit 1";
    }
}
