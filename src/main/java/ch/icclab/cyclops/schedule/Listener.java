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
