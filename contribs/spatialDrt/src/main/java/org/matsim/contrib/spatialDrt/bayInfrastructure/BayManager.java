package org.matsim.contrib.spatialDrt.bayInfrastructure;

import org.apache.log4j.Logger;
import org.matsim.contrib.spatialDrt.parkingStrategy.insertionOptimizer.DefaultUnplannedRequestInserter;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class BayManager implements VehicleDepartsAtFacilityEventHandler, IterationStartsListener {
    Map<Id<TransitStopFacility>, Bay> bays = new HashMap<>();
    Map<Id<Link>, Id<TransitStopFacility>> baysByStops = new HashMap<>();
    Collection<TransitStopFacility> transitStopFacilities;
    Network network;
    AtodConfigGroup atodConfigGroup;
    private static final Logger log = Logger.getLogger(BayManager.class);
    Set<Id<Link>> linksToBeUpdated = new HashSet<>();


    @Inject
    public BayManager(Scenario scenario, EventsManager eventsManager){
        transitStopFacilities = scenario.getTransitSchedule().getFacilities().values();
        this.network = scenario.getNetwork();
        this.atodConfigGroup = AtodConfigGroup.get(scenario.getConfig());
        initiate();
        eventsManager.addHandler(this);
    }

    private void initiate(){
        for (TransitStopFacility stop: transitStopFacilities){
            Bay bay;
            switch(atodConfigGroup.getBayMode()) {
                case curbSide:
                    bay = new CurbSide(stop, network.getLinks().get(stop.getLinkId()).getLength(), atodConfigGroup.getMinBaySize());
                    break;
                case externalBay:
                    bay = new ExternalBay(stop, network.getLinks().get(stop.getLinkId()).getLength(), atodConfigGroup.getMinBaySize());
                    break;
                case infinity:
                    bay = new ExternalBay(stop, Double.POSITIVE_INFINITY, atodConfigGroup.getMinBaySize());
                    break;
                case singleVehicle:
                    bay = new CurbSideByNumber(stop,1 );
                    break;
                default:
                    throw new RuntimeException("No such bay mode!");
            }
            bays.put(stop.getId(), bay);
            baysByStops.put(stop.getLinkId(),stop.getId());
        }
    }

    public Bay getBayByLinkId(Id<Link> linkId){
        if (!baysByStops.containsKey(linkId)){
            TransitStopFacility stop= (new TransitScheduleFactoryImpl()).createTransitStopFacility(Id.create(linkId.toString() + "_DRT", TransitStopFacility.class),
                    network.getLinks().get(linkId).getCoord(),false);
            stop.setLinkId(linkId);
            Bay bay;
            switch (atodConfigGroup.getDoor2DoorStop()){
                case infinity:
                    bay = new ExternalBay(stop, Double.POSITIVE_INFINITY, atodConfigGroup.getMinBaySize());
                    break;
                case linkLength:
                    bay = new ExternalBay(stop, network.getLinks().get(linkId).getLength(), atodConfigGroup.getMinBaySize());
                    break;
                case zero:
                    bay = new CurbSide(stop, network.getLinks().get(linkId).getLength(), atodConfigGroup.getMinBaySize());
                    break;
                default:
                    throw new RuntimeException("No such door-to-door stop strategy!");
            }
            bays.put(stop.getId(), bay);
            baysByStops.put(linkId, stop.getId());
        }
        return bays.get(baysByStops.get(linkId));
    }

    public Bay getBayByFacilityId(Id<TransitStopFacility> stopFacilityId){
        return bays.get(stopFacilityId);
    }

    public Id<TransitStopFacility> getStopIdByLinkId(Id<Link> linkId){
        return baysByStops.get(linkId);
    }

//    @Override
//    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
//        Bay bay = bays.get(event.getFacilityId());
//        bay.addVehicle(event.getVehicleId());
//    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        Bay bay = bays.get(event.getFacilityId());
        bay.removeVehicle(event.getVehicleId());
        int t=0;
        while (bay.isDwelling(event.getVehicleId())){
            log.warn("Vehicle to be removed is still in the bay! id:"+event.getVehicleId());
            bay.removeVehicle(event.getVehicleId());
            if(t++>100)
                break;
        }
        if (!bay.isBlocking()){
            linksToBeUpdated.add(bay.getLinkId());
        }
    }

    public Set<Id<Link>> getLinksToBeUpdated() {
        return linksToBeUpdated;
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        bays.clear();
        baysByStops.clear();
        initiate();
    }

    public void clearLinksToBeUpdated() {
        linksToBeUpdated.clear();
    }
}
