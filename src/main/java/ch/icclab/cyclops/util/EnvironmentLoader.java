package ch.icclab.cyclops.util;

/**
 * @author Manu
 *         Created by root on 09.11.15.
 */
public class EnvironmentLoader {
    protected String start;
    protected String queue_name;
    protected String events_dbname;

    public String getStart() {
        return start;
    }

    public String getQueue_name() {
        return queue_name;
    }

    public String getEvents_dbname() {
        return events_dbname;
    }
}



//    public void method() {
//        EnvironmentLoader environment;
//
//        if ("tnova".equalsIgnoreCase("tnova")) {
//            environment = new TnovaEnvironment();
//        }
//
//        environment.start;
//
//    }
