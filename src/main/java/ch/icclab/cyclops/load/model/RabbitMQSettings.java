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

package ch.icclab.cyclops.load.model;

/* Author: Martin Skoviera
 * Created on: 16-Nov-15
 * Description: Settings for RabbitMQ
 */
public class RabbitMQSettings {
    private String RabbitMQUsername;
    private String RabbitMQPassword;
    private String RabbitMQHost;
    private Integer RabbitMQPort;
    private String RabbitMQVirtualHost;

    /**
     * Simple constructor saving all provided information
     */
    public RabbitMQSettings(String rabbitMQUsername, String rabbitMQPassword, String rabbitMQHost, String rabbitMQPort, String rabbitMQVirtualHost) {
        RabbitMQUsername = rabbitMQUsername;
        RabbitMQPassword = rabbitMQPassword;
        RabbitMQHost = rabbitMQHost;

        // we can only work with real ports
        try {
            Integer tmp = Integer.parseInt(rabbitMQPort);
            if (tmp > 0) {
                RabbitMQPort = tmp;
            } else {
                RabbitMQPort = 0;
            }
        } catch (Exception e) {
            RabbitMQPort = 0;
        }

        RabbitMQVirtualHost = rabbitMQVirtualHost;
    }

    // ==== We need only getters
    public String getRabbitMQUsername() {
        return RabbitMQUsername;
    }
    public String getRabbitMQPassword() {
        return RabbitMQPassword;
    }
    public String getRabbitMQHost() {
        return RabbitMQHost;
    }
    public Integer getRabbitMQPort() {
        return RabbitMQPort;
    }
    public String getRabbitMQVirtualHost() {
        return RabbitMQVirtualHost;
    }
}
