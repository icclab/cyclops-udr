package ch.icclab.cyclops.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Implementation of listener for internal scheduler
 */
public class Listener implements ServletContextListener {

    final static Logger logger = LogManager.getLogger(Listener.class.getName());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("UDR Listener - successfully loaded");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.trace("UDR Listener - we are shutting down");
        Scheduler.getInstance().stop();
    }
}
