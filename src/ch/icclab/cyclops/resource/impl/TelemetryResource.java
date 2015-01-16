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

package ch.icclab.cyclops.resource.impl;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */

import ch.icclab.cyclops.model.udr.CumulativeMeterData;
import ch.icclab.cyclops.model.udr.GaugeMeterData;
import ch.icclab.cyclops.model.udr.Response;
import ch.icclab.cyclops.persistence.impl.TSDBResource;
import ch.icclab.cyclops.resource.client.KeystoneClient;
import ch.icclab.cyclops.resource.client.TelemetryClient;
import ch.icclab.cyclops.resource.interfc.MeteringResource;
import ch.icclab.cyclops.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Representation getMeterData() {
        System.out.println("Getting the response");

        boolean gaugeMeterOutput = false;
        boolean cumulativeMeterOutput = false;
        String token;
        Representation output = null;

        Response response = null;
        ResponseUtil util = new ResponseUtil();
        KeystoneClient kClient = new KeystoneClient();
        token = kClient.generateToken();

        cumulativeMeterOutput = setCumulativeMeterData(token);
        gaugeMeterOutput = setGaugeMeterData(token);

        response = constructResponse(cumulativeMeterOutput,gaugeMeterOutput);
        output = util.toJson(response);

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

        Response responseObj = new Response();
        LocalDateTime currentDateTime = new LocalDateTime();

        if(cumulativeMeterOutput && gaugeMeterOutput){
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Cumulative & Gauge Meters were Successfully saved into the DB");
        }else if(gaugeMeterOutput){
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Gauge Meter was Successfully saved into the DB");
        }else{
            responseObj.setTimestamp(currentDateTime.toDateTime().toString());
            responseObj.setStatus("Success");
            responseObj.setMessage("Cumulative Meter was Successfully saved into the DB");
        }

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
     * 5. If the subtracted value is negative, adjust the value as per the datatype max limit //TODO : To be implemented
     * 6. Save all these details along with the usage in the db
     *
     * @param token The token generated by the keystone service is used for authorization by Telemetry Service
     * @return output A String output of the success or failure of the data extraction process
     * @throws JSONException
     * @throws IOException
     */
    private boolean setCumulativeMeterData(String token) {

        boolean output = true;
        String response = null;
        Set keySet;
        String meterType = "cumulative";
        CumulativeMeterData data = null;
        LinkedList<CumulativeMeterData> linkedList;
        JSONArray array = null;

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<String> meter = new ArrayList<String>();
        TelemetryClient tClient = new TelemetryClient();
        ArrayList<CumulativeMeterData> cMeterArr = new ArrayList<CumulativeMeterData>();
        TSDBResource dbResource = new TSDBResource();
        HashMap<String, LinkedList<CumulativeMeterData>> map = new HashMap<String, LinkedList<CumulativeMeterData>>();

        meter.add("network.incoming.bytes"); //TODO : Remove hard code
        meter.add("network.outgoing.bytes");

        for(int j=0; j < meter.size();j++){
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
            } catch (IOException e) {
                output = false;
                e.printStackTrace();
                return output;
            } catch (JSONException e) {
                output = false;
                e.printStackTrace();
                return output;
            }
        }
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

        long diff;

        for(int i=0;i<(linkedList.size()-1);i++){
            if((i+1) <= linkedList.size()) {
                diff = linkedList.get(i).getVolume() - linkedList.get(i + 1).getVolume();
                linkedList.get(i).setUsage(diff);
                cMeterArr.add(linkedList.get(i));
            }
        }
        cMeterArr.add(linkedList.getLast());

        return cMeterArr;
    }

    /**
     * In this method, the usage metrics from the gauge meters are extracted
     *
     * @param token The token generated by the keystone service is used for authorization by Telemetry Service
     * @return output A String output of the success or failure of the data extraction process
     * @throws JSONException
     */
    private boolean setGaugeMeterData(String token){
        boolean saveStatus;
        String response;
        ArrayList<String> meter = new ArrayList<String>();
        JSONArray array ;
        ObjectMapper mapper = new ObjectMapper();
        TSDBResource dbResource = new TSDBResource();
        ArrayList<GaugeMeterData> dataArr = new ArrayList<GaugeMeterData>();
        String meterType  = "gauge";
        boolean output = true;

        meter.add("cpu_util"); //TODO : Remove hard code
        meter.add("disk.read.bytes.rate");
        meter.add("disk.ephemeral.size");
        meter.add("disk.read.requests.rate");
        meter.add("disk.root.size");
        meter.add("disk.write.bytes.rate");
        meter.add("disk.write.requests.rate");
        meter.add("instance");
        meter.add("instance:m1.small");
        meter.add("instance:m1.tiny");
        meter.add("ip.floating");
        meter.add("memory");
        meter.add("network.incoming.bytes.rate");
        meter.add("network.incoming.packets.rate");
        meter.add("network.outgoing.bytes.rate");
        meter.add("network.outgoing.packets.rate");
        meter.add("port");
        meter.add("vcpus");
        meter.add("volume");

        try{
            TelemetryClient tClient = new TelemetryClient();
            for (int i = 0; i < meter.size(); i++) {
                response = tClient.getData(token, meter.get(i), meterType);
                array = new JSONArray(response);
                if (response.toString() != "[]") {
                    for (int j=0; j< array.length(); j++){
                        JSONObject obj = array.getJSONObject(j);
                        GaugeMeterData data = mapper.readValue(obj.toString(),GaugeMeterData.class);
                        dataArr.add(data);
                    }
                    dbResource.saveGaugeMeterData(dataArr, meter.get(i));
                } else {
                    System.out.println("Ceilometer returned empty response for " + meter.get(i));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            output = false;
            return output;
        }
        return output;
    }

    //Reserved for future API extention
    public String getMeterData(String userId)
    {
        return null;
    }
}