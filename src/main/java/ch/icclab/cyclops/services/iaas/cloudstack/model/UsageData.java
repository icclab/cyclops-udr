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

import ch.icclab.cyclops.services.iaas.cloudstack.util.Time;
import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 15-Oct-15
 * Description: POJO object for Generic Usage Data
 */
public abstract class UsageData {

    // Name of the account
    private String account;

    // ID of the account
    private String accountid;

    // ID of the domain in which this account resides
    private String domainid;

    // A string describing what the usage record is tracking
    private String description;

    // The range of time for which the usage is aggregated
    private String startdate;
    private String enddate;

    // String representation of the usage, including the units of usage (e.g. "Hrs" for VM running time)
    private String usage;

    // Virtual machine
    private String usageid;

    // A number representing the usage type (see Usage Types)
    private Integer usagetype;

    // A number representing the actual usage in hours
    private String rawusage;

    // Zone where the usage occurred
    private String zoneid;

    // In case we don't have user but a project
    private String project;
    private String projectid;

    // when selecting and querying DB this will come handy
    private static String rawusagename = "rawusage";
    private static String accountname = "account";
    private static String projectidname = "projectid";

    /**
     * Construct POINT object that will be saved with InfluxDB
     *
     * @return
     */
    public Point getObjectAsPoint() {

        // make sure we are not sending any null variables
        Map parentTags = getParentTags();
        removeNullValues(parentTags);

        Map parentFields = getParentFields();
        removeNullValues(parentFields);

        Map objectTags = getObjectTags();
        removeNullValues(objectTags);

        Map objectFields = getObjectFields();
        removeNullValues(objectFields);

        // now return constructed point
        return Point.measurement(getMeterName())
                .time(Time.getMilisForTime(startdate), TimeUnit.MILLISECONDS)
                .tag(parentTags)
                .tag(objectTags)
                .fields(parentFields)
                .fields(objectFields)
                .build();
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
     * This method returns default tags
     *
     * @return
     */
    private Map<String, String> getParentTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("account", account);
        map.put("accountid", accountid);
        map.put("project", project);
        map.put("projectid", projectid);
        map.put("domainid", domainid);
        map.put("usageid", usageid);
        map.put("zoneid", zoneid);

        return map;
    }

    /**
     * This method returns default fields
     *
     * @return
     */
    private Map<String, Object> getParentFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("description", description);
        map.put("usage", usage);
        map.put(rawusagename, rawusage);

        return map;
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
     * Abstract method that will return name of the meter
     *
     * @return
     */
    protected abstract String getMeterName();

    /////////////////////////////
    // Getters and Setters

    public static String getUsageColumnName() {
        return rawusagename;
    }

    public static String getAccountColumnName() {
        return accountname;
    }

    public static String getProjectIdColumnName() {
        return projectidname;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(String domainid) {
        this.domainid = domainid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsageid() {
        return usageid;
    }

    public void setUsageid(String usageid) {
        this.usageid = usageid;
    }

    public Integer getUsagetype() {
        return usagetype;
    }

    public void setUsagetype(Integer usagetype) {
        this.usagetype = usagetype;
    }

    public String getRawusage() {
        return rawusage;
    }

    public void setRawusage(String rawusage) {
        this.rawusage = rawusage;
    }

    public String getZoneid() {
        return zoneid;
    }

    public void setZoneid(String zoneid) {
        this.zoneid = zoneid;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectid() {
        return projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }
}
