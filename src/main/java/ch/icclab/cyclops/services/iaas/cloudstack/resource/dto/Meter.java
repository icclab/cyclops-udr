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

import ch.icclab.cyclops.load.Loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 17-Nov-15
 * Description: POJO object that holds one meter
 */
public class Meter extends DTOObject {
    private String time;
    private String metertype;
    private String source;
    private String metername;

    // couple default values
    private String metersource = "cloudstack";
    private String status = "0";

    //=== Setters
    public void setTime(String time) {
        this.time = time;
    }

    public void setMetersource(String metersource) {
        this.metersource = metersource;
    }

    public void setMetertype(String metertype) {
        this.metertype = metertype;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMetername(String metername) {
        this.metername = metername;
    }

    @Override
    protected Map<String, String> getObjectTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("metername", metername);

        return map;
    }

    @Override
    protected Map<String, Object> getObjectFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("metersource", metersource);
        map.put("metertype", metertype);
        map.put("source", source);
        map.put("status", status);

        return map;
    }

    @Override
    public String getMeasurementName() {
        String name = Loader.getSettings().getCloudStackSettings().getCloudStackMeterListSelection();
        if (name == null || name.isEmpty()) {
            name = "cloudstack.meterselection";
        }

        return name;
    }

    @Override
    protected Long getTime() {
        // we are not using time here, as we want to object be saved with "now" mark
        return null;
    }

    public static String getGroupByName() {
        return "metername";
    }
}
