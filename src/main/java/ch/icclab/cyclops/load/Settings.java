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
package ch.icclab.cyclops.load;

import ch.icclab.cyclops.load.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Author: Martin Skoviera
 * Created on: 16-Nov-15
 * Description: Parent for specific environmental settings
 */
public class Settings {
    final static Logger logger = LogManager.getLogger(Settings.class.getName());

    // different settings options
    protected InfluxDBSettings influxDBSettings;
    protected SchedulerSettings schedulerSettings;
    protected CloudStackSettings cloudStackSettings;
    protected RabbitMQSettings rabbitMQSettings;
    protected KeyStoneSettings keyStoneSettings;
    protected TNovaSettings tNovaSettings;
    protected MCNSettings mcnSettings;

    // our running environment
    private String environment;

    Properties properties;

    /**
     * Load settings based on provided settings
     */
    public Settings(Properties prop) {
        // save properties first
        properties = prop;

        // parse environment settings
        environment = properties.getProperty("Environment");
    }

    // ===== Section for creating/loading settings objects
    private KeyStoneSettings loadKeyStoneSettings() {
        return new KeyStoneSettings(properties.getProperty("KeystoneURL"),
                properties.getProperty("KeystoneUsername"), properties.getProperty("KeystonePassword"),
                properties.getProperty("KeystoneTenantName"), properties.getProperty("CeilometerURL"));
    }

    private RabbitMQSettings loadRabbitMQSettings() {
        return new RabbitMQSettings(properties.getProperty("RabbitMQUsername"), properties.getProperty("RabbitMQPassword"),
                properties.getProperty("RabbitMQHost"), properties.getProperty("RabbitMQPort"),
                properties.getProperty("RabbitMQVirtualHost"));
    }

    private InfluxDBSettings loadInfluxDBSettings() {
        return new InfluxDBSettings(properties.getProperty("InfluxDBURL"), properties.getProperty("InfluxDBUsername"),
                properties.getProperty("InfluxDBPassword"), properties.getProperty("InfluxDBDatabaseName"));
    }

    private SchedulerSettings loadSchedulerSettings() {
        return new SchedulerSettings(properties.getProperty("ScheduleFrequency"));
    }

    private CloudStackSettings loadCloudStackSettings() {
        CloudStackSettings cloudstack = new CloudStackSettings(properties.getProperty("CloudStackURL"), properties.getProperty("CloudStackAPIKey"),
                properties.getProperty("CloudStackSecretKey"), properties.getProperty("CloudStackPageSize"), properties.getProperty("CloudStackDBLogsName"),
                properties.getProperty("CloudStackEventMeasurement"), properties.getProperty("CloudStackMeterListSelection"));

        // not mandatory, but helpful date of the first import
        String date = properties.getProperty("CloudStackFirstImport");
        if (date != null && !date.isEmpty()) {
            cloudstack.setCloudStackImportFrom(date);
        }

        return cloudstack;
    }

    private TNovaSettings loadTNovaSettings() {
        return new TNovaSettings(properties.getProperty("TNovaDBEventsName"), properties.getProperty("TNovaEventStart"), properties.getProperty("TNovaEventQueue"));
    }

    private MCNSettings loadMCNSettings() {
        return new MCNSettings(properties.getProperty("MCNDBEventsName"), properties.getProperty("MCNEventStart"), properties.getProperty("MCNEventQueue"));
    }

    // ===== Section for accessing loaded settings
    public String getEnvironment() {
        return environment;
    }

    public InfluxDBSettings getInfluxDBSettings() {
        if (influxDBSettings == null) {
            try {
                influxDBSettings = loadInfluxDBSettings();
            } catch (Exception e) {
                logger.error("Could not load InfluxDB settings from configuration file: " + e.getMessage());
            }
        }

        return influxDBSettings;
    }

    public SchedulerSettings getSchedulerSettings() {
        if (schedulerSettings == null) {
            try {
                schedulerSettings = loadSchedulerSettings();
            } catch (Exception e) {
                logger.error("Could not load Scheduler Settings settings from configuration file: " + e.getMessage());
            }
        }

        return schedulerSettings;
    }

    public CloudStackSettings getCloudStackSettings() {
        if (cloudStackSettings == null) {
            try {
                cloudStackSettings = loadCloudStackSettings();
            } catch (Exception e) {
                logger.error("Could not load CloudStack Settings from configuration file: " + e.getMessage());
            }
        }

        return cloudStackSettings;
    }

    public RabbitMQSettings getRabbitMQSettings() {
        if (rabbitMQSettings == null) {
            try {
                rabbitMQSettings = loadRabbitMQSettings();
            } catch (Exception e) {
                logger.error("Could not load RabbitMQ Settings from configuration file: " + e.getMessage());
            }
        }
        return rabbitMQSettings;
    }

    public KeyStoneSettings getKeyStoneSettings() {
        if (keyStoneSettings == null) {
            try {
                keyStoneSettings = loadKeyStoneSettings();
            } catch (Exception e) {
                logger.error("Could not load KeyStone Settings from configuration file: " + e.getMessage());
            }
        }
        return keyStoneSettings;
    }

    public TNovaSettings gettNovaSettings() {
        if (tNovaSettings == null) {
            try {
                tNovaSettings = loadTNovaSettings();
            } catch (Exception e) {
                logger.error("Could not load TNova Settings from configuration file: " + e.getMessage());
            }
        }
        return tNovaSettings;
    }

    public MCNSettings getMcnSettings() {
        if (mcnSettings == null) {
            try {
                mcnSettings = loadMCNSettings();
            } catch (Exception e) {
                logger.error("Could not load MCN Settings from configuration file: " + e.getMessage());
            }
        }
        return mcnSettings;
    }
}
