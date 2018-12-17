/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.spatialDrt.run;

import org.matsim.contrib.spatialDrt.parkingStrategy.ParkingStrategy;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Map;

public class AtodConfigGroup extends ReflectiveConfigGroup {

	public static final String GROUP_NAME = "atod";

	public AtodConfigGroup() {
		super(GROUP_NAME);
	}

	public enum BayMode {
		singleVehicle,
		externalBay,
		curbSide,
		infinity;
	}

	public enum Door2DoorStop{
		infinity, linkLength, zero
	}

	@SuppressWarnings("deprecation")
	public static AtodConfigGroup get(Config config) {
		return (AtodConfigGroup)config.getModule(GROUP_NAME);
	}

	public static final String PARKING_STRATEGY = "parkingStrategy";
	static final String PARKING = "Paring strategies, alwaysRoaming, parkingOntheRoad, parkingInDepot, MixedParking";

	public static final String DEPOT_FILE = "depotFile";
	static final String DEPOT_FILE_EXP = "An XML file specifying the location of depots. The file format according to depot.dtd";


	public static final String DOOR_2_DOOR_STOP = "door2DoorStop";
	static final String DOOR_2_DOOR_STOP_EXP = "The bay length of the door-to-door AV, infinity means no bay length restriction, linkLength means length equals to the link length";

	public static final String MIN_BAY_SIZE = "minBaySize";
	static final String MIN_BAY_SIZE_EXP = "The minimum bay size for transit stop, 0.0 by default.";

	public static final String CHARGE_FILE ="chargingFile";
	static final String CHARGE_FILE_EXP = "The location of the charging points. The file format according to charger.dtd";

	public static final String DRT_VEHICLE_TYPE_FILE ="drtVehicleTypeFile";
	static final String DRT_VEHICLE_TYPE_FILE_EXP = "The type of drt vehicles. The file format according to drt_vehicle_type.dtd";

	public static final String MIN_BATTERY = "minBattery";
	static final String MIN_BATTERY_EXP = "When reaches the min battery value, the vehicle will go to charge";

	public static final String MIN_REQUEST_ACCEPT = "minRequestAccept";
	static final String MIN_REQUEST_ACCEPT_EXP ="If the request is too far, reject it";

	public static final String EAV = "eav";
	static final String EAV_EXP = "If true, enable the charging behavior; If false, don't consider charging";

	public static final String BAY_MODE = "bayMode";
	static final String BAY_MODE_EXP = "Type of infrastructure for dwelling";



	@NotNull
	private ParkingStrategy.Strategies parkingStrategy = ParkingStrategy.Strategies.noParkingStrategy;

	@NotNull
	private String depotFile = null;

	@NotNull
	private Door2DoorStop door2DoorStop= Door2DoorStop.infinity;

	private double minBaySize = 0.0;

	private String chargeFile = null;
	private String drtVehicleTypeFile = null;
	private double minBattery = 0.0;
	private double minRequestAccept = 0.0;
	private boolean eav = false;

	private BayMode bayMode;




	@Override
	public Map<String, String> getComments() {
		Map<String, String> map = super.getComments();
		map.put(PARKING_STRATEGY, PARKING);
		map.put(DEPOT_FILE, DEPOT_FILE_EXP);
		map.put(DOOR_2_DOOR_STOP, DOOR_2_DOOR_STOP_EXP);
		map.put(MIN_BAY_SIZE, MIN_BAY_SIZE_EXP);
		map.put(CHARGE_FILE, CHARGE_FILE_EXP);
		map.put(DRT_VEHICLE_TYPE_FILE, DRT_VEHICLE_TYPE_FILE_EXP);
		map.put(MIN_BATTERY, MIN_BATTERY_EXP);
		map.put(MIN_REQUEST_ACCEPT, MIN_REQUEST_ACCEPT_EXP);
		map.put(EAV, EAV_EXP);
		map.put(BAY_MODE, BAY_MODE_EXP);
		return map;
	}

