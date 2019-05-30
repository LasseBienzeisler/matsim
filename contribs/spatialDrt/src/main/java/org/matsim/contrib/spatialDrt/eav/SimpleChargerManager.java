package org.matsim.contrib.spatialDrt.eav;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot.Depot;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleChargerManager implements ChargerManager {

    Map<Id<ActivityFacility>, Charger> chargers = new HashMap<>();
    Set<Id<Link>> occupiedLink = new HashSet<>();
    Network network;


    @Inject
    public SimpleChargerManager(Config config, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network, ActivityFacilities activityFacilities){
        AtodConfigGroup atodCfg = AtodConfigGroup.get(config);
        this.network = network;
        for (ActivityFacility activityFacility:activityFacilities.getFacilities().values()){
            for (ActivityOption activityOption: activityFacility.getActivityOptions().values()){
                if (activityOption.getType().equals(Depot.activityType)){
                    Charger charger = createCharger(activityFacility, activityOption);
                    chargers.put(charger.getId(), charger);
                }
            }
        }
        //new ChargerReader(this,network,config.qsim()).parse(atodCfg.getChargeFileURL(config.getContext()));
    }

    @Override
    public void addCharger(Charger charger) {
        chargers.put(charger.getId(), charger);
    }

    @Override
    public Map<Id<ActivityFacility>, Charger> getChargers() {
        return this.chargers;
    }

    @Override
    public Map<Id<ActivityFacility>, Charger> getChargers(double capacity) {
        if (capacity < 10){
            return chargers.entrySet().stream().filter(charger -> !charger.getKey().toString().startsWith("depot")).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        }else{
            return chargers.entrySet().stream().filter(charger -> !charger.getKey().toString().startsWith("HDB")).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        }
    }


    @Override
    public Map<Id<ActivityFacility>, Charger> getChargers(Charger.ChargerMode chargerMode) {
        return chargers.entrySet().stream().filter(charger -> charger.getValue().getChargerMode() == chargerMode).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    @Override
    public boolean isOccupied(Id<Link> linkId) {
        return occupiedLink.contains(linkId);
    }

    @Override
    public void modifyLanes(Id<Link> id, boolean value) {
        if (value) {
            occupiedLink.add(id);
        }else{
            occupiedLink.remove(id);
        }
    }


    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        for (Charger charger: chargers.values()){
            charger.clear();
            occupiedLink.clear();
        }
    }

    private Charger createCharger(ActivityFacility activityFacility, ActivityOption activityOption) {
        Id<ActivityFacility> id = activityFacility.getId();
        Link link = network.getLinks().get(activityFacility.getLinkId());
        //TODO: Create specific links for depots
        int capacity = (int) activityOption.getCapacity();
        boolean isBlocking = activityFacility.getAttributes().getAttribute("isBlocking").equals("true");
        switch (activityOption.getType()){
            case ("level2"):
                return new Level2Charger(id, link, capacity, activityOption, isBlocking);
            case ("fast"):
                return new FastCharger(id, link, capacity, activityOption, isBlocking);
            default:
                throw new RuntimeException("Wrong input charger type!");
        }
    }
}
