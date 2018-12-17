package org.matsim.contrib.spatialDrt.eav;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.listener.IterationStartsListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface ChargerManager extends IterationStartsListener {
    void addCharger(Charger charger);

    Map<Id<Charger>, Charger> getChargers();
    Map<Id<Charger>, Charger> getChargers(Charger.ChargerMode chargerMode);

    public boolean isOccupied(Id<Link> linkId);

    void modifyLanes(Id<Link> id, boolean value);
}
