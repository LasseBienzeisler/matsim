package org.matsim.contrib.spatialDrt.bayInfrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public class CurbSide extends BaySize {
    public CurbSide(TransitStopFacility transitStop, double linkLength, double minBaySize) {
        super(transitStop, linkLength, minBaySize);
    }

    @Override
    public boolean isQueuing(Id<Vehicle> vid) {
        return dwellingVehicles.contains(vid) || queingVehicles.contains(vid);
    }

    @Override
    protected boolean isBlocking() {
        return !dwellingVehicles.isEmpty() || !dwellingVehicles.isEmpty();
    }

}
