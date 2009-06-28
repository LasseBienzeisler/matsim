/* *********************************************************************** *
 * project: org.matsim.*
 * PlansCreateTripsFromODMatrix.java
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

package org.matsim.population.algorithms;

import java.util.ArrayList;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;
import org.matsim.world.WorldUtils;
import org.matsim.world.Zone;
import org.matsim.world.ZoneLayer;

public class PlansCreateTripsFromODMatrix {

	private final Matrix matrix;
	private final ArrayList<Double> timeDistribution;
	private final int timeBinSize;

	public PlansCreateTripsFromODMatrix(final Matrix matrix, final ArrayList<Double> timeDistribution) {
		super();
		this.matrix = matrix;
		this.timeDistribution = new ArrayList<Double>(timeDistribution.size());
		double sum = 0.0;
		for (int i = 1, max = timeDistribution.size(); i < max; i++) {
			sum += timeDistribution.get(i);	// instead of building the sum every time
			this.timeDistribution.add(sum); // build the sum once and cache it
		}
		this.timeBinSize = (24*3600) / timeDistribution.size();
	}

	public void run(final Population plans) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");

		if (plans.getName() == null) {
			plans.setName("created by '" + this.getClass().getName() + "' with matrix '" + this.matrix.getId() + "'");
		}
		if (!plans.getPersons().isEmpty()) {
			Gbl.errorMsg("[plans=" + plans + " is not empty]");
		}

		ZoneLayer layer = (ZoneLayer)this.matrix.getLayer();

		int counter = 0;
		double sum = 0.0;
		for (ArrayList<Entry> entries : this.matrix.getFromLocations().values()) {
			for (Entry entry : entries) {
				sum += entry.getValue();
				while (sum >= 1.0) {
					counter++;
					sum--;
					Person person = new PersonImpl(new IdImpl(counter));
					person.setCarAvail("yes");
					person.setEmployed("yes");
					Plan plan = person.createPlan(true);
					Coord coord = WorldUtils.getRandomCoordInZone((Zone)entry.getFromLocation(), layer);
					int endTime = -1;

					double rnd = MatsimRandom.getRandom().nextDouble();
					for (int i = 0, max = this.timeDistribution.size(); i < max && endTime == -1; i++) {
						if (rnd <= this.timeDistribution.get(i)) {
							endTime = i*this.timeBinSize + MatsimRandom.getRandom().nextInt(this.timeBinSize);
						}
					}

					ActivityImpl a = plan.createActivity("work", coord);
					a.setEndTime(endTime);
					plan.createLeg(TransportMode.car);
					a = plan.createActivity("work", coord);

					plans.addPerson(person); // add person should be last for when plans-streaming is one, because in this moment the plans are written to file.
				}
			}
		}

		System.out.println("    done.");
	}

}
