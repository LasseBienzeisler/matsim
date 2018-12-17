package org.matsim.contrib.spatialDrt.vehicle;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.vehicles.VehicleType;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Stack;

public class DrtVehicleTypeReader extends MatsimXmlParser {
    private static final String TYPE = "type";
    private static final int DEFAULT_CAPACITY = 1;
    private static final double DEFAULT_BOARDING = 1.0;
    private static final double DEFAULT_ALIGHTING = 1.0;
    private final Map<String, DynVehicleType> vehicleTypes;


    public DrtVehicleTypeReader(Map<String, DynVehicleType> vehicleTypes) {
        this.vehicleTypes =vehicleTypes;
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if (TYPE.equals(name)) {
            DynVehicleType vehicleType = createVehicleType(atts);
            vehicleTypes.put(vehicleType.getId().toString(), vehicleType);
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {

    }

    private DynVehicleType createVehicleType(Attributes atts) {
        Id<VehicleType> id = Id.create(atts.getValue("id"), VehicleType.class);
        int seats = ReaderUtils.getInt(atts, "seats", DEFAULT_CAPACITY);
        double batteryCapacity = ReaderUtils.getDouble(atts, "battery", Double.MAX_VALUE);
        double maxBatteryMeter = ReaderUtils.getDouble(atts, "max_battery_km", Double.MAX_VALUE) * 1000;
        double length = ReaderUtils.getDouble(atts,"length",7.5);
        double access_time = ReaderUtils.getDouble(atts, "boarding_time", DEFAULT_BOARDING);
        double egress_time = ReaderUtils.getDouble(atts, "alighting_time", DEFAULT_ALIGHTING);
        DynVehicleType dynVehicleType = new DynVehicleType(id, seats, access_time, egress_time);
        dynVehicleType.setBatteryCapacity(batteryCapacity);
        dynVehicleType.setLength(length);
        dynVehicleType.setMaxBatteryMeter(maxBatteryMeter);
        return dynVehicleType;
    }
}
