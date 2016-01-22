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
package ch.icclab.cyclops.usecases.tnova.rabbitMQClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Martin Skoviera
 * Created on: 09-Nov-15
 * Description: Implementation of thread executor for RabbitMQ
 */
public class TnovaThreadExecutor {

    final static Logger logger = LogManager.getLogger(TnovaThreadExecutor.class.getName());

    // this class has to be a singleton
    private static TnovaThreadExecutor singleton = new TnovaThreadExecutor();

    // executor service (we only need one thread)
    private ExecutorService executor;

    // RabbitMQ object that will be used for listening
    private TnovaRabbitMQClient rabbitmq;

    /**
     * We need to hide constructor from public
     */
    private TnovaThreadExecutor() {
        this.executor = null;
    }

    /**
     * Simple implementation of Singleton class
     *
     * @return instance of scheduler object
     */
    public static TnovaThreadExecutor getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     */
    public void start(String queueName) {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();

            this.rabbitmq = new TnovaRabbitMQClient(queueName);
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
