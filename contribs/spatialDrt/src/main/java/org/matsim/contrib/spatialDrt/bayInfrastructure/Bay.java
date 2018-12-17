package org.matsim.contrib.spatialDrt.bayInfrastructure;

import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Bay {
    private final TransitStopFacility transitStop;
    private final Id<Link> linkId;

    protected Queue<Id<Vehicle>> queingVehicles = new ConcurrentLinkedQueue<>();
    protected Queue<Id<Vehicle>> dwellingVehicles = new ConcurrentLinkedQueue<>();

    public Bay(TransitStopFacility transitStop){
        this.transitStop = transitStop;
        this.linkId = transitStop.getLinkId();
    }

    public TransitStopFacility getTransitStop() {
        return transitStop;
    }

    public Id<Link> getLinkId() {
        return linkId;
    }

    public boolean isDwelling(Id<Vehicle> vid) {
        return dwellingVehicles.contains(vid);
    }

    public abstract void addVehicle(Id<Vehicle> vid);

    public abstract void removeVehicle(Id<Vehicle> vid);
    
    public abstract boolean isQueuing(Id<Vehicle> vid);

    protected abstract boolean isBlocking();
}
