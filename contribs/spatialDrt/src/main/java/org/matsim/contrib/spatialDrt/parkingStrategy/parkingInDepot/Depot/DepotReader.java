package org.matsim.contrib.spatialDrt.parkingStrategy.parkingInDepot.Depot;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.AbstractRoutingNetworkLink;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.facilities.OpeningTimeImpl;
import org.xml.sax.Attributes;
import sun.nio.ch.Net;

import java.util.Stack;

public class DepotReader extends MatsimXmlParser {

    private static final String DEPOT = "depot";
    private static final double DEFAULT_CAPACITY = 0;
    private static final double DEFAULT_START_TIME = 0;
    private static final double DEFAULT_END_TIME = Double.MAX_VALUE;
    private final DepotManager depotManager;
    private final Network network;


    public DepotReader(DepotManager depotManager, Network network) {
        this.depotManager = depotManager;
        this.network = network;
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if (DEPOT.equals(name)) {
            Depot depot = createDepot(atts);
            depotManager.addDepot(depot);
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
    }

    private Depot createDepot(Attributes atts) {
        Id<ActivityFacility> id = Id.create(atts.getValue("id"), ActivityFacility.class);
        Link link = network.getLinks().get(Id.createLinkId(atts.getValue("link")));
        //TODO: Create specific links for depots
        double capacity = ReaderUtils.getDouble(atts, "capacity", DEFAULT_CAPACITY);
        double startTime = ReaderUtils.getDouble(atts, "start_time",DEFAULT_START_TIME);
        double endTime = ReaderUtils.getDouble(atts, "end_time",DEFAULT_END_TIME);
        Depot.DepotType depotType = Depot.DepotType.valueOf(atts.getValue("type"));
        ActivityOption activityOption = new ActivityOptionImpl(depotType.toString());
        activityOption.setCapacity(capacity);
        activityOption.addOpeningTime(new OpeningTimeImpl(startTime, endTime));
        return new DepotImpl(id, link,activityOption, depotType);
    }

}
