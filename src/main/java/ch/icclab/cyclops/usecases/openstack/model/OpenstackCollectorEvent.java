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
package ch.icclab.cyclops.usecases.openstack.model;

import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Serhiienko Oleksii
 * Created on: 5-April-16
 */
public class OpenstackCollectorEvent {
    private String userName;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String instanceId;


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    private String action;


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String time;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String service_type;


    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    private String clientId;


    public String getClientID() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private ObjectData objectData;


    public ObjectData getObjectData() {
        return objectData;
    }

    public void setObjectData(ObjectData objectData) {
        this.objectData = objectData;
    }

    public static class ObjectData{
        public Double id;
        public Double memory;
        public Double vcpus;

        public void setId(Double id) {this.id = id;}

        public void setMemory(Double memory) {this.memory = memory;}

        public void setVcpus(Double vcpus) {this.vcpus = vcpus;}

        public Double getId() {
            return id;
        }

        public Double getMemory() {
            return memory;
        }

        public Double getVcpus() {
            return vcpus;
        }

    }

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

        map.put("instanceId", instanceId);
        map.put("clientId", userName);
        map.put("productType", service_type);



        return map;
    }

    /**
     * Get fields for point generation
     *
     * @return hashmap
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("status", action);
        map.put("memory", objectData.memory.toString());
        map.put("cpu", objectData.vcpus.toString());


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

}
