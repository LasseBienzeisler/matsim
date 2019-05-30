package org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.facilities.*;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DepotImpl implements Depot{
    private Id<ActivityFacility> id;
    private Link link;
    private Coord coord;
    private ArrayList<Id<Vehicle>> vehicles = new ArrayList<>();
    ActivityOption activityOption;
    private Attributes attributes = new Attributes();
    private DepotType depotType;

    public DepotImpl(Id<ActivityFacility> depotId, Link link, ActivityOption activityOption, DepotType depotType){
        this.id = depotId;
        this.link = link;
        this.activityOption = activityOption;
        this.depotType = depotType;
    }
    public Link getLink() {
        return link;
    }

    public double getCapacity() {
        return activityOption.getCapacity();
    }

    public void addVehicle(Id<Vehicle> vid){
        if (vehicles.contains(vid)){
            return;
        }
        if (vehicles.size() ==  activityOption.getCapacity()){
            throw new RuntimeException("The depot " + link.getId() + " is full!!");
        }
        vehicles.add(vid);
    }

    public void removeVehicle(Id<Vehicle> vid){
        vehicles.remove(vid);
    }

    public double getNumOfVehicles() {
        return vehicles.size();
    }

    public Id<ActivityFacility> getId() {
        return id;
    }


    @Override
    public DepotType getDepotType() {
        return depotType;
    }

    @Override
    public boolean isOpen(double time) {
        for (OpeningTime openingTime: activityOption.getOpeningTimes()){
            if (time <= openingTime.getEndTime()){
                if (time >= openingTime.getStartTime()){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Map<String, ActivityOption> getActivityOptions() {
        return Collections.singletonMap(activityOption.getType(), activityOption);
    }

    @Override
    public void addActivityOption(ActivityOption option) {
    }

    @Override
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    @Override
    public Id<Link> getLinkId() {
        return link.getId();
    }

    @Override
    public Coord getCoord() {
        return coord;
    }

    @Override
    public Map<String, Object> getCustomAttributes() {
        return attributes.getAsMap();
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }
}
