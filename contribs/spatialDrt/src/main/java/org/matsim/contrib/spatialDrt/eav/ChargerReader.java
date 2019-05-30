package org.matsim.contrib.spatialDrt.eav;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.facilities.OpeningTimeImpl;
import org.xml.sax.Attributes;

import java.util.Stack;

public class ChargerReader extends MatsimXmlParser {

    private static final String CHARGER = "charger";
    private static final int DEFAULT_CAPACITY = 0;
    private final ChargerManager chargerManager;
    private final Network network;
    private final QSimConfigGroup qsimConfig;


    public ChargerReader(ChargerManager chargerManager, Network network, QSimConfigGroup qsimConfig) {
        this.chargerManager = chargerManager;
        this.network = network;
        this.qsimConfig = qsimConfig;
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if (CHARGER.equals(name)) {
            Charger charger = createCharger(atts);
            chargerManager.addCharger(charger);
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
    }

    private Charger createCharger(Attributes atts) {
        Id<ActivityFacility> id = Id.create(atts.getValue("id"), ActivityFacility.class);
        Link link = network.getLinks().get(Id.createLinkId(atts.getValue("link")));
        //TODO: Create specific links for depots
        int capacity = ReaderUtils.getInt(atts, "capacity", DEFAULT_CAPACITY);
        double startTime = ReaderUtils.getDouble(atts, "start_time",qsimConfig.getStartTime());
        double endTime = ReaderUtils.getDouble(atts, "end_time", qsimConfig.getEndTime());
        boolean isBlocking = !atts.getValue("is_blocking").equals("false");

        switch (atts.getValue("mode")){
            case ("level2"):
                ActivityOption activityOption = new ActivityOptionImpl(Level2Charger.activityType);
                activityOption.addOpeningTime(new OpeningTimeImpl(startTime, endTime));
                return new Level2Charger(id, link, capacity, activityOption, isBlocking);
            case ("fast"):
                ActivityOption activityOption2 = new ActivityOptionImpl(FastCharger.activityType);
                activityOption2.addOpeningTime(new OpeningTimeImpl(startTime, endTime));
                return new FastCharger(id, link, capacity,activityOption2, isBlocking);
            default:
                throw new RuntimeException("Wrong input charger type!");
        }
    }

}
