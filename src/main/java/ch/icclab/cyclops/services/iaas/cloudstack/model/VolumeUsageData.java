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
package ch.icclab.cyclops.services.iaas.cloudstack.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Martin Skoviera
 * Created on: 15-Oct-15
 * Description: POJO object for Volume Usage Data (type 6)
 */
public class VolumeUsageData extends UsageData {

    // The ID of the disk offering
    private String offeringid;

    // The amount of storage allocated
    private Long size;

    // The ID of template used
    private String templateid;

    /////////////////////////////
    // Getters and Setters

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getOfferingid() {
        return offeringid;
    }

    public void setOfferingid(String offeringid) {
        this.offeringid = offeringid;
    }

    public String getTemplateid() {
        return templateid;
    }

    public void setTemplateid(String templateid) {
        this.templateid = templateid;
    }

    @Override
    protected Map<String, String> getObjectTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("offeringid", offeringid);
        map.put("templateid", templateid);

        return map;
    }

    @Override
    protected Map<String, Object> getObjectFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("size", size);

        return map;
    }

    @Override
    protected String getMeterName() {
        return "cloudstack.volume.hours";
    }
}
