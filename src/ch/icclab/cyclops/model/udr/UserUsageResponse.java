package ch.icclab.cyclops.model.udr;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 26-Jan-15
 * Description:
 * Change Log
 * Name        Date     Comments
 */
public class UserUsageResponse {
    private String userid;
    private HashMap time;
    private ArrayList<HashMap> usage;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public HashMap getTime() {
        return time;
    }

    public void setTime(HashMap time) {
        this.time = time;
    }

    public ArrayList<HashMap> getUsage() {
        return usage;
    }

    public void setUsage(ArrayList<HashMap> usage) {
        this.usage = usage;
    }
}
