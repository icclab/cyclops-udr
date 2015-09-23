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

package ch.icclab.cyclops.services.iaas.openstack.resource.impl;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Description:
 */

import ch.icclab.cyclops.services.iaas.openstack.model.CumulativeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.GaugeMeterData;
import ch.icclab.cyclops.services.iaas.openstack.model.Response;
import ch.icclab.cyclops.services.iaas.openstack.persistence.TSDBResource;
import ch.icclab.cyclops.services.iaas.openstack.client.KeystoneClient;
import ch.icclab.cyclops.services.iaas.openstack.client.TelemetryClient;
import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.MeteringResource;
import ch.icclab.cyclops.util.Load;
import ch.icclab.cyclops.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.*;

public class TelemetryResource extends ServerResource implements MeteringResource {
    final static Logger logger = LogManager.getLogger(TelemetryResource.class.getName());

    /**
     * This method calls the private classes to get the data from the Cumulative and Gauge meters
     * of the Ceilometer. This method is called periodically by a scheduler
     *
     * Pseudo Code
     *  1. Create a Keystone client
     *  2. Generate a token
     *  3. Get the data from Cumulative Meters by passing the token
     *  4. Get the data from Gauge Meters by passing the token
     *  5. Display the output from step 3 and 4.
     *
     * @return output A String output of the success or failure of the data extraction process
     */
    @Get
    public Representation setMeterData() {
        logger.trace("BEGIN Representation setMeterData()");
        boolean gaugeMeterOutput = false;
        boolean cumulativeMeterOutput = false;
        String token;
        Representation output = null;
        Response response = null;
        ResponseUtil util = new ResponseUtil();
        Load load = new Load();

        KeystoneClient keystoneClient = new KeystoneClient();
        token = keystoneClient.generateToken();

        //Load the meter list
        load.meterList();
        for(String meter : Load.openStackCumulativeMeterList) {
            logger.debug("Cumulative Meter: " + meter);
        }
        for(String meter : Load.openStackGaugeMeterList) {
            logger.debug("Gauge Meter: " + meter);
        }
        //Get the usage data for the selected OpenStack Cumulative Meters
        cumulativeMeterOutput = getCumulativeMeterData(Load.openStackCumulativeMeterList, token);
        //Get the usage data for the selected OpenStack Gauge Meters
        gaugeMeterOutput = getGaugeMeterData(Load.openStackGaugeMeterList,token);
        //Construct the response of the data collection process
        response = constructResponse(cumulativeMeterOutput,gaugeMeterOutput);
        //Return the response in the JSON format
        output = util.toJson(response);
        logger.trace("END Representation setMeterData()");
        return output;
    }

    /**
     * Evaluates the outcome of the operation to save the Cumulative & Gauge and constructs the
     * response object accordingly
     *
     * @param cumulativeMeterOutput A boolean which indicates the outcome of the operation to save the Cumulative Meter data
     * @param gaugeMeterOutput A boolean which indicates the outcome of the operation to save the Gauge Meter data
     * @return A response object containing the details of the operation
     */
    private Response constructResponse(boolean cumulativeMeterOutput, boolean gaugeMeterOutput) {
        logger.trace("BEGIN Response constructResponse(boolean cumulativeMeterOutput, boolean gaugeMeterOutput)");
        Response responseObj = new Response();
        LocalDateTime currentDateTime = new LocalDateTime();

        if(cumulativeMeterOutput && gaugeMeterOutput){
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Cumulative & Gauge Meters were Successfully saved into the DB");
        }else if(gaugeMeterOutput){
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Only Gauge Meter was Successfully saved into the DB");
            logger.debug("DEBUG Response constructResponse(boolean cumulativeMeterOutput, boolean gaugeMeterOutput) - Only Gauge Meter was Successfully saved into the DB");
        }else{
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Only Cumulative Meter was Successfully saved into the DB");
            logger.debug("DEBUG Response constructResponse(boolean cumulativeMeterOutput, boolean gaugeMeterOutput) - Only Cumulative Meter was Successfully saved into the DB");
        }
        logger.trace("END Response constructResponse(boolean cumulativeMeterOutput, boolean gaugeMeterOutput)");
        return  responseObj;
    }

