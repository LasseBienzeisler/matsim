package org.matsim.contrib.spatialDrt.eav;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;


public class Level2Charger extends Charger {
    public static final double CHARGING_RATE_PER_SECOND =  90.0/5.0/3600.0;
    static String activityType = "level2";

    public Level2Charger(Id<ActivityFacility> id, Link link, int capacity, ActivityOption activityOption, boolean isBlocking) {
        super(id, link,  activityOption,capacity, isBlocking);
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