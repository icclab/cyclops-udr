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

import ch.icclab.cyclops.usecases.tnova.impl.TnovaUDRResponse;

import java.util.ArrayList;

/**
 * Created by Manu on 04/01/16.
 */
public class TnovaInstanceUsage {
    private String name;
    private String from;
    private String to;
    private String instanceId;
    private TnovaUDRResponse[] usages;

    public void setName(String name) {
        this.name = name;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setUsages(ArrayList<TnovaUDRResponse> charges) {
        this.usages = new TnovaUDRResponse[charges.size()];
        ArrayList<TnovaUDRResponse> dailyCharges = new ArrayList<TnovaUDRResponse>();
        double dayUsage = 0.0;
        for(int i = 0; i<charges.size(); i++){
            if(i != 0 && charges.get(i-1) != null){
                if(sameDay(charges.get(i-1).getTime(), charges.get(i).getTime())){
                    dayUsage = dayUsage + charges.get(i).getUsage();
                }else{
                    TnovaUDRResponse dailyResponse = new TnovaUDRResponse();
                    dailyResponse.setTime(charges.get(i-1).getTime());
                    dailyResponse.setUsage(dayUsage);
                    dailyCharges.add(dailyResponse);
                    //And dayUsage gets new days Usage.
                    dayUsage = charges.get(i).getUsage();
                }
            }if(i == 0){
                dayUsage = charges.get(i).getUsage();
            }if(i == charges.size()-1){
                TnovaUDRResponse dailyResponse = new TnovaUDRResponse();
                dailyResponse.setTime(charges.get(i).getTime());
                dailyResponse.setUsage(dayUsage);
                dailyCharges.add(dailyResponse);
            }
        }

        for(int i = 0; i<dailyCharges.size(); i++){
            this.usages[i] = dailyCharges.get(i);
        }
    }

    private boolean sameDay(String before, String now){
        int previousDay = Integer.parseInt(before.split("-")[0]);
        int currentDay = Integer.parseInt(now.split("-")[0]);
        return previousDay == currentDay;
    }
}
