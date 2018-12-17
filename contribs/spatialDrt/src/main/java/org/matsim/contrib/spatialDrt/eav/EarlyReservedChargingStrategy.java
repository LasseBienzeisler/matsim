package org.matsim.contrib.spatialDrt.eav;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.spatialDrt.parkingStrategy.DefaultDrtOptimizer;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;


public class EarlyReservedChargingStrategy implements ChargingStrategy {
    @Inject
    ChargerManager chargerManager;

    LeastCostPathCalculator router;
    TravelTime travelTime;

    @Inject
    public EarlyReservedChargingStrategy(@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network, @Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime,
                                         @Named(DefaultDrtOptimizer.DRT_OPTIMIZER) TravelDisutility travelDisutility ){
        this.router = new FastAStarEuclideanFactory().createPathCalculator(network, travelDisutility, travelTime);
        this.travelTime =travelTime;
    }


    @Override
    public ChargerPathPair charging(Vehicle vehicle, double time) {
        ChargerPathPair bestLink = null;
        double bestTime = Double.MAX_VALUE;
        for (Charger charger : chargerManager.getChargers().values()){
            Link link = charger.getLink();
            VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink(), link, time, router, travelTime);
            double travelTime = path.getTravelTime();
            if (time + travelTime < charger.getEndTime()) {
                TimeChargerPair timeChargerPair = charger.calculateBestWaitTime(time + travelTime);
                if (travelTime + timeChargerPair.waitTime < bestTime) {
                    bestLink = new ChargerPathPair(charger, path);
                    bestTime = travelTime + timeChargerPair.waitTime;
                }
            }
        }
        return bestLink;
    }

    @Override
    public void leaving(Vehicle vehicle, double time) {

    }

    @Override
    public ChargingStrategy.Strategies getCurrentStrategy(Id<Vehicle> vehicleId) {
        return Strategies.EarlyReserved;
    }
}
