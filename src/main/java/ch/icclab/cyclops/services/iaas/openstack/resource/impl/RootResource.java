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

package ch.icclab.cyclops.services.iaas.openstack.resource.impl;

/**
 * Author: Srikanta
 * Created on: 06-Oct-14
 * Description: A class to handle the root API endpoint
 */

import ch.icclab.cyclops.services.iaas.openstack.resource.interfc.UDRResource;
import ch.icclab.cyclops.util.APICallCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Returns a basic response when the root API endpoint is invoked
 */
public class RootResource extends ServerResource implements UDRResource {
    final static Logger logger = LogManager.getLogger(RootResource.class.getName());
    private String endpoint = "/";
    private APICallCounter counter = APICallCounter.getInstance();

    @Get
    public String rootMsg() {
        counter.increment(endpoint);
        logger.trace("BEGIN String rootMsg()");
        String response = "CYCLOPS UDR Service - v 0.6.0";
        logger.trace("END String rootMsg()");
        return response;
    }
}
