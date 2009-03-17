/* *********************************************************************** *
 * project: org.matsim.*
 * PlanEuclideanLength.java
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

import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.utils.geometry.CoordUtils;

public class PlanEuclideanLength {

	public double getPlanLength(Plan plan) {

		double length = 0.;
		Activity fromAct = (Activity) plan.getPlanElements().get(0);
		for (int i = 2, max = plan.getPlanElements().size(); i < max; i += 2) {
			Activity toAct = (Activity) (plan.getPlanElements().get(i));

			if (fromAct != null && toAct != null) {
				double dist = CoordUtils.calcDistance(fromAct.getCoord(), toAct.getCoord());
				length += dist;
			}
			fromAct = toAct;
		}
		return length;
	}
}
