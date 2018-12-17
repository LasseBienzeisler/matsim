/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package org.matsim.contrib.spatialDrt.vehicle;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.core.scenario.ProjectionUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.utils.objectattributes.attributable.AttributesXmlReaderDelegate;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleCapacityImpl;
import org.matsim.vehicles.VehicleType;
import org.xml.sax.Attributes;
import org.matsim.contrib.dvrp.data.Vehicle;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author michalm
 */
public class VehicleReader extends MatsimXmlParser {
	private static final String VEHICLE = "vehicle";

	private static final double DEFAULT_CAPACITY = 1;
	private static final double DEFAULT_T_0 = 0;
	private static final double DEFAULT_T_1 = 24 * 60 * 60;
	private static final double DEFAULT_BOARDING = 1.0;
	private static final double DEFAULT_ALIGHTING = 1.0;

	private FleetImpl fleet;
	private Map<Id<Link>, ? extends Link> links;
	private Map<String, DynVehicleType> vehicleTypes = new HashMap<>();

	public VehicleReader(Network network, FleetImpl fleet, URL drtVehicleTypeFileURL) {
		this.fleet = fleet;
		links = network.getLinks();
		new DrtVehicleTypeReader(vehicleTypes).parse(drtVehicleTypeFileURL);
		fleet.setVehicleTypes(vehicleTypes.values());
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (VEHICLE.equals(name)) {
			fleet.addVehicle(createVehicle(atts));
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}

	private VehicleImpl createVehicle(Attributes atts) {
		Id<Vehicle> id = Id.create(atts.getValue("id"), Vehicle.class);
		Link startLink = links.get(Id.createLinkId(atts.getValue("start_link")));
		double t0 = ReaderUtils.getDouble(atts, "t_0", DEFAULT_T_0);
		double t1 = ReaderUtils.getDouble(atts, "t_1", DEFAULT_T_1);
		String mode = ReaderUtils.getString(atts, "mode",null);
		String type = ReaderUtils.getString(atts, "type", null);
		DynVehicleType vehicleType = vehicleTypes.get(type);
		int capacity = vehicleType.getSeats();
		VehicleImpl vehicle =  createVehicle(id, startLink, capacity, t0, t1, vehicleType);
		vehicle.getAttributes().putAttribute("mode", mode);
		return vehicle;
	}

	protected VehicleImpl createVehicle(Id<Vehicle> id, Link startLink, int capacity, double t0, double t1,
										DynVehicleType vehicleType) {
		return new VehicleImpl(id, startLink, capacity, t0, t1,vehicleType);
	}
}
