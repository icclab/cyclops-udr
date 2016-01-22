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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Manu
 *         Created on 09.12.15.
 */
public class TnovaTSDBData {
    private String name;
    private ArrayList<String> columns;
    private ArrayList<ArrayList<Object>> values;
    private HashMap tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    public ArrayList<ArrayList<Object>> getValues() {
        return values;
    }

    public void setValues(ArrayList<ArrayList<Object>> values) {
        this.values = values;
    }

    public HashMap getTags() {
        return tags;
    }

    public void setTags(HashMap tags) {
        this.tags = tags;
    }
}
