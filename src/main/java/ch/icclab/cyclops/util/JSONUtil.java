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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * Created by Konstantin on 21.10.2015.
 */
public class JSONUtil {



        /**
         * Converts the response object into a JSON string
         *
         * @param pojoObj a response POJO with values set
         * @return a Representation object with JSON string
         */

        public Representation toJson(Object pojoObj) {
            ObjectMapper mapper = new ObjectMapper();
            String output;
            JsonRepresentation responseJson = null;

            try {
                output = mapper.writeValueAsString(pojoObj);
                responseJson = new JsonRepresentation(output);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return responseJson;
        }


}
