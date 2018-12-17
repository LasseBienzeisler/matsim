package org.matsim.contrib.spatialDrt.eav;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;


public class Level2Charger extends Charger {
    public static final double CHARGING_RATE_PER_SECOND =  8.0/75.0/60.0;

    public Level2Charger(Id<Charger> id, Link link, int capacity, double startTime, double endTime, boolean isBlocking) {
        super(id, link, capacity, startTime, endTime, isBlocking);
    }

    @Override
    public double getChargingRate() {
        return CHARGING_RATE_PER_SECOND;
    }

    @Override
    public double getChargingTime(VehicleImpl vehicle) {
        return ((vehicle.getVehicleType()).getBatteryCapacity() - vehicle.getBattery()) / CHARGING_RATE_PER_SECOND;
    }

    @Override
    public double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery) {
        return (((vehicle.getVehicleType()).getBatteryCapacity()) - estimatedBattery) / CHARGING_RATE_PER_SECOND;
    }


    @Override
    public ChargerMode getChargerMode() {
        return ChargerMode.Level2;
    }
}