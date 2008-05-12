/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesFindMinMaxCoords.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.socialnetworks.algorithms;

import org.matsim.facilities.Facilities;
import org.matsim.facilities.Facility;
import org.matsim.utils.geometry.CoordI;
import org.matsim.utils.geometry.shared.Coord;

public class FacilitiesFindScenarioMinMaxCoords {

	private CoordI minCoord;
	private CoordI maxCoord;

	public FacilitiesFindScenarioMinMaxCoords() {
		super();
	}

	public void run(Facilities facilities) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");
		System.out.println("  NOTE you could get these limits from world");

		double min_x = Double.MAX_VALUE;
		double min_y = Double.MAX_VALUE;
		double max_x = Double.MIN_VALUE;
		double max_y = Double.MIN_VALUE;

		for (Facility f : facilities.getFacilities().values()) {
			CoordI c = f.getCenter();
			if(c.getX()<=min_x){
				min_x=c.getX();
			}
			if(c.getY()<=min_y){
				min_y=c.getY();
			}
			if(c.getX()>=max_x){
				max_x=c.getX();
			}
			if(c.getY()>=max_y){
				max_y=c.getY();
			}
		}
		minCoord = new Coord(min_x, min_y);
		maxCoord = new Coord(max_x, max_y);
	}
	public CoordI getMinCoord(){
		return this.minCoord;
	}
	public CoordI getMaxCoord(){
		return this.maxCoord;
	}
}
