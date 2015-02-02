package ch.icclab.cyclops.resource.interfc;

import org.restlet.representation.Representation;

import java.util.ArrayList;

/**
 * Author: Srikanta
 * Created on: 21-Jan-15
 * Description:
 * Change Log
 * Name        Date     Comments
 */
public interface UsageResource extends UDRResource {

    public Representation getData();
    
    public Representation constructResponse(ArrayList usageArr, String userId, String fromDate, String toDate);
}
