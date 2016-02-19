package ch.icclab.cyclops.usecases.external.model;

/**
 * Created by manu on 17/02/16.
 */
public class ExternalDataPoint {

    private String source;
    private Double usage;
    private String meterName;
    private long timestamp;
    private String userId;

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUsage(Double usage) {
        this.usage = usage;
    }

    public String getSource() {
        return source;
    }

    public Double getUsage() {
        return usage;
    }

    public String getMeterName() {
        return meterName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }
}
