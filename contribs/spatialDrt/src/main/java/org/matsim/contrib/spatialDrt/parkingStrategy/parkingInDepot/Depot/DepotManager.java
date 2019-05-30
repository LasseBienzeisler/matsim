package org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.facilities.ActivityFacility;

import java.util.Map;

public interface DepotManager {



     void addDepot(Depot depot);

     Map<Id<ActivityFacility>, Depot> getDepots(double time);
     Map<Id<ActivityFacility>, Depot> getDepots(Depot.DepotType depotType, double time);

     boolean isVehicleInDepot(Vehicle vehicle);

     void registerVehicle(Id<Vehicle> vid, Id<ActivityFacility> did);

     Depot getDepotOfVehicle(Vehicle vehicle) ;

     void vehicleLeavingDepot(Vehicle vehicle) ;

     Map<Id<ActivityFacility>, Depot> getDepots(double capacity, double time);
}