		/**
	 *
	 * @return -- {@value #DEPOT_FILE_EXP}
	 */
	@StringGetter(DEPOT_FILE)
	public String getDepotFile() {
		return depotFile;
	}
	/**
	 *
	 * @param depotFile -- {@value #DEPOT_FILE_EXP}
	 */
	@StringSetter(DEPOT_FILE)
	public void setDepotFile(String depotFile) {
		this.depotFile = depotFile;
	}
	/**
	 *
	 * @return -- {@value #DEPOT_FILE_EXP}
	 */
	public URL getDepotFileUrl(URL context) {
		return ConfigGroup.getInputFileURL(context, this.depotFile);
	}

	/**
	 *
	 * @return -- {@value #PARKING}
	 */
	@StringGetter(PARKING_STRATEGY)
	public ParkingStrategy.Strategies getParkingStrategy() {
		return parkingStrategy;
	}

	/**
	 *
	 * @param parkingStrategy -- {@value #PARKING}
	 */
	@StringSetter(PARKING_STRATEGY)
	public void setParkingStrategy(String parkingStrategy) {

		this.parkingStrategy = ParkingStrategy.Strategies.valueOf(parkingStrategy);
	}


	@StringGetter(DOOR_2_DOOR_STOP)
	public Door2DoorStop getDoor2DoorStop() {
		return door2DoorStop;
	}

	@StringSetter(DOOR_2_DOOR_STOP)
	public void setDoor2DoorStop(String door2doorStop) {
		this.door2DoorStop = Door2DoorStop.valueOf(door2doorStop);
	}

	@StringGetter(MIN_BAY_SIZE)
	public double getMinBaySize(){
		return minBaySize;
	}
	@StringSetter(MIN_BAY_SIZE)
	public void setMinBaySize(double minBaySize){
		this.minBaySize = minBaySize;
	}

	@StringGetter(BAY_MODE)
	public BayMode getBayMode() {
		return bayMode;
	}
	@StringSetter(BAY_MODE)
	public void setBayMode(String bayMode) {
		this.bayMode= BayMode.valueOf(bayMode);
	}

	public URL getDrtVehicleTypeFileURL(URL context){
		return ConfigGroup.getInputFileURL(context, drtVehicleTypeFile);
	}
	@StringGetter(DRT_VEHICLE_TYPE_FILE)
	public String getDrtVehicleTypeFile(){
		return this.drtVehicleTypeFile;
	}
	@StringSetter(DRT_VEHICLE_TYPE_FILE)
	public void setDrtVehicleTypeFile(String drtVehicleTypeFile){
		this.drtVehicleTypeFile = drtVehicleTypeFile;
	}

	public URL getChargeFileURL(URL context){
		return ConfigGroup.getInputFileURL(context, chargeFile);
	}
	@StringGetter(CHARGE_FILE)
	public String getChargeFile(){
		return this.chargeFile;
	}
	@StringSetter(CHARGE_FILE)
	public void setChargeFile(String chargeFile){
		this.chargeFile = chargeFile;
	}

	@StringGetter(MIN_BATTERY)
	public double getMinBattery() {
		return minBattery;
	}
	@StringSetter(MIN_BATTERY)
	public void setMinBattery(double minBattery) {
		this.minBattery = minBattery;
	}

	@StringGetter(MIN_REQUEST_ACCEPT)
	public double getMinRequestAccept() {
		return minRequestAccept;
	}
	@StringSetter(MIN_REQUEST_ACCEPT)
	public void setMinRequestAccept(double minRequestAccept) {
		this.minRequestAccept = minRequestAccept;
	}

	@StringGetter(EAV)
	public boolean isEAV() {
		return eav;
	}
	@StringSetter(EAV)
	public void setEAV(boolean eav) {
		this.eav = eav;
	}



}
