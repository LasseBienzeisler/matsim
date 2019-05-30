package org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DepotManagerDifferentDepots implements DepotManager{

    Map<Id<ActivityFacility>, Depot> depots = new HashMap<>();
    Map<Id<Vehicle>,Id<ActivityFacility>> vehicleLists = new HashMap<>(); //  vehicle id, depot id
    Network network;

    @Inject
    public DepotManagerDifferentDepots(Config config, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network, ActivityFacilities activityFacilities){
        AtodConfigGroup drtConfig = AtodConfigGroup.get(config);
        this.network = network;
        for (ActivityFacility activityFacility:activityFacilities.getFacilities().values()){
            for (ActivityOption activityOption: activityFacility.getActivityOptions().values()){
                    Depot depot = createDepot(activityFacility, activityOption);
                    depots.put(depot.getId(), depot);
            }
        }
//        new DepotReader(this,network).parse(drtConfig.getDepotFileUrl(config.getContext()));
    }

    public void addDepot(Depot depot) {
        depots.put(depot.getId(), depot);
    }

    public Map<Id<ActivityFacility>, Depot> getDepots(double time) {
        return depots.entrySet().stream().filter(depot -> depot.getValue().isOpen(time)).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }
    public Map<Id<ActivityFacility>, Depot> getDepots(Depot.DepotType depotType, double time) {
        return getDepots(time).entrySet().stream().filter(depot -> depot.getValue().getDepotType() == depotType).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    public boolean isVehicleInDepot(Vehicle vehicle) {
        return vehicleLists.containsKey(vehicle.getId());
    }

    public void registerVehicle(Id<Vehicle> vid, Id<ActivityFacility> did) {
        vehicleLists.put(vid,did);
    }

    public Depot getDepotOfVehicle(Vehicle vehicle) {
        if (vehicleLists.containsKey(vehicle.getId())){
            return depots.get(vehicleLists.get(vehicle.getId()));
        }
        return null;
    }

    public void vehicleLeavingDepot(Vehicle vehicle) {
        Depot currentDepot = getDepotOfVehicle(vehicle);
        if (currentDepot == null){
            return;
        }
        currentDepot.removeVehicle(vehicle.getId());
        vehicleLists.remove(vehicle.getId());
    }

    public Map<Id<ActivityFacility>, Depot> getDepots(double capacity, double time) {
        if (capacity < 10){
            return getDepots(Depot.DepotType.HDB, time);
        }else{
            return getDepots(Depot.DepotType.DEPOT, time);
        }
    }

    private Depot createDepot(ActivityFacility activityFacility, ActivityOption activityOption) {
        Id<ActivityFacility> id = activityFacility.getId();
        Link link = network.getLinks().get(activityFacility.getLinkId());
        //TODO: Create specific links for depots
        Depot.DepotType depotType = Depot.DepotType.valueOf(activityOption.getType());
        return new DepotImpl(id, link,activityOption, depotType);
    }
}
