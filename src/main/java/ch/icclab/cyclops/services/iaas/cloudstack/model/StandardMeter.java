package ch.icclab.cyclops.services.iaas.cloudstack.model;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 24/02/16.
 */

public class StandardMeter {
    private String counter_name;
    private String source;
    private String counter_type;

    public StandardMeter(String meterName, String meterSource) {
        this.counter_name = meterName;
        this.counter_type = "gauge";
        this.source = meterSource;
    }

    public String getCounter_name() {
        return counter_name;
    }

    public void setCounter_name(String counter_name) {
        this.counter_name = counter_name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCounter_type() {
        return counter_type;
    }

    public void setCounter_type(String counter_type) {
        this.counter_type = counter_type;
    }
}
