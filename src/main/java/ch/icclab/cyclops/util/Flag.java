package ch.icclab.cyclops.util;

/**
 * Author: Srikanta
 * Created on: 03-Mar-15
 * Description:
 *
 * Change Log
 * Name        Date     Comments
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
