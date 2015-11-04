package ch.icclab.cyclops.util;

import com.google.gson.Gson;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.HashMap;

/**
 * Author: Martin Skoviera
 * Created on: 04-Nov-15
 * Description: This class handles the internal APICallCounter
 */
public class APICallEndpoint extends ServerResource {

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    // who am I?
    private String endpoint = "/status";

    /**
     * This method will return JSON stats for APICallCounter object
     * @return JSON
     */
    @Get
    public String processCommand() {

        // we should also log this entry
        counter.increment(endpoint);

        // first get running statistics
        HashMap<String, Integer> stats = counter.getRunningStats();

        // now transform it to JSON
        Gson gson = new Gson();
        String json = gson.toJson(stats);

        // and return
        return json;
    }
}