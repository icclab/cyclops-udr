package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.udr.UserUsageResponse;
import ch.icclab.cyclops.persistence.client.InfluxDBClient;
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
     *  3. Query the DB to get the usage data
     *  4. Construct the response and return it in the JSON format
     *    
     * @return Representation
     */
    @Get
    public Representation getData(){
        
        Representation udrResponse;
        Double usageData;
        HashMap usageDataMap;
        ArrayList<HashMap> usageArr = new ArrayList<HashMap>();
        
        String userId = getQueryValue("userid");
        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");

        InfluxDBClient dbClient = new InfluxDBClient();
        ArrayList meters = new ArrayList();
        
        //Add the time series name to fetch the total usage data
        meters.add("network.incoming.bytes"); //TODO : Remove hard code
        meters.add("network.outgoing.bytes");

        //Get the data from the DB and create the arraylist consisting of hashmaps of meter name and usage value
        for(int i=0;i<meters.size(); i++){
            usageDataMap = new HashMap();
            usageData = dbClient.getData(fromDate, toDate, userId, meters.get(i));
            usageDataMap.put("meter",meters.get(i));
            usageDataMap.put("value",usageData);
            usageArr.add(usageDataMap);
        }
        
        //Construct the response in JSON string
        udrResponse = constructResponse(usageArr, userId, fromDate, toDate);
        
        return udrResponse;
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
     * @param usageArr
     * @param userId
     * @param fromDate
     * @param toDate
     * @return
     */
    public Representation constructResponse(ArrayList usageArr, String userId, String fromDate, String toDate){

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
