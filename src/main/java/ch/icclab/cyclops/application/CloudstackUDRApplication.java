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

package ch.icclab.cyclops.application;

import ch.icclab.cyclops.services.iaas.cloudstack.client.CloudStackScheduler;
import ch.icclab.cyclops.services.iaas.cloudstack.model.CloudStackUsageTypes;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.dto.MeterList;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.impl.CloudStackMeter;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.impl.CloudStackEndpoint;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.impl.CloudStackResources;
import ch.icclab.cyclops.services.iaas.cloudstack.resource.impl.CloudStackUsage;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.*;

/**
 * @author Manu
 *         Created by root on 16.11.15.
 */
public class CloudstackUDRApplication extends AbstractApplication {
    @Override
    public void createRoutes() {
        router.attach("/ext/app", ExternalAppResource.class); // API used for data insertion from external PaaS/IaaS
        counter.registerEndpoint("/ext/app");

        //API used for fetching the usage info for a user
        router.attach("/usage/users/{userid}", CloudStackUsage.class);
        counter.registerEndpoint("/usage/users");

        // API endpoint used for requesting resource usage
        router.attach("/usage/resources/{metername}", CloudStackResources.class);
        counter.registerEndpoint("/usage/resources");

        //API used for saving and returning the information on selected meters for usage metrics collection
        router.attach("/meters", CloudStackMeter.class);
        counter.registerEndpoint("/meters");

        // API used to get usage data from CloudStack
        router.attach("/scheduler/{command}", CloudStackEndpoint.class);
        counter.registerEndpoint("/scheduler");

        // also start collection immediately
        CloudStackScheduler.getInstance().start();

    }

    @Override
    public void initialiseDatabases() {
        dbClient.createDatabases(settings.getInfluxDBSettings().getInfluxDBDatabaseName(), settings.getCloudStackSettings().getCloudStackDBLogsName());

        // make sure we have proper meterList in database
        new CloudStackMeter().initializeWithList(CloudStackUsageTypes.getList());
    }

}