    /**
     * In this method, the usage metrics from the cumulative meters are extracted
     *
     * Pseudo Code
     * 1. Query the sample api of Telemetry
     * 2. Receive the ungrouped samples but already sorted for timestamp
     * 3. Group the sample on per resource basis
     * 4. Iterate through the array, add the subtracted value of two simultaneous samples at a time
     * 5. If the subtracted value is negative, adjust the value as per the datatype max limit
     * 6. Save all these details along with the usage in the db
     *
     * @param token The token generated by the keystone service is used for authorization by Telemetry Service
     * @return output A String output of the success or failure of the data extraction process
     * @throws JSONException
     * @throws IOException
     */
    private boolean getCumulativeMeterData(ArrayList<String> meter, String token) {
        logger.trace("BEGIN getCumulativeMeterData(ArrayList<String> meter, String token)");
        boolean output = false;
        String response = null;
        Set keySet;
        String meterType = "cumulative";
        CumulativeMeterData data = null;
        LinkedList<CumulativeMeterData> linkedList;
        JSONArray array = null;

        ObjectMapper mapper = new ObjectMapper();
        TelemetryClient tClient = new TelemetryClient();
        ArrayList<CumulativeMeterData> cMeterArr;
        TSDBResource dbResource = new TSDBResource();
        HashMap<String, LinkedList<CumulativeMeterData>> map;

        for(int j=0; j < meter.size();j++){
            cMeterArr = new ArrayList<CumulativeMeterData>();
            map = new HashMap<String, LinkedList<CumulativeMeterData>>();
            try {
                response = tClient.getData(token, meter.get(j), meterType);
                array = new JSONArray(response);

            //Builds an array of samples and a hashmap of resourceID as key and a linkedlist of samples as values.
            for (int i=0; i< array.length(); i++){
                JSONObject obj = null;
                obj = array.getJSONObject(i);
                data = mapper.readValue(obj.toString(),CumulativeMeterData.class);

                if(map.containsKey(data.getResource_id())){
                    linkedList = map.get(data.getResource_id());
                    linkedList.add(data);
                    map.remove(data.getResource_id());
                    map.put(data.getResource_id(), linkedList);
                }else{
                    linkedList = new LinkedList<CumulativeMeterData>();
                    linkedList.add(data);
                    map.put(data.getResource_id(), linkedList);
                }
            }

            //Get the Set of keys
            keySet = map.keySet();
            Iterator setIterator = keySet.iterator();

            //Iterate through the Set to extract the LinkedList
            while (setIterator.hasNext()){
                linkedList = map.get(setIterator.next());
                cMeterArr = calculateCumulativeMeterUsage(cMeterArr, linkedList);
            }
            dbResource.saveCumulativeMeterData(cMeterArr, meter.get(j));
            output = true;
            } catch (IOException e) {
                logger.error("EXCEPTION IOEXCEPTION getCumulativeMeterData(ArrayList<String> meter, String token)");
                output = false;
                e.printStackTrace();
                return output;
            } catch (JSONException e) {
                logger.error("EXCEPTION JSONEXCEPTION getCumulativeMeterData(ArrayList<String> meter, String token)");
                output = false;
                e.printStackTrace();
                return output;
            }
        }
        logger.trace("END getCumulativeMeterData(ArrayList<String> meter, String token)");
        return output;
    }

    /**
     * In this method, usage made is calculated on per resource basis in the cumulative meters
     *
     * Pseudo Code
     * 1. Traverse through the linkedlist
     * 2. Subtract the volumes of i and (i+1) samples
     * 3. Set the difference into the i sample object
     * 4. Add the updates sample object into an arraylist
     *
     * @param cMeterArr This is an arrayList of type CumulativeMeterData containing sample object with the usage information
     * @param linkedList This is a Linked List of type CumulativeMeterData containing elements from a particular resource
     * @return An arrayList of type CumulativeMeterData containing sample objects with the usage information
     */
    private ArrayList<CumulativeMeterData> calculateCumulativeMeterUsage(ArrayList<CumulativeMeterData> cMeterArr, LinkedList<CumulativeMeterData> linkedList) {
        logger.trace("BEGIN ArrayList<CumulativeMeterData> calculateCumulativeMeterUsage(ArrayList<CumulativeMeterData> cMeterArr, LinkedList<CumulativeMeterData> linkedList)");
        long diff;
        //BigInteger maxMeterValue ;

        for(int i=0;i<(linkedList.size()-1);i++){
            if((i+1) <= linkedList.size()) {
                diff = linkedList.get(i).getVolume() - linkedList.get(i + 1).getVolume();
                if(diff < 0){
                    linkedList.get(i).setUsage(0); //TODO: Update the negative difference usecase
                }else{
                    linkedList.get(i).setUsage(diff);
                }
                cMeterArr.add(linkedList.get(i));
            }
        }
        cMeterArr.add(linkedList.getLast());
        logger.trace("END ArrayList<CumulativeMeterData> calculateCumulativeMeterUsage(ArrayList<CumulativeMeterData> cMeterArr, LinkedList<CumulativeMeterData> linkedList)");
        return cMeterArr;
    }

    /**
     * In this method, the usage metrics from the gauge meters are extracted
     *
     * @param token The token generated by the keystone service is used for authorization by Telemetry Service
     * @return output A String output of the success or failure of the data extraction process
     * @throws JSONException
     */
    private boolean getGaugeMeterData(ArrayList<String> meter, String token){
        logger.trace("BEGIN boolean getGaugeMeterData(ArrayList<String> meter, String token)");
        String response;
        JSONArray array ;
        ObjectMapper mapper = new ObjectMapper();
        TSDBResource dbResource = new TSDBResource();
        ArrayList<GaugeMeterData> dataArr;
        String meterType  = "gauge";
        boolean output = true;

        try{
            TelemetryClient tClient = new TelemetryClient();
            for (int i = 0; i < meter.size(); i++) {
                dataArr = new ArrayList<GaugeMeterData>();
                response = tClient.getData(token, meter.get(i), meterType);
                array = new JSONArray(response);
                logger.debug("Array Size: "+array.length());
                if (response.toString() != "[]") {
                    for (int j=0; j< array.length(); j++){
                        JSONObject obj = array.getJSONObject(j);
                        GaugeMeterData data = mapper.readValue(obj.toString(),GaugeMeterData.class);
                        dataArr.add(data);
                    }
                    dbResource.saveGaugeMeterData(dataArr, meter.get(i));
                } else {
                    //System.out.println("Ceilometer returned empty response for " + meter.get(i));
                }
            }

        } catch (Exception e) {
            logger.error("EXCEPTION boolean getGaugeMeterData(ArrayList<String> meter, String token)");
            e.printStackTrace();
            output = false;
            return output;
        }
        logger.trace("END boolean getGaugeMeterData(ArrayList<String> meter, String token)");
        return output;
    }
}