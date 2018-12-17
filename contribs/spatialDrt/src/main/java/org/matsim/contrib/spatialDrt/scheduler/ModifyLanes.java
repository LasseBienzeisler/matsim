package org.matsim.contrib.spatialDrt.scheduler;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.network.NetworkChangeEvent;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class ModifyLanes {
    @Inject
    private Network network;
    @Inject
    private QSim qSim;
    private final Set<Id<Link>> lastChangeNoZero = new HashSet<>();
    /*private int reductions = 0;
    private int recoveries = 0;
    private final Map<Id<Link>, Double> lastChangeNoZeroMap = new HashMap<>();
    int timeReduce = 0;*/
//    public void modifyLanes(Id<Link> linkId, double time, double change){
//        /*if(time>99000)
//            for(int i=0; i<10000; i++) {
//                System.out.println(timeReduce);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }*/
//        if(change!=0) {
//            if(lastChangeNoZero.contains(linkId))
//                return;
//            lastChangeNoZero.add(linkId);
//           // lastChangeNoZeroMap.put(linkId, time);
//        }
//        else {
//            if(!lastChangeNoZero.contains(linkId))
//                return;
//            lastChangeNoZero.remove(linkId);
//            /*Double sTime = lastChangeNoZeroMap.remove(linkId);
//            timeReduce+=time-sTime;*/
//        }
//        /*if(change==0) {
//            recoveries++;
//            System.out.println("+ "+recoveries);
//        }
//        else {
//            reductions++;
//            System.out.println("- "+reductions);
//        }*/
//        Link currentLink = network.getLinks().get(linkId);
//        double numOfLanes = currentLink.getNumberOfLanes();
//        if (numOfLanes == 1){
//            change = 0.5 * change;
//        }
//        NetworkChangeEvent event = new NetworkChangeEvent(time + Math.random());
//        event.addLink(currentLink);
//        NetworkChangeEvent.ChangeValue capacityChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, (numOfLanes + change)/numOfLanes * currentLink.getCapacity() / 3600.0);
//        NetworkChangeEvent.ChangeValue lanesChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, numOfLanes + change);
//        event.setLanesChange(lanesChange);
//        event.setFlowCapacityChange(capacityChange);
//        qSim.addNetworkChangeEvent(event);
//    }

    public void modifyLanes(Id<Link> linkId, double time, double change){
        if (linkId.toString().equals("mpla_53") && time != 36000 && change == 0){
            System.out.println();
        }
        Link currentLink = network.getLinks().get(linkId);
        double numOfLanes = currentLink.getNumberOfLanes();
        if ((change == 0 && currentLink.getCapacity() != currentLink.getCapacity(time)) || (change != 0 && currentLink.getCapacity() == currentLink.getCapacity(time)) ) {
            if (numOfLanes == 1) {
                change = 0.5 * change;
            }
            NetworkChangeEvent event = new NetworkChangeEvent(time + MatsimRandom.getRandom().nextDouble());
            event.addLink(currentLink);
            NetworkChangeEvent.ChangeValue capacityChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, (numOfLanes + change) / numOfLanes * currentLink.getCapacity() / 3600.0);
            NetworkChangeEvent.ChangeValue lanesChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, numOfLanes + change);
            event.setLanesChange(lanesChange);
            event.setFlowCapacityChange(capacityChange);
            qSim.addNetworkChangeEvent(event);
        }
    }
}
