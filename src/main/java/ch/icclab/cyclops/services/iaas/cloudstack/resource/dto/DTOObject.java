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

import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 17-Nov-15
 * Description: POJO template for DTO objects
 */
public abstract class DTOObject {

    /**
     * Create an InfluxDB Point that can be saved into InfluxDB database
     *
     * @return point
     */
    public Point toDBPoint() {
        // get tags without nulls
        Map tags = getObjectTags();
        removeNullValues(tags);

        // get fields without nulls
        Map fields = getObjectFields();
        removeNullValues(fields);

        // start building DB Point
        Point.Builder builder = Point.measurement(getMeasurementName());

        Long time = getTime();
        if (time != null && time > 0) {
            builder = builder.time(time, TimeUnit.MILLISECONDS);
        }

        // finish up and return constructed DB point
        return builder.tag(tags).fields(fields).build();
    }

    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    /**
     * Abstract method that will return tags of the object
     *
     * @return
     */
    protected abstract Map<String, String> getObjectTags();

    /**
     * Abstract method that will return tags of the object
     *
     * @return
     */
    protected abstract Map<String, Object> getObjectFields();

    /**
     * Abstract method that will return name that will be used saving to DB
     *
     * @return
     */
    public abstract String getMeasurementName();

    /**
     * Use this time when saving into DB
     *
     * @return milliseconds in Long or null if you want to skip this
     */
    protected abstract Long getTime();
}
