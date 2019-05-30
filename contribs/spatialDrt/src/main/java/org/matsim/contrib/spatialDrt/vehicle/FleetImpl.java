/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;


/**
 * @author michalm
 */
public class FleetImpl implements Fleet {
	private final Map<Id<Vehicle>, Vehicle> vehicles = new LinkedHashMap<>();
	private Collection<DynVehicleType> vehicleTypes;

	@Override
	public Map<Id<Vehicle>, ? extends Vehicle> getVehicles() {
		return vehicles;
	}


	public Map<Id<Vehicle>, ? extends Vehicle> getVehicles(String mode) {
		return vehicles.entrySet().stream().filter(vehicle -> (vehicle.getValue()).getAttributes().getAttribute("mode").equals(mode)&& !((VehicleImpl)vehicle.getValue()).getStatus() && ((VehicleImpl)vehicle.getValue()).getBattery() > 0).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
	}

	public void addVehicle(VehicleImpl vehicle) {
		vehicles.put(vehicle.getId(), vehicle);
	}

	public void resetSchedules() {
		for (Vehicle v : vehicles.values()) {
			v.resetSchedule();
		}
	}

	public Collection<DynVehicleType> getVehicleTypes(){
		return vehicleTypes;
	}

	public void setVehicleTypes(Collection<DynVehicleType> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
	}
}
