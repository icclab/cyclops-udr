package ch.icclab.cyclops.util;

/**
 * @author Manu
 * Created by root on 09.11.15.
 */
public class TnovaEnvironment extends EnvironmentLoader {
    public TnovaEnvironment() {
        this.start = "running";
        this.events_dbname = "tnova_events";
        this.queue_name = "event";
    }
}
