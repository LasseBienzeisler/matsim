/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.core.population;

import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

/**
 * Test for convenience methods of Act.
 * 
 * @author dgrether
 */
public class ActivityTest extends MatsimTestCase {

	public void testCalculateDuration() {
		ActivityImpl testee = new org.matsim.core.population.ActivityImpl("h", new CoordImpl(0, 0));
		testee.setStartTime(0.0);
		testee.setDuration(0.0);
		testee.setEndTime(0.0);

		assertNotNull(testee);
		assertEquals(0.0, testee.calculateDuration(), EPSILON);
		testee.setEndTime(5.5 * 3600.0);
		assertEquals(5.5 * 3600.0, testee.calculateDuration(), EPSILON);
		testee.setStartTime(Time.UNDEFINED_TIME);
		assertEquals(5.5 * 3600.0, testee.calculateDuration(), EPSILON);
		testee.setEndTime(Time.UNDEFINED_TIME);
		assertEquals(0.0, testee.calculateDuration(), EPSILON);
		testee.setDuration(Time.UNDEFINED_TIME);
		Exception e = null;
		try {
			testee.calculateDuration();
		} catch (RuntimeException ex) {
			e = ex;
		}
		assertNotNull(e);
		testee.setStartTime(17 * 3600.0);
		assertEquals(7 * 3600.0, testee.calculateDuration(), EPSILON);
	}

}
