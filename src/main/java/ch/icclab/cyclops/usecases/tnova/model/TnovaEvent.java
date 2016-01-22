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
package ch.icclab.cyclops.usecases.tnova.model;

import ch.icclab.cyclops.services.iaas.openstack.model.Event;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Manu
 * Date: 25/11/2015
 * Description: This class holds the Tnova Event data
 */
public class TnovaEvent extends Event{

    private int id;
    private String flavour;

    public TnovaEvent(ArrayList<Object> point, HashMap<String, Integer> columnMap) {
        this.clientId = (String) point.get(columnMap.get("clientId"));
        this.instanceId = (String) point.get(columnMap.get("instanceId"));
        this.productType = (String) point.get(columnMap.get("productType"));
        this.status = (String) point.get(columnMap.get("status"));
        this.dateModified = (String) point.get(columnMap.get("time"));
        this.productId = (String) point.get(columnMap.get("productId"));
        this.agreementId = (String) point.get(columnMap.get("agreementId"));
        this.relatives = (String) point.get(columnMap.get("relatives"));
        this.startDate = (String) point.get(columnMap.get("startDate"));
        this.lastBillDate = (String) point.get(columnMap.get("lastBillDate"));
        this.providerId = (String) point.get(columnMap.get("providerId"));
        this.billingModel = (String) point.get(columnMap.get("billingModel"));
        this.period = (String) point.get(columnMap.get("period"));
        this.priceUnit = (String) point.get(columnMap.get("priceUnit"));
        this.periodCost =  Double.valueOf(String.valueOf(point.get(columnMap.get("periodCost"))));
        //this.setupCost = (Double) point.get(columnMap.get("setupCost"));
        this.renew = (Boolean) point.get(columnMap.get("renew"));
        this.dateCreated = (String) point.get(columnMap.get("dateCreated"));

    }

    /**
     * This public method will access data and create db Point
     *
     * @return db point
     */
    public Point toPoint() {
        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        return Point.measurement(getMeterName())
                .tag(tags)
                .fields(fields)
                .build();
    }

    /**
     * Get tags for point generation
     *
     * @return hashmap
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("clientId", clientId);
        map.put("productType", productType);
        map.put("instanceId", instanceId);
        map.put("providerId", providerId);
        map.put("productId", productId);

        return map;
    }

    /**
     * Get fields for point generation
     *
     * @return hashmap
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("agreementId", agreementId);
        map.put("relatives", relatives);
        map.put("flavour", relatives);
        map.put("startDate", startDate);
        map.put("lastBillDate", lastBillDate);
        map.put("status", status);
        map.put("billingModel", billingModel);
        map.put("period", period);
        map.put("priceUnit", priceUnit);
        map.put("periodCost", periodCost);
        map.put("status", status);
        map.put("renew", renew);
        map.put("dateCreated", dateCreated);
        map.put("dateModified", dateModified);

        return map;
    }

    /**
     * @return meter name
     */
    private String getMeterName() {
        return "events";
    }

    /**
     * Make sure we are not having any null values
     *
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    public int getId() {
        return id;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getAgreementId() {
        return agreementId;
    }

    @Override
    public String getRelatives() {
        return relatives;
    }

    @Override
    public String getProductType() {
        return productType;
    }

    public String getFlavour() {
        return flavour;
    }

    @Override
    public String getStartDate() {
        return startDate;
    }

    @Override
    public String getLastBillDate() {
        return lastBillDate;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getBillingModel() {
        return billingModel;
    }

    @Override
    public String getPeriod() {
        return period;
    }

    @Override
    public String getPriceUnit() {
        return priceUnit;
    }

    @Override
    public double getPeriodCost() {
        return periodCost;
    }

    @Override
    public double getSetupCost() {
        return setupCost;
    }

    @Override
    public boolean getRenew() {
        return renew;
    }

    @Override
    public String getDateCreated() {
        return dateCreated;
    }

    @Override
    public String getDateModified() {
        return dateModified;
    }
}
