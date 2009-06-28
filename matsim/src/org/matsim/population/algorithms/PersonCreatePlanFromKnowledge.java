/* *********************************************************************** *
 * project: org.matsim.*
 * PersonCreatePlanFromKnowledge.java
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

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.api.facilities.ActivityOption;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.ActivityImpl;
import org.matsim.knowledges.Knowledges;

public class PersonCreatePlanFromKnowledge extends AbstractPersonAlgorithm {

	private Knowledges knowledges;

	public PersonCreatePlanFromKnowledge(Knowledges knowledges) {
		super();
		this.knowledges = knowledges;
	}

	@Override
	public void run(final Person person) {
		Plan p = person.createPlan(true);
		ActivityFacility home_facility = this.knowledges.getKnowledgesByPersonId().get(person.getId()).getActivities("home").get(0).getFacility();
		ArrayList<ActivityOption> acts = this.knowledges.getKnowledgesByPersonId().get(person.getId()).getActivities();

		// first act end time = [7am.9am]
		int time = 7*3600 + (MatsimRandom.getRandom().nextInt(2*3600));

		// first act (= home)
		ActivityImpl a = p.createActivity("home", home_facility.getCoord());
		a.setLink(home_facility.getLink());
		a.setStartTime(0.0);
		a.setDuration(time);
		a.setEndTime(time);
		a.setFacility(home_facility);
		Leg l = p.createLeg(TransportMode.car);
		l.setDepartureTime(time);
		l.setTravelTime(0);
		l.setArrivalTime(time);

		int nof_acts = 1 + MatsimRandom.getRandom().nextInt(3);
		int dur = 12*3600/nof_acts;

		// in between acts
		for (int i=0; i<nof_acts; i++) {
			int act_index = MatsimRandom.getRandom().nextInt(acts.size());
			ActivityOption act = acts.get(act_index);
			ActivityFacility f = act.getFacility();
			a = p.createActivity(act.getType(),f.getCoord());
			a.setLink(f.getLink());
			a.setStartTime(time);
			a.setDuration(dur);
			a.setEndTime(time + dur);
			a.setFacility(f);
			time += dur;
			l = p.createLeg(TransportMode.car);
			l.setDepartureTime(time);
			l.setTravelTime(0);
			l.setArrivalTime(time);
		}

		// last act (= home)
		a = p.createActivity("home",home_facility.getCoord());
		a.setLink(home_facility.getLink());
		a.setStartTime(time);
		a.setEndTime(24*3600);
		a.setDuration(24*3600 - time);
		a.setFacility(home_facility);
	}
}
