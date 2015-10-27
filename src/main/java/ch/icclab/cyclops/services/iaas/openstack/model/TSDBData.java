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

package ch.icclab.cyclops.services.iaas.openstack.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * <b>POJO Object</b><p/>
 * Author: Srikanta
 * Created on: 15-Oct-14
 * Description: A POJO class for InfluxDB
 *
 * Change Log
 * Name        Date     Comments
 */
public class TSDBData {
    private String name;
    private ArrayList<String> columns;
    private ArrayList<ArrayList<Object>> points;
    private HashMap tags;

    final static Logger logger = LogManager.getLogger(TSDBData.class.getName());
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

    public ArrayList<ArrayList<Object>> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<ArrayList<Object>> points) {
        this.points = points;
    }

    public HashMap getTags() {
        return tags;
    }

    public void setTags(HashMap tags) {
        this.tags = tags;
    }

//    public String toString() {
//        //logger.trace("BEGIN toString()");
//        StringBuilder buf = new StringBuilder(new String(""));
//        //logger.trace("DATA toString(): buf="+buf);
//        buf = buf.append("name: "+this.getName()+"\n ");
//        //logger.trace("DATA toString(): buf="+buf);
//        for (String column : this.getColumns()){
//            buf = buf.append("column: "+column+"\n ");
//            //logger.trace("DATA toString(): buf="+buf);
//        }
//        for (ArrayList<Object> point : this.getPoints()){
//            buf = buf.append("point: ");
//            for (Object value : point) {
//                buf = buf.append(value.toString() + " ");
//            }
//            buf = buf.append("\n ");
//            //logger.trace("DATA toString(): buf="+buf);
//        }
//        //logger.trace("DATA toString(): buf="+buf);
//        /*HashMap mp = this.getTags();
//        //logger.trace("DATA toString(): mp=" + mp.toString());
//        Iterator it = mp.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            buf = buf.append(pair.getKey().toString() + " : " + pair.getValue().toString()+"\n ");
//            //logger.trace("DATA toString(): buf=" + buf);
//            it.remove(); // avoids a ConcurrentModificationException
//        }*/
//        buf = buf.append(this.getTags()+"\n ");
//
//        String result = buf.toString();
//        //logger.trace("DATA toString(): result"+result);
//        //logger.trace("END toString()");
//        return result;
//    }

}