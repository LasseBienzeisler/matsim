package org.matsim.contrib.spatialDrt.bayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.spatialDrt.scheduler.ModifyLanes;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

public abstract class BaySize extends Bay {
    private double dwellLength = 0;
    private final double capacity;

    public BaySize(TransitStopFacility transitStop, double linkLength, double minBaySize){
        super(transitStop);
        if (transitStop.getAttributes().getAttribute("capacity") == null){
            this.capacity = Double.POSITIVE_INFINITY;
        }else if ((double) transitStop.getAttributes().getAttribute("capacity") == 0.0) {
            this.capacity = Double.max(linkLength, minBaySize);
        }else{
            this.capacity = (double) transitStop.getAttributes().getAttribute("capacity");
        }
    }

    public double getCapacity() {
        return capacity;
    }


    public void addVehicle(Id<Vehicle> vid){
        if (dwellLength > capacity){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (dwellingVehicles.contains(vid) || queingVehicles.contains(vid)){
            return;
        }
        double vehicleLength = VehicleLength.getLength(vid);
        if (dwellLength +  vehicleLength >= capacity){
            if (!queingVehicles.contains(vid)) {
                queingVehicles.add(vid);
            }
        }else{
            dwellLength = dwellLength + vehicleLength;
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
            throw new RuntimeException("vid: " + vid.toString() + ", dwellV: " + dwell + ", v: " + vehs);
        }
        queingVehicles.remove(vid);
        if (dwellingVehicles.remove(vid)) {
            double vehicleLength = VehicleLength.getLength(vid);
            dwellLength = dwellLength - vehicleLength;
            Id<Vehicle> vehicleId = queingVehicles.peek();
            double vehLength = vehicleId==null?Double.POSITIVE_INFINITY:VehicleLength.getLength(vehicleId);
            while (dwellLength + vehLength <= capacity && queingVehicles.size() > 0) {
                dwellLength = dwellLength + vehLength;
                dwellingVehicles.add(vehicleId);
                queingVehicles.poll();
                vehicleId = queingVehicles.peek();
                vehLength = vehicleId==null?Double.POSITIVE_INFINITY:VehicleLength.getLength(vehicleId);
            }
        }

    }

}
