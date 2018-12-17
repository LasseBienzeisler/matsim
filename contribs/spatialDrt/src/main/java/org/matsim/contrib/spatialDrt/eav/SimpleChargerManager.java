package org.matsim.contrib.spatialDrt.eav;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.spatialDrt.run.AtodConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.controler.events.IterationStartsEvent;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleChargerManager implements ChargerManager {

    Map<Id<Charger>, Charger> chargers = new HashMap<>();
    Set<Id<Link>> occupiedLink = new HashSet<>();


    @Inject
    public SimpleChargerManager(Config config, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network){
        AtodConfigGroup atodCfg = AtodConfigGroup.get(config);
        new ChargerReader(this,network,config.qsim()).parse(atodCfg.getChargeFileURL(config.getContext()));
    }

    @Override
    public void addCharger(Charger charger) {
        chargers.put(charger.getId(), charger);
    }

    @Override
    public Map<Id<Charger>, Charger> getChargers() {
        return this.chargers;
    }

    @Override
    public Map<Id<Charger>, Charger> getChargers(Charger.ChargerMode chargerMode) {
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
        }
    }
}
