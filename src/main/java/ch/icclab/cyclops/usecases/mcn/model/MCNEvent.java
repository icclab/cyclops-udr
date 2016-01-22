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
package ch.icclab.cyclops.usecases.mcn.model;

import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Date: 06/11/2015
 * Description: This class holds the MCN Event data
 */
public class MCNEvent {
    private String service_type;
    private String instance_id;
    private String status;
    private String tenant_id;

    /**
     * This public method will access data and create db Point
     *
     * @return db point
     */
    public Point toPoint() {
        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        return Point.measurement(getMeterName())
                .tag(tags)
                .fields(fields)
                .build();
    }

    /**
     * Get tags for point generation
     *
     * @return hashmap
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("clientId", tenant_id);
        map.put("productType", service_type);
        map.put("instanceId", instance_id);

        return map;
    }

    /**
     * Get fields for point generation
     *
     * @return hashmap
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("status", status);

        return map;
    }

    /**
     * @return meter name
     */
    private String getMeterName() {
        return "events";
    }

    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }


    //===== Setters and Getters


    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }
}
