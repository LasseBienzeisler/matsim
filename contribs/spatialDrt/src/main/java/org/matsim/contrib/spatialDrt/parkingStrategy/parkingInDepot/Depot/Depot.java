package org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.utils.objectattributes.attributable.Attributable;

public interface Depot extends ActivityFacility{
    static String activityType = "parking";
    public static enum DepotType{
        DEPOT,
        HDB;
    }
    public Link getLink();

    public double getCapacity();

    public void addVehicle(Id<Vehicle> vehicleId);


    public void removeVehicle(Id<Vehicle> vehicleId);

    public double getNumOfVehicles();

    public Id<ActivityFacility> getId();

    public DepotType getDepotType();

    public boolean isOpen(double time);
}
