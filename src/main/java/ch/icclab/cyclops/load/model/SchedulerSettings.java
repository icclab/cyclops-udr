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
 * Description: Settings for Internal Scheduler
 */
public class SchedulerSettings {
    private long schedulerFrequency;
    private static final long defaultFrequency = 300;

    /**
     * Constructor that will save either default frequency, or provided one
     * @param frequency in seconds
     */
    public SchedulerSettings(String frequency) {
        try {
            long freq = Long.parseLong(frequency);

            this.schedulerFrequency = (freq > 0)? freq : defaultFrequency;
        } catch (Exception e) {
            this.schedulerFrequency = defaultFrequency;
        }
    }

    //==== we only need getters
    public long getSchedulerFrequency() {
        return schedulerFrequency;
    }
}
