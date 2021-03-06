/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package org.matsim.contrib.ev.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.charging.ChargingLogic;
import org.matsim.core.api.experimental.events.EventsManager;

import com.google.common.collect.ImmutableMap;

/**
 * @author michalm
 */
public interface ChargingInfrastructure {
	ImmutableMap<Id<Charger>, Charger> getChargers();

	//FIXME remove (after switching to the Qsim scope)
	default void initChargingLogics(ChargingLogic.Factory logicFactory, EventsManager eventsManager) {
		for (Charger c : getChargers().values()) {
			ChargingLogic logic = logicFactory.create(c);
			logic.initEventsHandling(eventsManager);
			c.setLogic(logic);
		}
	}
}
