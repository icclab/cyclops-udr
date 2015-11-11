package ch.icclab.cyclops.rabbitMQClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Listener for shutting down the RabbitMQ thread
 */
public class ThreadListener implements ServletContextListener {
    final static Logger logger = LogManager.getLogger(ThreadListener.class.getName());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("RabbitMQ ThreadListener - successfully loaded");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.trace("RabbitMQ ThreadListener - we are shutting down");
        ThreadExecutor.getInstance().stop();
    }
}
