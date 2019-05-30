package org.matsim.contrib.spatialDrt.vehicle;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleCapacityImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;


public class DynVehicleType extends VehicleTypeImpl {
    public static String DYNTYPE = "dynType";

    private double batteryCapacity;

    private double maxBatteryMeter;

    private String mode;




    public DynVehicleType(Id<VehicleType> id, int seats, double accessTime, double egressTime, String mode){
        super(id);
        VehicleCapacity capacity = new VehicleCapacityImpl();
        capacity.setSeats(Integer.valueOf(seats));
        setCapacity(capacity);
        setAccessTime(accessTime);
        setEgressTime(egressTime);
        setMode(mode);
    }


    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public int getSeats() {
        return getCapacity().getSeats();
    }

    public static DynVehicleType defaultDynVehicleType(Id<VehicleType> id){
        return new DynVehicleType(id, 0, 0.0,0.0, null);
    }

    public void setMaxBatteryMeter(double maxBatteryMeter) {
        this.maxBatteryMeter = maxBatteryMeter;
    }

    public double getMaxBatteryMeter() {
        return maxBatteryMeter;
    }

    public double getDischargingRate() {
        return batteryCapacity / maxBatteryMeter;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
