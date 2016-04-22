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

/**
 * Author: Serhiienko Oleksii
 * Created on: 5-April-16
 */
public class OpenstackCollectorSettings {
    private String OpenstackCollectorDBEventsName;
    private String OpenstackCollectorEventStart;
    private String OpenstackCollectorEventSpawn;
    private String OpenstackCollectorEventUnpause;
    private String OpenstackCollectorEventResume;
    private String OpenstackCollectorEventQueue;

    // Constructor
    public OpenstackCollectorSettings(String OpenstackCollectorDBEventsName, String OpenstackCollectorEventStart, String OpenstackCollectorEventQueue,
                                      String OpenstackCollectorEventSpawn, String OpenstackCollectorEventUnpause, String OpenstackCollectorEventResume) {
        this.OpenstackCollectorDBEventsName = OpenstackCollectorDBEventsName;
        this.OpenstackCollectorEventStart = OpenstackCollectorEventStart;
        this.OpenstackCollectorEventSpawn = OpenstackCollectorEventSpawn;
        this.OpenstackCollectorEventUnpause = OpenstackCollectorEventUnpause;
        this.OpenstackCollectorEventResume = OpenstackCollectorEventResume;
        this.OpenstackCollectorEventQueue = OpenstackCollectorEventQueue;
    }

    //=== Getters
    public String getOpenstackCollectorDBEventsName() {
        return OpenstackCollectorDBEventsName;
    }
    public String getOpenstackCollectorEventStart() {
        return OpenstackCollectorEventStart;
    }
    public String getOpenstackCollectorEventSpawn() {
        return OpenstackCollectorEventSpawn;
    }
    public String getOpenstackCollectorEventUnpause() {
        return OpenstackCollectorEventUnpause;
    }
    public String getOpenstackCollectorEventResume() {
        return OpenstackCollectorEventResume;
    }
    public String getOpenstackCollectorEventQueue() {
        return OpenstackCollectorEventQueue;
    }
}
