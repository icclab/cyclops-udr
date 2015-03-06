package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.udr.TSDBData;
import ch.icclab.cyclops.model.udr.UserUsageResponse;
import ch.icclab.cyclops.persistence.impl.TSDBResource;
import ch.icclab.cyclops.resource.interfc.UsageResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 21-Jan-15
 * Description:  Services the API GET request coming in for usage data for a given user ID.
 *
 * Change Log
 * Name        Date     Comments
 */
public class UsageDataResource extends ServerResource implements UsageResource {

    /**
     *  Get the usage data for the userID mentioned in the URL string
     *  
     *  Pseudo Code
     *  1. Extract the QueryValues from the URL
     *  2. Add the meters name to the meter list
     *  3. Query the DB to get the usage data and add the response to an array list
     *  4. Add the array list with its source name into a HashMap
     *  5. Construct the response and return it in the JSON format
     *    
     * @return userUsageResponse Response consisting of the usage data for the request userID
     */
    @Get
    public Representation getData(){
        
        Representation userUsageResponse;
        TSDBData usageData;
        HashMap usageArr = new HashMap();
        ArrayList<TSDBData> meterDataArrList = new ArrayList<TSDBData>(); //TODO : Remove hard code
        
        String userId = getQueryValue("userid");
        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");

        TSDBResource dbResource = new TSDBResource();
        ArrayList cMeters = new ArrayList();
        ArrayList gMeters = new ArrayList();
        
        //Get data from OpenStack
        //Get Cumulative Meters from Openstack
        //Add the time series name to fetch the total usage data
        cMeters.add("network.incoming.bytes"); //TODO : Remove hard code
        cMeters.add("network.outgoing.bytes");

        //Get the data from the DB and create the arraylist consisting of hashmaps of meter name and usage value
        for(int i=0;i<cMeters.size(); i++){
            usageData = dbResource.getUsageData(fromDate, toDate, userId, cMeters.get(i), "openstack", "cumulative");
            meterDataArrList.add(usageData);
        }
        
        //Get Gauge Meters from Openstack
        gMeters.add("cpu_util"); //TODO : Remove hard code
        gMeters.add("disk.read.bytes.rate");
        gMeters.add("disk.write.bytes.rate");
        gMeters.add("network.incoming.bytes.rate");
        gMeters.add("network.outgoing.bytes.rate");

        for(int i=0;i<gMeters.size(); i++){
            usageData = dbResource.getUsageData(fromDate, toDate, userId, gMeters.get(i), "openstack", "gauge");
            meterDataArrList.add(usageData);
        }
        
        usageArr.put("openstack",meterDataArrList);
        //Construct the response in JSON string
        userUsageResponse = constructResponse(usageArr, userId, fromDate, toDate);
        
        return userUsageResponse;
    }

    /**
     * * Construct the JSON response consisting of the meter and the usage values
     * * 
     * * Pseudo Code
     * * 1. Create the HasMap consisting of time range
     * * 2. Create the response POJO
     * * 3. Convert the POJO to JSON
     * * 4. Return the JSON string
     *  
     * @param usageArr An arraylist consisting of metername and corresponding usage
     * @param userId UserID for which the usage details is to be returned.
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructResponse(HashMap usageArr, String userId, String fromDate, String toDate){

        String jsonStr;
        JsonRepresentation responseJson = null;
        
        UserUsageResponse responseObj = new UserUsageResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();
        
        time.put("from",fromDate);
        time.put("to",toDate);
        
        //Build the response POJO
        responseObj.setUserid(userId);
        responseObj.setTime(time);
        responseObj.setUsage(usageArr);

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }
    
}
