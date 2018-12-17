package org.matsim.contrib.spatialDrt.eav;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;

public class ChargerPathPair {
    public Charger charger = null;
    public VrpPathWithTravelData path = null;

    public ChargerPathPair(Charger charger, VrpPathWithTravelData path) {
        this.charger = charger;
        this.path = path;
    }
}
