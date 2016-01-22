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
package ch.icclab.cyclops.usecases.tnova.model;

import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.DTOObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Manu
 *         Created on 30.11.15.
 */
public class ServiceInstance extends DTOObject {
    private String time;
    private String usage;
    private String instanceId;

    public void setTime(String time) {
        this.time = time;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    protected Map<String, String> getObjectTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("instanceId", instanceId);

        return map;
    }

    @Override
    protected Map<String, Object> getObjectFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("usage", usage);
        map.put("time", time);

        return map;
    }

    @Override
    public String getMeasurementName() {
        return this.instanceId;
    }

    @Override
    protected Long getTime() {
        return null;
    }
}
