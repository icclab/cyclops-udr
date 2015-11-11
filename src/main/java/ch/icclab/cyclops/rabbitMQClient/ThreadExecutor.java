package ch.icclab.cyclops.rabbitMQClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Martin Skoviera
 * Created on: 09-Nov-15
 * Description: Implementation of thread executor for RabbitMQ
 */
public class ThreadExecutor {

    final static Logger logger = LogManager.getLogger(ThreadExecutor.class.getName());

    // this class has to be a singleton
    private static ThreadExecutor singleton = new ThreadExecutor();

    // executor service (we only need one thread)
    private ExecutorService executor;

    // RabbitMQ object that will be used for listening
    private RabbitMQClient rabbitmq;

    /**
     * We need to hide constructor from public
     */
    private ThreadExecutor() {
        this.executor = null;
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static ThreadExecutor getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     */
    public void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();

            this.rabbitmq = new RabbitMQClient("MCN Events Handler");
            executor.submit(rabbitmq);
        }
    }

    /**
     * Stops execution run
     */
    public void stop() {
        if (executor != null) {

            // stop listening
            rabbitmq.stopListening();
            rabbitmq = null;

            // shut down thread
            executor.shutdownNow();
            executor = null;
        }
    }
}
