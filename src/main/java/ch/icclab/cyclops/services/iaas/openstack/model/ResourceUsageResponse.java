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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <b>POJO Object</b><p/>
 * Author: Srikanta
 * Created on: 01-Apr-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class ResourceUsageResponse {
    private String resourceid;
    private HashMap time;
    private ArrayList column;
    private ArrayList<ArrayList<Object>> usage;
    private HashMap tags;

    //TODO: Create constructors to POJO Classes so we don't have to set all the attributes

    public String getResourceid() {
        return resourceid;
    }

    public void setResourceid(String resourceid) {
        this.resourceid = resourceid;
    }

    public HashMap getTime() {
        return time;
    }

    public void setTime(HashMap time) {
        this.time = time;
    }

    public ArrayList getColumn() {
        return column;
    }

    public void setColumn(ArrayList column) {
        this.column = column;
    }

    public ArrayList<ArrayList<Object>> getUsage() {
        return usage;
    }

    public void setUsage(ArrayList<ArrayList<Object>> usage) {
        this.usage = usage;
    }

    public HashMap getTags() {
        return tags;
    }

    public void setTags(HashMap tags) {
        this.tags = tags;
    }
}
