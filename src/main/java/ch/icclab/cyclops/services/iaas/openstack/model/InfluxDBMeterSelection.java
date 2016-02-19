package ch.icclab.cyclops.services.iaas.openstack.model;

import com.rabbitmq.client.AMQP;

import java.util.ArrayList;

/**
 * Created by manu on 19/02/16.
 */
public class InfluxDBMeterSelection {
    private String name;
    private ArrayList<ArrayList<String>> columns;
    private ArrayList<ArrayList<String>> points;
    private ArrayList<ArrayList<String>> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ArrayList<String>> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<ArrayList<String>> columns) {
        this.columns = columns;
    }

    public ArrayList<ArrayList<String>> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<ArrayList<String>> points) {
        this.points = points;
    }

    public ArrayList<ArrayList<String>> getTags() {
        return tags;
    }

    public void setTags(ArrayList<ArrayList<String>> tags) {
        this.tags = tags;
    }

    public OpenstackMeter[] standarizeFromInfluxDB(){
        ArrayList<OpenstackMeter> meters = new ArrayList<OpenstackMeter>();
        int nameIndex = columns.indexOf("metername");
        int enabledIndex = columns.indexOf("status");
        int typeIndex = columns.indexOf("metertype");
        int sourceIndex = columns.indexOf("source");

        for (int i = 0; i < points.size(); i++) {
            OpenstackMeter meter = new OpenstackMeter();
            meter.setName(points.get(i).get(nameIndex));
            meter.setSource(points.get(i).get(sourceIndex));
            meter.setType(points.get(i).get(typeIndex));

            if (meter.getType().equals(""))
                meter.setType("gauge");

            meters.add(meter);
        }
        return (OpenstackMeter[]) meters.toArray();
    }
}
