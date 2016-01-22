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

package ch.icclab.cyclops.util;

import ch.icclab.cyclops.services.iaas.openstack.model.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * Author: Srikanta
 * Created on: 16-Jan-15
 * Description: Util class for doing operation on response object
 */
public class ResponseUtil {

    /**
     * Converts the response object into a JSON string
     *
     * @param response a response POJO with values set
     * @return a Representation object with JSON string
     */

    public Representation toJson(Response response) {
        ObjectMapper mapper = new ObjectMapper();
        String output;
        JsonRepresentation responseJson = null;

        try {
            output = mapper.writeValueAsString(response);
            responseJson = new JsonRepresentation(output);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return responseJson;
    }
}
