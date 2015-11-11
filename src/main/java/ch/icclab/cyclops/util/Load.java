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

package ch.icclab.cyclops.util;

import ch.icclab.cyclops.services.iaas.openstack.model.TSDBData;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 17-Nov-14
 * Description: Loads the configuration file into a static object
 */
public class Load extends ClientResource {

    private static Load singleton;
    private static Context context;

    private Load() {
        this.singleton = null;
        try {
            this.configuration(context);
            this.createDatabase();
            this.meterList();
            this.setEnvironment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Load getInstance() {
        if (singleton == null)
            singleton = new Load();
        return singleton;
    }

    public static Load getInstance(Context c) {
        if (singleton == null) {
            context = c;
            singleton = new Load();
        }
        return singleton;
    }

    final static Logger logger = LogManager.getLogger(Load.class.getName());
    private EnvironmentLoader environment;
    //Instantiating the Instance variable for saving the config details
    public static HashMap<String, String> configuration;
    public static ArrayList<String> openStackCumulativeMeterList = new ArrayList<String>();
    public static ArrayList<String> openStackGaugeMeterList = new ArrayList<String>();
    public static ArrayList<String> externalMeterList = new ArrayList<String>();
    private InfluxDB influxDB;

    // frequency for internal scheduler
    private long schedulerFrequency = 0;

    // Rabbit MQ Settings
    private RabbitMQSettings rabbitMQSettings;

    /**
     * This class holds Rabbit MQ Settings
     */
    public class RabbitMQSettings {
        private String RabbitMQQueueName;
        private String RabbitMQUsername;
        private String RabbitMQPassword;
        private String RabbitMQHost;
        private Integer RabbitMQPort;
        private String RabbitMQVirtualHost;

        public RabbitMQSettings(String rabbitMQQueueName, String rabbitMQUsername, String rabbitMQPassword, String rabbitMQHost, Integer rabbitMQPort, String rabbitMQVirtualHost) {
            RabbitMQQueueName = rabbitMQQueueName;
            RabbitMQUsername = rabbitMQUsername;
            RabbitMQPassword = rabbitMQPassword;
            RabbitMQHost = rabbitMQHost;
            RabbitMQPort = rabbitMQPort;
            RabbitMQVirtualHost = rabbitMQVirtualHost;
        }

        public String getRabbitMQQueueName() {
            return RabbitMQQueueName;
        }

        public String getRabbitMQUsername() {
            return RabbitMQUsername;
        }

        public String getRabbitMQPassword() {
            return RabbitMQPassword;
        }

        public String getRabbitMQHost() {
            return RabbitMQHost;
        }

        public Integer getRabbitMQPort() {
            return RabbitMQPort;
        }

        public String getRabbitMQVirtualHost() {
            return RabbitMQVirtualHost;
        }
    }

    /**
     * Load the RabbitMQ Settings for the first time
     */
    private RabbitMQSettings loadRabbitMQSettings() {
        return new RabbitMQSettings(configuration.get("RabbitMQQueueName"),
                configuration.get("RabbitMQUsername"), configuration.get("RabbitMQPassword"),
                configuration.get("RabbitMQHost"), Integer.parseInt(configuration.get("RabbitMQPort")),
                configuration.get("RabbitMQVirtualHost"));
    }

    /**
     * Simple getter for Rabbit MQ settings
     *
     * @return null or object
     */
    public RabbitMQSettings getRabbitMQSettings() {
        if (rabbitMQSettings == null) {
            try {
                rabbitMQSettings = loadRabbitMQSettings();
            } catch (Exception e) {
                logger.error("Could not load Rabbit MQ Settings: " + e.getMessage());
            }
        }
        return rabbitMQSettings;
    }

    /**
     * Loads the configuration file
     * <p>
     * Pseudo Code
     * 1. Create an instance of the ServletContext
     * 2. Get the relative path of the configuration.txt file
     * 3. Load the file and save the values into a static HashMap
     *
     * @param context
     * @throws IOException
     */
    public void configuration(Context context) throws IOException {
        logger.trace("BEGIN void configuration(Context context) throws IOException");
        configuration = new HashMap();
        String nextLine;
        ServletContext servlet = (ServletContext) context.getAttributes().get("org.restlet.ext.servlet.ServletContext");
        // Get the path of the config file relative to the WAR
        String rootPath = servlet.getRealPath("/WEB-INF/configuration.txt");
        Path path = Paths.get(rootPath);
        String webInfPath = servlet.getRealPath("/WEB-INF");
        configuration.put("WEB-INF", webInfPath);
        File configFile = new File(path.toString());
        FileRepresentation file = new FileRepresentation(configFile, MediaType.TEXT_PLAIN);
        // Read the values from the config file
        try {
            BufferedReader reader = new BufferedReader(file.getReader());
            while ((nextLine = reader.readLine()) != null) {
                String[] str = nextLine.split("==");
                configuration.put(str[0], str[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //RabbitmqClient queueThread = new RabbitmqClient();
        //queueThread.start();
    }

    public void createDatabase() {
        influxDB = InfluxDBFactory.connect(configuration.get("InfluxDBURL"), configuration.get("InfluxDBUsername"), configuration.get("InfluxDBPassword"));
        influxDB.createDatabase(configuration.get("dbName"));
        influxDB.createDatabase(configuration.get("events_dbname"));
    }

    /**
     * This method formats the meters and inserts them in the Cumulative and Gauge meter lists.
     */
    public void meterList() {
        TSDBResource tsdbResource = new TSDBResource();
        TSDBData tsdbData;
        ArrayList<ArrayList<String>> masterMeterList;
        ArrayList meterList;
        int indexStatus = -1;
        int indexMeterName = -1;
        int indexMeterType = -1;
        int indexMeterSource = -1;

        if (Flag.isMeterListReset()) {
            Flag.setMeterListReset(false);
            Load.openStackGaugeMeterList.clear();
            Load.openStackCumulativeMeterList.clear();

            // Get the meterlist and corresponding indexes of the columns
            tsdbData = tsdbResource.getMeterList();
            // Extract the data points
            masterMeterList = (ArrayList) tsdbData.getPoints();
            indexStatus = tsdbData.getColumns().indexOf("status");
            indexMeterType = tsdbData.getColumns().indexOf("metertype");
            indexMeterName = tsdbData.getColumns().indexOf("metername");
            indexMeterSource = tsdbData.getColumns().indexOf("metersource");
            // Iterate through the list of arraylist & segregate the meters
            for (int i = 0; i < masterMeterList.size(); i++) {
                meterList = masterMeterList.get(i);
                if ((meterList.get(indexStatus).equals(String.valueOf(Constant.METER_SELECTED)))
                        && (meterList.get(indexMeterSource).equals(Constant.OPENSTACK))
                        && meterList.get(indexMeterType).equals(Constant.OPENSTACK_CUMULATIVE_METER)
                        && !Load.openStackCumulativeMeterList.contains(meterList.get(indexMeterName).toString())) {
                    Load.openStackCumulativeMeterList.add(meterList.get(indexMeterName).toString());
                } else if (meterList.get(indexStatus).equals(String.valueOf(Constant.METER_SELECTED))
                        && (meterList.get(indexMeterSource).equals(Constant.OPENSTACK))
                        && meterList.get(indexMeterType).equals(Constant.OPENSTACK_GAUGE_METER)
                        && !Load.openStackGaugeMeterList.contains(meterList.get(indexMeterName).toString())) {
                    Load.openStackGaugeMeterList.add(meterList.get(indexMeterName).toString());
                } else if ((meterList.get(indexStatus).equals(String.valueOf(Constant.METER_SELECTED)))
                        && !(meterList.get(indexMeterSource).equals(Constant.OPENSTACK))
                        && !Load.externalMeterList.contains(meterList.get(indexMeterName).toString())) {
                    Load.externalMeterList.add(meterList.get(indexMeterName).toString());
                }
            }
        }
    }

    public void setEnvironment() {
        String environment = configuration.get("Environment");
        if (environment != null) {
            if (environment.equalsIgnoreCase("Tnova")) {
                this.environment = new TnovaEnvironment();
            } else if (environment.equalsIgnoreCase("mcn")) {
                this.environment = new McnEnvironment();
            } else {
                logger.error("Error while reading the configuration file. The Environment variable is not supported: " + environment);
            }
        } else {
            logger.error("Error while reading the configuration file. Empty Environment variable.");
        }
    }

    public EnvironmentLoader getEnvironment() {
        return this.environment;
    }

    /**
     * Get desired frequency for internal scheduler
     * @return long
     */
    public long getScheduleFrequency() {
        // parse only once
        if (schedulerFrequency <= 0){
            try {
                // parse it from config file
                long freq = Long.parseLong(configuration.get("scheduleFrequency"));

                if (freq > 0) {
                    // use parsed value
                    schedulerFrequency = freq;
                } else {
                    // or add default one
                    schedulerFrequency = 300;
                }
            } catch (Exception e) {
                // add default value
                schedulerFrequency = 300;
            }
        }

        // now return cached frequency
        return schedulerFrequency;
    }
}
