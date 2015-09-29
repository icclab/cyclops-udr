package ch.icclab.cyclops.util;

/**
 * Author: Srikanta
 * Created on: 03-Mar-15
 * Description: This class is used to check if there has been any changes done to the meterlist. If yes, then it
 * will trigger the logic to query the db for the latest list of selected meters.
 */
public class Flag {
    private static boolean meterListReset = true;

    public static boolean isMeterListReset() {
        return meterListReset;
    }

    public static void setMeterListReset(boolean meterListReset) {
        Flag.meterListReset = meterListReset;
    }
}
