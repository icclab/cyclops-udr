/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */
package ch.icclab.cyclops.services.iaas.cloudstack.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Author: Martin Skoviera
 * Created on: 23-Oct-15
 * Description: Servlet listener that will shut down any running schedulers
 */
public class CloudStackListener implements ServletContextListener {
    final static Logger logger = LogManager.getLogger(CloudStackListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("CloudStack Listener - successfully loaded");
    }

    /**
     * We have to mercifully shut down our scheduler and planned tasks
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.trace("Cloudstack Listener - we are shutting down");
        CloudStackScheduler.getInstance().stop();
    }
}
