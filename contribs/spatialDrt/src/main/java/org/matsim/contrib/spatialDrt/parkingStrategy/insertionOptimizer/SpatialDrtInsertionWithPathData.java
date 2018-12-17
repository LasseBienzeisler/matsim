package org.matsim.contrib.spatialDrt.parkingStrategy.insertionOptimizer;

import org.matsim.contrib.drt.optimizer.insertion.InsertionWithDetourTimes;
import org.matsim.contrib.drt.optimizer.insertion.InsertionWithPathData;
import org.matsim.contrib.dvrp.path.OneToManyPathSearch;

public class SpatialDrtInsertionWithPathData implements InsertionWithDetourTimes {

    private final int pickupIdx;
    private final int dropoffIdx;
    private final OneToManyPathSearch.PathData pathToPickup;
    private final OneToManyPathSearch.PathData pathFromPickup;
    private final OneToManyPathSearch.PathData pathToDropoff;// null if dropoff inserted directly after pickup
    private final OneToManyPathSearch.PathData pathFromDropoff;// null if dropoff inserted at the end

    SpatialDrtInsertionWithPathData(int pickupIdx, int dropoffIdx, OneToManyPathSearch.PathData pathToPickup, OneToManyPathSearch.PathData pathFromPickup,
                          OneToManyPathSearch.PathData pathToDropoff, OneToManyPathSearch.PathData pathFromDropoff) {
        this.pickupIdx = pickupIdx;
        this.dropoffIdx = dropoffIdx;
        this.pathToPickup = pathToPickup;
        this.pathFromPickup = pathFromPickup;
        this.pathToDropoff = pathToDropoff;
        this.pathFromDropoff = pathFromDropoff;
    }

    @Override
    public int getPickupIdx() {
        return pickupIdx;
    }

    @Override
    public int getDropoffIdx() {
        return dropoffIdx;
    }

    @Override
    public double getTimeToPickup() {
        return pathToPickup.getTravelTime();
    }

    @Override
    public double getTimeFromPickup() {
        return pathFromPickup.getTravelTime();
    }

    @Override
    public double getTimeToDropoff() {
        return pathToDropoff.getTravelTime();// NullPointerException if dropoff inserted directly after pickup
    }

    @Override
    public double getTimeFromDropoff() {
        return pathFromDropoff.getTravelTime();// NullPointerException if dropoff inserted at the end
    }

    public OneToManyPathSearch.PathData getPathToPickup() {
        return pathToPickup;
    }

    public OneToManyPathSearch.PathData getPathFromPickup() {
        return pathFromPickup;
    }

    public OneToManyPathSearch.PathData getPathToDropoff() {
        return pathToDropoff;
    }

    public OneToManyPathSearch.PathData getPathFromDropoff() {
        return pathFromDropoff;
    }

    @Override
    public String toString() {
        return "Insertion: pickupIdx=" + pickupIdx + ", dropoffIdx=" + dropoffIdx;
    }
}
