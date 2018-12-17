package org.matsim.contrib.spatialDrt.eav;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

public interface ChargingStrategy {
    public enum Strategies{
        EarlyReserved;
    }
    ChargerPathPair charging(Vehicle vehicle, double time);

    void leaving(Vehicle vehicle, double time);

    ChargingStrategy.Strategies getCurrentStrategy(Id<Vehicle> vehicleId);

}
