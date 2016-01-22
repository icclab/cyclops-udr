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

import ch.icclab.cyclops.services.iaas.cloudstack.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Martin Skoviera
 * Created on: 24-Nov-15
 * Description: POJO object that holds Usage list for selected user
 */
public class UsageList {
    final static Logger logger = LogManager.getLogger(UsageList.class.getName());
    private String userid;
    private HashMap<String, String> time;
    protected CustomList usage;

    // we will be adding to our custom list
    public class CustomList {
        // TODO do this universally once it is not hardcoded in Dashboard
        private ArrayList<UserUsage> External;

        public CustomList() {
            External = new ArrayList<UserUsage>();
        }

        // simply add new entry to the list
        public void addUsageToList(UserUsage item) {
            External.add(item);
        }

        // mandatory getters
        public ArrayList<UserUsage> getExternal() {
            return External;
        }
    }

    // Usual constructor
    public UsageList() {
        time = new HashMap<String, String>();
        usage = new CustomList();
    }

    // Getters and setters
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public HashMap<String, String> getTime() {
        return time;
    }

    public void setTime(String from, String to) {
        time.put("from", from);
        time.put("to", to);
    }

    public CustomList getUsage() {
        return usage;
    }

    public void addUsageFromMeter(QueryResult queryResult) {

        // create object based on received result
        UserUsage userUsage = new UserUsage(queryResult);

        // now normalise columns names and everything that is expected by Dashboard
        Boolean valid = userUsage.normaliseForDashboard();

        if (valid) {
            usage.addUsageToList(userUsage);
        }
    }

    /**
     * Create influxDB query for selecting time and rawusage for selected time window and users
     *
     * @param user
     * @param from
     * @param to
     * @param meter
     * @return string
     */
    public String createQuery(String user, String from, String to, String meter) {
        String normalizedFrom = Time.normaliseString(from);
        String normalizedTo = Time.normaliseString(to);
        return "select time,rawusage from \"" + meter + "\" where account='" + user + "' and time > '" + normalizedFrom + "' and time < '" + normalizedTo + "'";
    }
}
