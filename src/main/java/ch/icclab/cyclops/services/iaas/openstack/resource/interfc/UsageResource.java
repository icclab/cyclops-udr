package ch.icclab.cyclops.services.iaas.openstack.resource.interfc;

import org.restlet.representation.Representation;

import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 21-Jan-15
 * Description:
 * Change Log
 * Name        Date     Comments
 */
public interface UsageResource extends UDRResource {

    public Representation getData();
    
    public Representation constructResponse(HashMap usageArr, String userId, String fromDate, String toDate);
}
