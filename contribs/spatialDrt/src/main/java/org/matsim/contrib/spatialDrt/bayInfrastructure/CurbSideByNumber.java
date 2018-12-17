package org.matsim.contrib.spatialDrt.bayInfrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public class CurbSideByNumber extends Bay {
    private final int maximumNumber;

    public CurbSideByNumber(TransitStopFacility transitStop, int maximumNumber) {
        super(transitStop);
        this.maximumNumber = maximumNumber;
    }

    @Override
    public boolean isQueuing(Id<Vehicle> vid) {
        return dwellingVehicles.contains(vid) || queingVehicles.contains(vid);
    }

    @Override
    public boolean isBlocking() {
        return !dwellingVehicles.isEmpty() || !queingVehicles.isEmpty();
    }

    public void addVehicle(Id<Vehicle> vid){
        if (dwellingVehicles.size() > maximumNumber){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (dwellingVehicles.contains(vid) || queingVehicles.contains(vid)){
            return;
        }
        if (dwellingVehicles.size() + 1 > maximumNumber){
            if (!queingVehicles.contains(vid)) {
                queingVehicles.add(vid);
            }
        }else{
            dwellingVehicles.add(vid);
        }
    }

    public void removeVehicle(Id<Vehicle> vid){
        if(!dwellingVehicles.contains(vid) && !queingVehicles.contains(vid)){
            String dwell = new String();
            String vehs = new String();
            for (Id<Vehicle> dvehid : dwellingVehicles){
                dwell = dwell + ";" + dvehid.toString();
            }
            for (Id<Vehicle> vehid : queingVehicles){
                vehs = vehs + ";" + vehid.toString();
            }
            throw new RuntimeException("vid: " + vid.toString()  + ", dwellV: " + dwell + ", v: " + vehs);
        }
        queingVehicles.remove(vid);
        if (dwellingVehicles.remove(vid)) {
            Id<Vehicle> vehicleId = queingVehicles.peek();
            while (dwellingVehicles.size() + 1 <= maximumNumber && queingVehicles.size() > 0) {
                dwellingVehicles.add(vehicleId);
                queingVehicles.poll();
                vehicleId = queingVehicles.peek();
            }
        }
    }
}
