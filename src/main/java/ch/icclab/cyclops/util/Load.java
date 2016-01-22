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
import org.restlet.resource.ClientResource;

import java.util.ArrayList;

/**
 * Author: Srikanta
 * Created on: 17-Nov-14
 * Description: Loads the configuration file into a static object
 */
public class Load extends ClientResource {
    //TODO Manu meterlist has to go to the OpenStack layer, then delete this Load class (as we are using different package now)

    //Instantiating the Instance variable for saving the config details
    private static ArrayList<String> openStackCumulativeMeterList;
    private static ArrayList<String> openStackGaugeMeterList;
    private static ArrayList<String> externalMeterList;

    /**
     * Private constructor, because we are working with singleton pattern
     */
    public Load() {
        try {
            // prepare openstack
            openStackCumulativeMeterList = new ArrayList<String>();
            openStackGaugeMeterList = new ArrayList<String>();
            externalMeterList  = new ArrayList<String>();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static ArrayList<String> getOpenStackCumulativeMeterList() {
        return openStackCumulativeMeterList;
    }

    public static ArrayList<String> getOpenStackGaugeMeterList() {
        return openStackGaugeMeterList;
    }

    public static ArrayList<String> getExternalMeterList() {
        return externalMeterList;
    }
}
