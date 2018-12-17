package org.matsim.contrib.spatialDrt.eav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.Drt;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.contrib.spatialDrt.vehicle.DynVehicleType;
import org.matsim.contrib.spatialDrt.vehicle.FleetImpl;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.vehicles.Vehicle;

import org.matsim.vehicles.VehicleType;

public class DischargingRate implements LinkEnterEventHandler, LinkLeaveEventHandler, IterationEndsListener{
    Map<Id<Vehicle>, Event> events = new HashMap<>();
    ArrayList<Id<Vehicle>> departure = new ArrayList<>();
    @Inject
    Network network;
    Fleet fleet;
    private static double MIN_METER;
    private static double MIN_ACCEPTED_METER;
    private static Map<Id<VehicleType>,DynVehicleType> vehicleTypes = new HashMap<>();



    @Inject
    public DischargingRate(EventsManager eventsManager, AtodConfigGroup atodConfigGroup, @Drt Fleet fleet){
        eventsManager.addHandler(this);
        MIN_METER = atodConfigGroup.getMinBattery() ;
        MIN_ACCEPTED_METER = atodConfigGroup.getMinRequestAccept() ;
        this.fleet = fleet;
        for (DynVehicleType vehicleType: ((FleetImpl)fleet).getVehicleTypes()){
            vehicleTypes.put(vehicleType.getId(),vehicleType);
        }
    }



    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (event.getVehicleId().toString().startsWith("drt")) {
            if (!events.containsKey(event.getVehicleId())) {
                events.put(event.getVehicleId(), event);
            } else {
                throw new RuntimeException("Cannot enter another link before leave!");
            }
        }
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (event.getVehicleId().toString().startsWith("drt")) {
            if (events.containsKey(event.getVehicleId()) || !departure.contains(event.getVehicleId())) {
                discharge(event.getVehicleId(), event.getLinkId());
                events.remove(event.getVehicleId());
                departure.add(event.getVehicleId());
            } else {
                throw new RuntimeException("Cannot leave without enter!");
            }
        }
    }

    private void discharge(Id<Vehicle> vehicleId, Id<Link> linkId) {
        VehicleImpl veh = (VehicleImpl) fleet.getVehicles().get(vehicleId);
        Link link = network.getLinks().get(linkId);
        veh.discharge(link.getLength() * vehicleTypes.get(veh.getVehicleType().getId()).getDischargingRate());
    }

    public static double calculateDischargeByDistance(double distance, Id<VehicleType> vehicle){
        return distance * vehicleTypes.get(vehicle).getDischargingRate();
    }


    public void clear(){
        events.clear();
        departure.clear();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        clear();
    }

    public static double getMinBattery(Id<VehicleType> vehicleTypeId) {
        return vehicleTypes.get(vehicleTypeId).getDischargingRate() * MIN_METER;
    }

    public static double getMinAccepted(Id<VehicleType> vehicleTypeId) {
        return vehicleTypes.get(vehicleTypeId).getDischargingRate() * MIN_ACCEPTED_METER;
    }
}
