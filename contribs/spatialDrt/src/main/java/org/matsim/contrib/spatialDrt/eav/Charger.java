package org.matsim.contrib.spatialDrt.eav;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.OpeningTime;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class Charger implements ActivityFacility {
    private Id<ActivityFacility> id;
    private Link link;
    private int capacity;

    ActivityOption activityOption;
    private boolean isBlocking;
    private ArrayList<VehicleImpl> chargingVehicles = new ArrayList<>();
    private Queue<VehicleImpl> waitingVehicles = new ConcurrentLinkedQueue<>();
    private Queue<Id<Vehicle>> waitingForOpenVehicles = new ConcurrentLinkedQueue<>();
    private PriorityBlockingQueue<Double> chargerAvailableTime = new PriorityBlockingQueue<>();
    private boolean firstStart = true;


    public Charger(Id<ActivityFacility> id, Link link, ActivityOption activityOption,int capacity, boolean isBlocking){
        this.id = id;
        this.link = link;
        this.capacity = capacity;
        this.activityOption = activityOption;
        this.isBlocking = isBlocking;
    }

    public abstract double getChargingRate();

    public boolean isCharging(Vehicle vehicle) {
        return chargingVehicles.contains(vehicle);
    }

    public enum ChargerMode{
        Level2,
    fast;
}



    public abstract double getChargingTime(VehicleImpl vehicle);

    public abstract double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery);


    public boolean isFull() {
        return waitingVehicles.size() > 0;
    }

    public boolean isOpen(double now){
        for (OpeningTime openingTime: activityOption.getOpeningTimes()){
            if (now <= openingTime.getEndTime()){
                if (now >= openingTime.getStartTime()){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }


    public boolean isQueue(Vehicle veh) {
        return waitingVehicles.contains(veh);
    }


    public void removeVehicle(VehicleImpl vehicle, double now) {
        if(!chargingVehicles.contains(vehicle) && !waitingVehicles.contains(vehicle)){
//            String dwell = new String();
//            String vehs = new String();
//            for (Id<org.matsim.vehicles.Vehicle> dvehid : chargingVehicles){
//                if (dvehid == null){
//                    dwell = dwell + ";null";
//                }
//                dwell = dwell + ";" + dvehid.toString();
//            }
//            for (Id<org.matsim.vehicles.Vehicle> vehid : vehicles){
//                if (vehid == null){
//                    vehs = vehs + ";null";
//                }
//                vehs = vehs + ";" + vehid.toString();
//            }
            //throw new RuntimeException("vid: " + vid.toString() + ", transitStop: " + transitStop.getId().toString() + ", dwellV: " + dwell + ", v: " + vehs);
            throw new RuntimeException("Vehicle is neither charging nor waiting! Cannot be removed!");
        }
        waitingVehicles.remove(vehicle);
        if (chargingVehicles.remove(vehicle) && isFull()){
            VehicleImpl veh = waitingVehicles.poll();
            chargingVehicles.add(veh);
        }else{
            chargerAvailableTime.poll();
        }
    }

    public boolean isEmpty(){
        return isBlocking?(chargingVehicles.isEmpty() && waitingForOpenVehicles.isEmpty()): (waitingForOpenVehicles.isEmpty() && waitingVehicles.isEmpty());
    }


    public void clear() {
        waitingVehicles.clear();
        chargingVehicles.clear();
        chargerAvailableTime.clear();
        waitingForOpenVehicles.clear();
    }


    public Link getLink() {
        return link;
    }


    public double getCapacity() {
        return capacity;
    }


    public TimeChargerPair calculateBestWaitTime( double arrivalTime) {
        TimeChargerPair best = new TimeChargerPair(Double.MAX_VALUE, this);
        if (checkChargerAvailability(arrivalTime)) {
            if (chargerAvailableTime.size() < capacity) {
                best.waitTime = Double.max(0, getStartTime(arrivalTime) - arrivalTime);
            } else {
                best.waitTime = Double.max(Double.max(0, getStartTime(arrivalTime) - arrivalTime), chargerAvailableTime.peek() - arrivalTime);
            }
        }
        return best;
    }

    public boolean checkChargerAvailability(double arrivalTime){
        return chargerAvailableTime.peek() == null?true:chargerAvailableTime.peek() <= getCloseTime();
    }


    public abstract ChargerMode getChargerMode();


    public void addVehicle(VehicleImpl vehicle, double now) {
        if (chargingVehicles.size() > capacity){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (chargingVehicles.contains(vehicle)){
            return;
        }
        if (now == getStartTime(now) && waitingForOpenVehicles.contains(vehicle.getId())){
            waitingForOpenVehicles.remove(vehicle.getId());
            if (firstStart){
                chargerAvailableTime.clear();
                firstStart = false;
            }
        }
        if (now < getStartTime(now) ){
            if (!waitingForOpenVehicles.contains(vehicle.getId())) {
                if (chargerAvailableTime.size() == capacity) {
                    double time = chargerAvailableTime.poll() + getChargingTime(vehicle);
                    chargerAvailableTime.add(time);
                } else {
                    chargerAvailableTime.add(getStartTime(now) + getChargingTime(vehicle));
                }
                waitingForOpenVehicles.add(vehicle.getId());
            }
            return;
        }
        if (chargingVehicles.size() == capacity){
            if (!waitingVehicles.contains(vehicle)) {
                waitingVehicles.add(vehicle);
                double time = chargerAvailableTime.poll() + getChargingTime(vehicle);
                chargerAvailableTime.add(time);
            }
        }else{
            chargingVehicles.add(vehicle);
            chargerAvailableTime.add(now + getChargingTime(vehicle));
        }
    }


    public Id<ActivityFacility> getId() {
        return id;
    }

    public double getStartTime(double now) {
        for (OpeningTime openingTime: activityOption.getOpeningTimes()){
            if (now <= openingTime.getEndTime()){
                return openingTime.getStartTime();
            }
        }
        return Double.MAX_VALUE;
    }

    public double getEndTime(double now) {
        for (OpeningTime openingTime: activityOption.getOpeningTimes()){
            if (now <= openingTime.getEndTime()){
                return openingTime.getEndTime();
            }
        }
        return Double.MAX_VALUE;
    }

    public double getCloseTime(){
        return activityOption.getOpeningTimes().last().getEndTime();
    }

    @Override
    public Map<String, ActivityOption> getActivityOptions() {
        return null;
    }

    @Override
    public void addActivityOption(ActivityOption option) {

    }

    @Override
    public void setCoord(Coord coord) {

    }

    @Override
    public Id<Link> getLinkId() {
        return null;
    }

    @Override
    public Coord getCoord() {
        return null;
    }

    @Override
    public Map<String, Object> getCustomAttributes() {
        return null;
    }

    @Override
    public Attributes getAttributes() {
        return null;
    }
}


class TimeChargerPair{
    double waitTime;
    Charger charger;

    TimeChargerPair(double waitTime, Charger charger){
        this.waitTime = waitTime;
        this.charger = charger;
    }
}

class TimeVehiclePair{
    double time;
    VehicleImpl vehicle;

    TimeVehiclePair(double time, VehicleImpl vehicle){
        this.time = time;
        this.vehicle = vehicle;
    }

}

//class VehiclesByStation{
//    double endTime = 0;
//    Id<Vehicle> vid;
//
//    public VehiclesByStation(double endTime, Id<Vehicle> vid){
//        this.endTime =endTime;
//        this.vid = vid;
//    }
//}
