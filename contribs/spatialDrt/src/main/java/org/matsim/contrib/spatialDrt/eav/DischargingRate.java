package org.matsim.contrib.spatialDrt.eav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.Drt;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.path.DivertedVrpPath;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.contrib.spatialDrt.schedule.DrtStopTask;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.contrib.spatialDrt.vehicle.DynVehicleType;
import org.matsim.contrib.spatialDrt.vehicle.FleetImpl;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.vehicles.Vehicle;

import org.matsim.vehicles.VehicleType;

public class DischargingRate implements LinkLeaveEventHandler{

    EventsManager eventsManager;
    @Inject@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
    Network network;
    Fleet fleet;
    private static double MIN_METER;
    private static double MIN_ACCEPTED_METER;
    private static Map<Id<VehicleType>,DynVehicleType> vehicleTypes = new HashMap<>();
    public static boolean isEAV = false;



    @Inject
    public DischargingRate(EventsManager eventsManager, AtodConfigGroup atodConfigGroup, @Drt Fleet fleet){
        this.eventsManager = eventsManager;
        eventsManager.addHandler(this);
        MIN_METER = atodConfigGroup.getMinBattery() ;
        MIN_ACCEPTED_METER = atodConfigGroup.getMinRequestAccept() ;
        this.fleet = fleet;
        for (DynVehicleType vehicleType: ((FleetImpl)fleet).getVehicleTypes()){
            vehicleTypes.put(vehicleType.getId(),vehicleType);
        }
        isEAV = true;
    }



    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (event.getVehicleId().toString().startsWith("drt")) {
            discharge(event.getVehicleId(), event.getLinkId(), event.getTime());
        }
    }

    private void discharge(Id<Vehicle> vehicleId, Id<Link> linkId, double time) {
        VehicleImpl veh = (VehicleImpl) fleet.getVehicles().get(vehicleId);
        Link link = network.getLinks().get(linkId);
        if (!veh.discharge(link.getLength() * vehicleTypes.get(veh.getVehicleType().getId()).getDischargingRate())){
            for (int i = veh.getSchedule().getCurrentTask().getTaskIdx() + 1; i < veh.getSchedule().getTasks().size(); ){
                veh.getSchedule().removeTask(veh.getSchedule().getTasks().get(i));
            }
            switch (((DrtTask)veh.getSchedule().getCurrentTask()).getDrtTaskType()){
                case DRIVE:
                    DrtDriveTask driveTask = (DrtDriveTask) veh.getSchedule().getCurrentTask();
                    Link newLink = ((OnlineDriveTaskTracker) driveTask.getTaskTracker()).getDiversionPoint().link;
                    VrpPathWithTravelData newPath = VrpPaths.createZeroLengthPath(newLink, ((OnlineDriveTaskTracker) driveTask.getTaskTracker()).getDiversionPoint().time);
                    ((OnlineDriveTaskTracker) driveTask.getTaskTracker()).divertPath(newPath);
                    veh.getSchedule().addTask(new DrtStayTask(driveTask.getEndTime(), driveTask.getEndTime() , newLink));
                    return;
                case STAY:
                    DrtStayTask stayTask = (DrtStayTask) veh.getSchedule().getCurrentTask();
                    stayTask.setEndTime(stayTask.getBeginTime());
                    return;
                case STOP:
                    DrtStopTask stopTask = (DrtStopTask) veh.getSchedule().getCurrentTask();
                    stopTask.setEndTime(time);
                    veh.getSchedule().addTask(new DrtStayTask(time,time, stopTask.getLink()));
                    return;
                default:
                    throw new RuntimeException();
            }
        }
    }

    public static double calculateDischargeByDistance(double distance, Id<VehicleType> vehicle){
        return isEAV?distance * vehicleTypes.get(vehicle).getDischargingRate():0.0;
    }


    public static double getMinBattery(Id<VehicleType> vehicleTypeId) {
        return isEAV?vehicleTypes.get(vehicleTypeId).getDischargingRate() * MIN_METER : 0.0;
    }

    public static double getMinAccepted(Id<VehicleType> vehicleTypeId) {
        return isEAV?vehicleTypes.get(vehicleTypeId).getDischargingRate() * MIN_ACCEPTED_METER :0.0;
    }
}
