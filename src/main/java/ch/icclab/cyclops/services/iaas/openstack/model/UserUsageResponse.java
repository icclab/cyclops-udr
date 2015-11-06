package ch.icclab.cyclops.services.iaas.openstack.model;

import java.util.HashMap;

/**
 * <b>POJO Object</b><p/>
 * Author: Srikanta
 * Created on: 26-Jan-15
 * Description:
 * Change Log
 * Name        Date     Comments
 */
public class UserUsageResponse {
    private String userid;
    private HashMap time;
    private HashMap usage;

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

    public HashMap getUsage() {
        return usage;
    }

    public void setUsage(HashMap usage) {
        this.usage = usage;
    }
}
