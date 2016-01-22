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

package ch.icclab.cyclops.load.model;

/* Author: Martin Skoviera
 * Created on: 16-Nov-15
 * Description: Settings for InfluxDB
 */
public class InfluxDBSettings {
    private String InfluxDBURL;
    private String InfluxDBUsername;
    private String InfluxDBPassword;
    private String InfluxDBDatabaseName;

    /**
     * Simple constructor saving all provided information
     */
    public InfluxDBSettings(String influxDBURL, String influxDBUsername, String influxDBPassword, String influxDBDatabaseName) {
        InfluxDBURL = influxDBURL;
        InfluxDBUsername = influxDBUsername;
        InfluxDBPassword = influxDBPassword;
        InfluxDBDatabaseName = influxDBDatabaseName;
    }

    //==== We need only getters
    public String getInfluxDBURL() {
        return InfluxDBURL;
    }

    public String getInfluxDBUsername() {
        return InfluxDBUsername;
    }

    public String getInfluxDBPassword() {
        return InfluxDBPassword;
    }

    public String getInfluxDBDatabaseName() {
        return InfluxDBDatabaseName;
    }
}
