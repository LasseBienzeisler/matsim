/* *********************************************************************** *
 * project: org.matsim.*
 * FixedRouteLegTravelTimeEstimatorTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.planomat.costestimators;

import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.basic.v01.population.BasicPlanElement;
import org.matsim.core.api.experimental.Scenario;
import org.matsim.core.api.experimental.ScenarioLoader;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanomatConfigGroup;
import org.matsim.core.events.AgentDepartureEvent;
import org.matsim.core.events.BasicEventImpl;
import org.matsim.core.events.Events;
import org.matsim.core.events.LinkEnterEvent;
import org.matsim.core.events.LinkLeaveEvent;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.costcalculators.TravelTimeDistanceCostCalculator;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

public class FixedRouteLegTravelTimeEstimatorTest extends MatsimTestCase {

	protected Scenario scenario = null;
	
	protected static final Id TEST_PERSON_ID = new IdImpl("1");
	private static final int TEST_PLAN_NR = 0;
	private static final int TEST_LEG_NR = 0;
	private static final int TIME_BIN_SIZE = 900;

	protected PersonImpl testPerson = null;
	protected PlanImpl testPlan = null;
	protected LegImpl testLeg = null;
	protected ActivityImpl originAct = null;
	protected ActivityImpl destinationAct = null;
	protected Config config = null;

	private static final String CONFIGFILE = "test/scenarios/equil/config.xml";

	private static final Logger log = Logger.getLogger(FixedRouteLegTravelTimeEstimatorTest.class);

	@Override
	protected void setUp() throws Exception {

		super.setUp();

		this.config = super.loadConfig(CONFIGFILE);
		this.config.plans().setInputFile("test/scenarios/equil/plans1.xml");

		ScenarioLoader loader = new ScenarioLoader(config);
		loader.loadScenario();
		this.scenario = loader.getScenario();
		
		// the estimator is tested on the central route alternative through equil-net
		// first person
		this.testPerson = this.scenario.getPopulation().getPersons().get(TEST_PERSON_ID);
		// only plan of that person
		this.testPlan = this.testPerson.getPlans().get(TEST_PLAN_NR);
		// first leg
		List<? extends BasicPlanElement> actsLegs = this.testPlan.getPlanElements();
		this.testLeg = (LegImpl) actsLegs.get(TEST_LEG_NR + 1);
		// activities before and after leg
		this.originAct = (ActivityImpl) actsLegs.get(TEST_LEG_NR);
		this.destinationAct = (ActivityImpl) actsLegs.get(TEST_LEG_NR + 2);

		config.travelTimeCalculator().setTraveltimeBinSize(TIME_BIN_SIZE);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.scenario = null;
		this.destinationAct = null;
		this.originAct = null;
		this.testLeg = null;
		this.testPerson = null;
		this.testPlan = null;
	}

	public void testGetLegTravelTimeEstimation() {

		this.scenario.getConfig().charyparNagelScoring().setMarginalUtlOfDistanceCar(0.0);

		for (PlanomatConfigGroup.SimLegInterpretation simLegInterpretation : PlanomatConfigGroup.SimLegInterpretation.values()) {

			DepartureDelayAverageCalculator tDepDelayCalc = new DepartureDelayAverageCalculator(this.scenario.getNetwork(), TIME_BIN_SIZE);
			TravelTimeCalculator linkTravelTimeEstimator = new TravelTimeCalculator(this.scenario.getNetwork(), config.travelTimeCalculator());
			TravelCost linkTravelCostEstimator = new TravelTimeDistanceCostCalculator(linkTravelTimeEstimator, config.charyparNagelScoring());

			PlansCalcRoute plansCalcRoute = new PlansCalcRoute(this.scenario.getConfig().plansCalcRoute(), this.scenario.getNetwork(), linkTravelCostEstimator, linkTravelTimeEstimator);

			LegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory = new LegTravelTimeEstimatorFactory(linkTravelTimeEstimator, tDepDelayCalc);
			FixedRouteLegTravelTimeEstimator testee = (FixedRouteLegTravelTimeEstimator) legTravelTimeEstimatorFactory.getLegTravelTimeEstimator(
					simLegInterpretation, 
					plansCalcRoute);
			
			Events events = new Events();
			events.addHandler(tDepDelayCalc);
			events.addHandler(linkTravelTimeEstimator);
			events.printEventHandlers();

			NetworkRoute route = (NetworkRoute) testLeg.getRoute();
			List<LinkImpl> links = route.getLinks();

			// let's test a route without events first
			// should result in free speed travel time, without departure delay
			double departureTime = Time.parseTime("06:03:00");
			double legTravelTime = testee.getLegTravelTimeEstimation(
					testPerson.getId(),
					departureTime,
					originAct,
					destinationAct,
					testLeg);

			double expectedLegEndTime = departureTime;

			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CharyparEtAlCompatible)) {
				expectedLegEndTime += originAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}
			for (LinkImpl link : links) {
				expectedLegEndTime += link.getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}
			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CetinCompatible)) {
				expectedLegEndTime += destinationAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}			
			assertEquals(expectedLegEndTime, departureTime + legTravelTime, MatsimTestCase.EPSILON);

			// next, a departure delay of 5s at the origin link is added
			departureTime = Time.parseTime("06:05:00");
			double depDelay = Time.parseTime("00:00:05");
			AgentDepartureEvent depEvent = new AgentDepartureEvent(
					departureTime,
					TEST_PERSON_ID,
					originAct.getLink().getId());
			LinkLeaveEvent leaveEvent = new LinkLeaveEvent(departureTime + depDelay, testPerson, originAct.getLink());

			for (BasicEventImpl event : new BasicEventImpl[]{depEvent, leaveEvent}) {
				events.processEvent(event);
			}

			legTravelTime = testee.getLegTravelTimeEstimation(
					TEST_PERSON_ID,
					departureTime,
					originAct,
					destinationAct,
					testLeg);

			expectedLegEndTime = departureTime;
			expectedLegEndTime += depDelay;

			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CharyparEtAlCompatible)) {
				expectedLegEndTime += originAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}
			for (LinkImpl link : links) {
				expectedLegEndTime += link.getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}
			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CetinCompatible)) {
				expectedLegEndTime += destinationAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
			}			
			assertEquals(expectedLegEndTime, departureTime + legTravelTime, MatsimTestCase.EPSILON);
			
			// now let's add some travel events
			String[][] eventTimes = new String[][]{
					new String[]{"06:05:00", "06:07:00", "06:09:00"},
					new String[]{"06:16:00", "06:21:00", "06:26:00"}
			};

			BasicEventImpl event = null;
			for (int eventTimesCnt = 0; eventTimesCnt < eventTimes.length; eventTimesCnt++) {
				for (int linkCnt = 0; linkCnt < links.size(); linkCnt++) {
					event = new LinkEnterEvent(
							Time.parseTime(eventTimes[eventTimesCnt][linkCnt]),
							testPerson,
							links.get(linkCnt));
					events.processEvent(event);
					event = new LinkLeaveEvent(
							Time.parseTime(eventTimes[eventTimesCnt][linkCnt + 1]),
							testPerson,
							links.get(linkCnt));
					events.processEvent(event);
				}
			}

			// test a start time where all link departures will be in the first time bin
			departureTime = Time.parseTime("06:10:00");
			legTravelTime = testee.getLegTravelTimeEstimation(
					TEST_PERSON_ID,
					departureTime,
					originAct,
					destinationAct,
					testLeg);
			expectedLegEndTime = departureTime;
			expectedLegEndTime += depDelay;

			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CharyparEtAlCompatible)) {
				expectedLegEndTime = testee.processLink(originAct.getLink(), expectedLegEndTime);
			}
			expectedLegEndTime = testee.processRouteTravelTime(route, expectedLegEndTime);
			if (simLegInterpretation.equals(PlanomatConfigGroup.SimLegInterpretation.CetinCompatible)) {
				expectedLegEndTime = testee.processLink(destinationAct.getLink(), expectedLegEndTime);
			}
			
			assertEquals(expectedLegEndTime, departureTime + legTravelTime, EPSILON);
			
			// test public transport mode
			departureTime = Time.parseTime("06:10:00");
			
			LegImpl ptLeg = new LegImpl(this.testLeg);
			
			ptLeg.setMode(TransportMode.pt);
			legTravelTime = testee.getLegTravelTimeEstimation(
					TEST_PERSON_ID,
					departureTime,
					originAct,
					destinationAct,
					ptLeg);
			// the free speed travel time from h to w in equil-test, as simulated by Cetin, is 15 minutes
			expectedLegEndTime = departureTime + (2 * Time.parseTime("00:15:00"));

			// quite a high epsilon here, due to rounding of the free speed in the network.xml file
			// which is 27.78 m/s, but should be 27.777777... m/s, reflecting 100 km/h
			// and 5.0 seconds travel time estimation error is not _that_ bad
			double freeSpeedEpsilon = 5.0;
			assertEquals(expectedLegEndTime, departureTime + legTravelTime, freeSpeedEpsilon);

		}
	}

	public void testProcessDeparture() {

		DepartureDelayAverageCalculator tDepDelayCalc = new DepartureDelayAverageCalculator(this.scenario.getNetwork(), TIME_BIN_SIZE);
		TravelTimeCalculator linkTravelTimeEstimator = new TravelTimeCalculator(this.scenario.getNetwork(), config.travelTimeCalculator());
		TravelCost linkTravelCostEstimator = new TravelTimeDistanceCostCalculator(linkTravelTimeEstimator, config.charyparNagelScoring());

		PlansCalcRoute plansCalcRoute = new PlansCalcRoute(
				this.scenario.getConfig().plansCalcRoute(), 
				this.scenario.getNetwork(), 
				linkTravelCostEstimator, 
				linkTravelTimeEstimator);

		LegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory = new LegTravelTimeEstimatorFactory(linkTravelTimeEstimator, tDepDelayCalc);
		
		FixedRouteLegTravelTimeEstimator testee = (FixedRouteLegTravelTimeEstimator) legTravelTimeEstimatorFactory.getLegTravelTimeEstimator(
				PlanomatConfigGroup.SimLegInterpretation.CetinCompatible, 
				plansCalcRoute);
		Id linkId = this.originAct.getLinkId();

		Events events = new Events();
		events.addHandler(tDepDelayCalc);
		events.printEventHandlers();

		// this gives a delay of 36s (1/100th of an hour)
		AgentDepartureEvent depEvent = new AgentDepartureEvent(6.03 * 3600, TEST_PERSON_ID, this.originAct.getLinkId());
		LinkLeaveEvent leaveEvent = new LinkLeaveEvent(6.04 * 3600, TEST_PERSON_ID, this.originAct.getLinkId());

		for (BasicEventImpl event : new BasicEventImpl[]{depEvent, leaveEvent}) {
			events.processEvent(event);
		}

		double startTime = 6.00 * 3600;
		double delayEndTime = testee.processDeparture(this.scenario.getNetwork().getLink(new IdImpl("1")), startTime);
		assertEquals(delayEndTime, startTime + 36.0, EPSILON);

		// let's add another delay of 72s, should result in an average of 54s
		depEvent = new AgentDepartureEvent(6.02 * 3600, TEST_PERSON_ID, linkId);
		leaveEvent = new LinkLeaveEvent(6.04 * 3600, TEST_PERSON_ID, linkId);

		for (BasicEventImpl event : new BasicEventImpl[]{depEvent, leaveEvent}) {
			events.processEvent(event);
		}

		startTime = 6.00 * 3600;
		delayEndTime = testee.processDeparture(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(delayEndTime, startTime + (36.0 + 72.0) / 2, EPSILON);

		// the time interval for the previously tested events was for departure times from 6.00 to 6.25
		// for other time intervals, we don't have event information, so estimated delay should be 0s

		startTime = 5.9 * 3600;
		delayEndTime = testee.processDeparture(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(delayEndTime, startTime, EPSILON);

		startTime = 6.26 * 3600;
		delayEndTime = testee.processDeparture(this.scenario.getNetwork().getLink(linkId), 6.26 * 3600);
		assertEquals(delayEndTime, startTime, EPSILON);

	}

	public void testProcessRouteTravelTime() {

		DepartureDelayAverageCalculator tDepDelayCalc = new DepartureDelayAverageCalculator(this.scenario.getNetwork(), TIME_BIN_SIZE);
		TravelTimeCalculator linkTravelTimeEstimator = new TravelTimeCalculator(this.scenario.getNetwork(), config.travelTimeCalculator());
		TravelCost linkTravelCostEstimator = new TravelTimeDistanceCostCalculator(linkTravelTimeEstimator, config.charyparNagelScoring());

		PlansCalcRoute plansCalcRoute = new PlansCalcRoute(
				this.scenario.getConfig().plansCalcRoute(), 
				this.scenario.getNetwork(), 
				linkTravelCostEstimator, 
				linkTravelTimeEstimator);

		LegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory = new LegTravelTimeEstimatorFactory(linkTravelTimeEstimator, tDepDelayCalc);
		
		FixedRouteLegTravelTimeEstimator testee = (FixedRouteLegTravelTimeEstimator) legTravelTimeEstimatorFactory.getLegTravelTimeEstimator(
				PlanomatConfigGroup.SimLegInterpretation.CetinCompatible, 
				plansCalcRoute);

		Events events = new Events();
		events.addHandler(linkTravelTimeEstimator);
		events.printEventHandlers();

		NetworkRoute route = (NetworkRoute) this.testLeg.getRoute();
		log.info(route.toString());

		// generate some travel times
		BasicEventImpl event = null;

		List<LinkImpl> links = route.getLinks();
		System.out.println(links.size());

		String[][] eventTimes = new String[][]{
			new String[]{"06:05:00", "06:07:00", "06:09:00"},
			new String[]{"06:16:00", "06:21:00", "06:26:00"}
		};

		for (int eventTimesCnt = 0; eventTimesCnt < eventTimes.length; eventTimesCnt++) {
			for (int linkCnt = 0; linkCnt < links.size(); linkCnt++) {
				event = new LinkEnterEvent(
						Time.parseTime(eventTimes[eventTimesCnt][linkCnt]),
						this.testPerson,
						links.get(linkCnt));
				events.processEvent(event);
				event = new LinkLeaveEvent(
						Time.parseTime(eventTimes[eventTimesCnt][linkCnt + 1]),
						this.testPerson,
						links.get(linkCnt));
				events.processEvent(event);
			}
		}

		// test a start time where all link departures will be in the first time bin
		double startTime = Time.parseTime("06:10:00");
		double routeEndTime = testee.processRouteTravelTime(route, startTime);
		assertEquals(Time.parseTime("06:14:00"), routeEndTime, EPSILON);

		// test a start time where all link departures will be in the second time bin
		startTime = Time.parseTime("06:20:00");
		routeEndTime = testee.processRouteTravelTime(route, startTime);
		assertEquals(Time.parseTime("06:30:00"), routeEndTime, EPSILON);

		// test a start time in the first bin where one link departure is in the first bin, one in the second bin
		startTime = Time.parseTime("06:13:00");
		routeEndTime = testee.processRouteTravelTime(route, startTime);
		assertEquals(Time.parseTime("06:20:00"), routeEndTime, EPSILON);

		// test a start time in a free speed bin, having second departure in the first bin
		startTime = Time.parseTime("05:59:00");
		routeEndTime = testee.processRouteTravelTime(route, startTime);
		assertEquals(
				testee.processLink(links.get(1), startTime + this.scenario.getNetwork().getLink(links.get(0).getId()).getFreespeedTravelTime(Time.UNDEFINED_TIME)),
				routeEndTime, EPSILON);

		// test a start time in the second bin, having second departure in the free speed bin
		startTime = Time.parseTime("06:28:00");
		routeEndTime = testee.processRouteTravelTime(route, startTime);
		assertEquals(
				testee.processLink(links.get(0), startTime) + this.scenario.getNetwork().getLink(links.get(1).getId()).getFreespeedTravelTime(Time.UNDEFINED_TIME),
				routeEndTime, EPSILON);

	}

	public void testProcessLink() {

		DepartureDelayAverageCalculator tDepDelayCalc = new DepartureDelayAverageCalculator(this.scenario.getNetwork(), TIME_BIN_SIZE);
		TravelTimeCalculator linkTravelTimeEstimator = new TravelTimeCalculator(this.scenario.getNetwork(), config.travelTimeCalculator());
		TravelCost linkTravelCostEstimator = new TravelTimeDistanceCostCalculator(linkTravelTimeEstimator, config.charyparNagelScoring());

		PlansCalcRoute plansCalcRoute = new PlansCalcRoute(
				this.scenario.getConfig().plansCalcRoute(), 
				this.scenario.getNetwork(), 
				linkTravelCostEstimator, 
				linkTravelTimeEstimator);

		LegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory = new LegTravelTimeEstimatorFactory(linkTravelTimeEstimator, tDepDelayCalc);

		FixedRouteLegTravelTimeEstimator testee = (FixedRouteLegTravelTimeEstimator) legTravelTimeEstimatorFactory.getLegTravelTimeEstimator(
				PlanomatConfigGroup.SimLegInterpretation.CetinCompatible, 
				plansCalcRoute);

		Id linkId = ((NetworkRoute) this.testLeg.getRoute()).getLinks().get(0).getId();

		Events events = new Events();
		events.addHandler(linkTravelTimeEstimator);
		events.printEventHandlers();

		// we have one agent on this link, taking 1 minute and 48 seconds
		LinkEnterEvent enterEvent = new LinkEnterEvent(Time.parseTime("06:05:00"), this.testPerson, this.scenario.getNetwork().getLink(linkId));
		LinkLeaveEvent leaveEvent = new LinkLeaveEvent(Time.parseTime("06:06:48"), this.testPerson, this.scenario.getNetwork().getLink(linkId));

		for (BasicEventImpl event : new BasicEventImpl[]{enterEvent, leaveEvent}) {
			events.processEvent(event);
		}

		// for start times inside the time bin, the predicted travel time is always the same
		double startTime = Time.parseTime("06:10:00");
		double linkEndTime = testee.processLink(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(linkEndTime, Time.parseTime("06:11:48"), EPSILON);

		startTime = Time.parseTime("06:01:00");
		linkEndTime = testee.processLink(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(linkEndTime, Time.parseTime("06:02:48"), EPSILON);

		// for start times outside the time bin, the free speed travel time is returned
		double freeSpeedTravelTime = ((NetworkLayer)this.scenario.getNetwork()).getLink(linkId.toString()).getFreespeedTravelTime(Time.UNDEFINED_TIME);

		startTime = Time.parseTime("05:59:00");
		linkEndTime = testee.processLink(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(startTime + freeSpeedTravelTime, linkEndTime, EPSILON);

		startTime = Time.parseTime("08:12:00");
		linkEndTime = testee.processLink(this.scenario.getNetwork().getLink(linkId), startTime);
		assertEquals(startTime + freeSpeedTravelTime, linkEndTime, EPSILON);

	}

}
