package org.matsim.contrib.spatialDrt.eav;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.contrib.spatialDrt.vehicle.DynVehicleType;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.Map;

public class FastCharger extends Charger {
    public static final double CHARGING_RATE_PER_SECOND =  0.8/60.0;
    static String activityType = "fast";

    public FastCharger(Id<ActivityFacility> id, Link link, int capacity, ActivityOption activityOption, boolean isBlocking) {
        super(id, link,activityOption, capacity, isBlocking);
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
        return ((vehicle.getVehicleType().getBatteryCapacity()) - estimatedBattery) / CHARGING_RATE_PER_SECOND;
    }


    @Override
    public ChargerMode getChargerMode() {
        return ChargerMode.fast;
    }


}