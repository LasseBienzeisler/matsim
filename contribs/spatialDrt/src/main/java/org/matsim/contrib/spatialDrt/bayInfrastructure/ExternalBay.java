package org.matsim.contrib.spatialDrt.bayInfrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public class ExternalBay extends BaySize {
    public ExternalBay(TransitStopFacility transitStop, double linkLength, double minBaySize) {
        super(transitStop, linkLength, minBaySize);
    }

    @Override
    public boolean isQueuing(Id<Vehicle> vid) {
        return queingVehicles.contains(vid);
    }

    @Override
    public boolean isBlocking() {
        return !queingVehicles.isEmpty();
    }
}
