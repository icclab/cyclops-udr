package ch.icclab.cyclops.schedule;

import ch.icclab.cyclops.mcn.impl.EventToUDR;
import ch.icclab.cyclops.util.Load;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Implementation of internal scheduler
 */
public class Scheduler {

    final static Logger logger = LogManager.getLogger(Scheduler.class.getName());

    // this class has to be a singleton
    private static Scheduler singleton = new Scheduler();

    // executor service (we only need one thread)
    private ScheduledExecutorService executor;

    /**
     * We need to hide constructor from public
     */
    private Scheduler() {
        this.executor = null;
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static Scheduler getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     */
    public void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();

            // schedule thread immediately and then run it again at specified time
            executor.scheduleAtFixedRate(new EventToUDR(), 0, Load.getInstance().getScheduleFrequency(), TimeUnit.SECONDS);
        }
    }

    /**
     * Stops execution run
     */
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Returns whether scheduler is running or not
     * @return
     */
    public Boolean isRunning() {
        return (executor != null);
    }

    /**
     * Manually (on top of scheduler) force scheduler run
     */
    public void force() {
        new EventToUDR().run();
    }

}
