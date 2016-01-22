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
package ch.icclab.cyclops.services.iaas.cloudstack.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 17-Nov-15
 * Description: list of CloudStack usage types
 */
public class CloudStackUsageTypes {

    /**
     * Access list of vanilla usage record types
     *
     * @return list
     */
    public static List<String> getList() {
        List<String> list = new ArrayList<String>();

        // we have to add them manually
        list.add(new VMUsageData().getMeterNameForRunning());
        list.add(new VMUsageData().getMeterNameForAllocated());
        list.add(new IPUsageData().getMeterName());
        list.add(new NetworkUsageData().getMeterNameForOutgoing());
        list.add(new NetworkUsageData().getMeterNameForIncoming());
        list.add(new VolumeUsageData().getMeterName());
        list.add(new TemplateAndIsoUsageData().getMeterNameTemplate());
        list.add(new TemplateAndIsoUsageData().getMeterNameISO());
        list.add(new SnapshotUsageData().getMeterName());
        list.add(new PolicyOrRuleUsageData().getMeterNameBalancer());
        list.add(new PolicyOrRuleUsageData().getMeterNameForwarder());
        list.add(new NetworkOfferingUsageData().getMeterName());
        list.add(new VPNUserUsageData().getMeterName());

        return list;
    }

}
