package ch.icclab.cyclops.util;

/**
 * @author Manu
 * Created by root on 09.11.15.
 */
public class McnEnvironment extends EnvironmentLoader{
    public McnEnvironment() {
        this.start = "start";
        this.events_dbname = "mcn_events";
        this.queue_name = "mcnevents";
    }
}
