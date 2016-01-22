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
package ch.icclab.cyclops.services.iaas.openstack.model;

import ch.icclab.cyclops.util.DateTimeUtil;
import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Manu
 *         Created by root on 03.11.15.
 */
public class UDREntry {
    private String name = "tnova_udr";
    private long time;
    private String clientId;
    private String instanceId;
    private String productId;
    private String productType;
    private String to;
    private long usedSeconds;
    private boolean flagSetupCost;

    /**
     * Construct and populate UDREntry object
     *
     * @param event object
     */
    public UDREntry(Event event, long usedSeconds, long computeUDRFrom, long computeUDRTo, boolean flagSetupCost) {
        this.time = computeUDRFrom;
        this.clientId = event.getClientId();
        this.instanceId = event.getInstanceId();
        this.productId = event.getProductId();
        this.productType = event.getProductType();
        this.to = DateTimeUtil.getDate(computeUDRTo);
        this.usedSeconds = usedSeconds;
        this.flagSetupCost = flagSetupCost;
    }

    /**
     * Create an InfluxDB Point that can be saved into InfluxDB database
     *
     * @return
     */
    public Point toDBPoint() {

        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        // now return constructed point
        return Point.measurement(name)
                .time(this.time, TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();
    }

    /**
     * This method returns default tags
     *
     * @return
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("clientId", clientId);
        map.put("productId", this.productId);

        return map;
    }

    /**
     * This method returns default fields
     *
     * @return
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("instanceId", this.instanceId);
//        map.put("productId", this.productId);
        map.put("productType", this.productType);
        map.put("to", this.to);
        map.put("usage", this.usedSeconds);
        map.put("flagSetupCost", this.flagSetupCost);

        return map;
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
