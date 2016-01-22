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

import java.util.HashMap;

/**
 * Author: Martin Skoviera
 * Created on: 04-Nov-15
 * Description: Class that will count API Calls and then provide the information (and reset counters)
 */
public class APICallCounter {

    // we need a singleton for this
    private static APICallCounter singleton = new APICallCounter();

    // Hash-map to be used for counting
    private HashMap<String, Integer> map;

    // Hash-map used as a template (for registering fields)
    private HashMap<String, Integer> template;

    /**
     * Private Constructor is necessary for the singleton model
     */
    private APICallCounter() {
        template = new HashMap<String, Integer>();
        map = new HashMap<String, Integer>();
    }

    /**
     * Simply return singleton instance
     *
     * @return
     */
    public static APICallCounter getInstance() {
        return singleton;
    }

    /**
     * Will increment the counter for provided key
     * In case that this key was not previously registered, it will add it to the hash-map
     * However on dump, it will get removed and only the registered ones will stay
     *
     * @param key meaning API endpoint
     */
    public void increment(String key) {
        Object value = map.get(key);

        // now save new value or increment it
        if (value == null) {
            map.put(key, 1);
        } else {
            map.put(key, ((Integer) value) + 1);
        }
    }

    /**
     * Register the API endpoint to the counter
     *
     * @param key as endpoint string
     */
    public void registerEndpoint(String key) {
        template.put(key, 0);
        map.put(key, 0);
    }

    /**
     * Will ask for running stats and DUMP the old hash-map so it can start from zero again
     *
     * @return hash-map
     */
    public HashMap<String, Integer> getRunningStats() {
        // make a deep copy
        HashMap<String, Integer> stats = new HashMap<String, Integer>();
        stats.putAll(map);

        // reset map based on registered entries in template
        map = new HashMap<String, Integer>();
        map.putAll(template);

        // return deep copy
        return stats;
    }
}