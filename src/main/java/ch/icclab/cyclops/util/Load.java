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

import ch.icclab.cyclops.model.udr.TSDBData;
import ch.icclab.cyclops.persistence.impl.TSDBResource;
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
 *
 */
public class Load extends ClientResource {

    //Instantiating the Instance variable for saving the config details
    public static HashMap<String,String> configuration;
    public static ArrayList<String> openStackCumulativeMeterList = new ArrayList<String>();
    public static ArrayList<String> openStackGaugeMeterList = new ArrayList<String>();
    public static ArrayList<String> externalMeterList = new ArrayList<String>();

    /**
     * Loads the configuration file
     *
     * Pseudo Code
     * 1. Create an instance of the ServletContext
     * 2. Get the relative path of the configuration.txt file
     * 3. Load the file and save the values into a static HashMap
     *
     * @param context
     * @throws IOException
     */
    public void configuration(Context context) throws IOException {
        configuration = new HashMap();
        String nextLine;
        ServletContext servlet = (ServletContext) context.getAttributes().get("org.restlet.ext.servlet.ServletContext");
        System.out.println("Reading the config file from " + servlet.getRealPath("/WEB-INF/configuration.txt"));
        // Get the path of the config file relative to the WAR
        String rootPath = servlet.getRealPath("/WEB-INF/configuration.txt");
        Path path = Paths.get(rootPath);
        File configFile = new File(path.toString());
        FileRepresentation file = new FileRepresentation(configFile, MediaType.TEXT_PLAIN);
        // Read the values from the config file
        try {
            BufferedReader reader = new BufferedReader(file.getReader());
            while((nextLine = reader.readLine()) != null ) {
                String[] str = nextLine.split("==");
                configuration.put(str[0],str[1]);
            }
        } catch (IOException e) {
            System.out.println("Failed to load the Config file");
            e.printStackTrace();
        }
    }

    public void meterList(){
        TSDBResource tsdbResource = new TSDBResource();
        TSDBData tsdbData;
        ArrayList<ArrayList<String>> masterMeterList;
        ArrayList meterList;
        int indexStatus = -1;
        int indexMeterName = -1;
        int indexMeterType = -1;
        int indexMeterSource = -1;

        if(Flag.isMeterListReset()){
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
            for(int i=0; i < masterMeterList.size(); i++){
                meterList = masterMeterList.get(i);
                if((meterList.get(indexStatus).equals(Constant.METER_SELECTED))
                        && (meterList.get(indexMeterSource).equals(Constant.OPENSTACK))
                        && meterList.get(indexMeterType).equals(Constant.OPENSTACK_CUMULATIVE_METER)){
                    Load.openStackCumulativeMeterList.add(meterList.get(indexMeterName).toString());
                }else if(meterList.get(indexStatus).equals(Constant.METER_SELECTED)
                        && (meterList.get(indexMeterSource).equals(Constant.OPENSTACK))
                        && meterList.get(indexMeterType).equals(Constant.OPENSTACK_GAUGE_METER)){
                    Load.openStackGaugeMeterList.add(meterList.get(indexMeterName).toString());
                } else if((meterList.get(indexStatus).equals(Constant.METER_SELECTED))
                        && !(meterList.get(indexMeterSource).equals(Constant.OPENSTACK))){
                    Load.externalMeterList.add(meterList.get(indexMeterName).toString());
                }
            }
        }
    }
}
