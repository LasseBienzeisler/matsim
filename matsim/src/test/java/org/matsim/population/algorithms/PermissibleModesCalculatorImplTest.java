/* *********************************************************************** *
 * project: org.matsim.*
 * PermissibleModesCalculatorImplTest.java
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
package org.matsim.population.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

/**
 * @author thibautd
 */
public class PermissibleModesCalculatorImplTest {
	private static class Fixture {
		public final String name;
		public final Plan plan;
		public final boolean carAvail;

		public Fixture( 
				final String name,
				final Plan plan,
				final boolean carAvail ) {
			this.name = name;
			this.plan = plan;
			this.carAvail = carAvail;
		}
	}
	private final List<Fixture> fixtures = new ArrayList<Fixture>();

	@After
	public void clean() {
		fixtures.clear();
	}

	@Before
	public void fixtureWithNothing() {
		String name = "no information";
		PersonImpl person = new PersonImpl( new IdImpl( name ) );
		Plan plan = new PlanImpl( person );
		fixtures.add( new Fixture( name , plan , true ) );
	}

	@Before
	public void fixtureWithNoLicense() {
		String name = "no License";
		PersonImpl person = new PersonImpl( new IdImpl( name ) );
		Plan plan = new PlanImpl( person );
		person.setLicence( "no" );
		fixtures.add( new Fixture( name , plan , false ) );
	}

	@Before
	public void fixtureWithNoCar() {
		String name = "no car" ;
		PersonImpl person = new PersonImpl( new IdImpl( name ) );
		Plan plan = new PlanImpl( person );
		person.setCarAvail( "never" );
		fixtures.add( new Fixture( name , plan , false ) );
	}

	@Before
	public void fixtureWithCarSometimes() {
		String name = "car sometimes";
		PersonImpl person = new PersonImpl( new IdImpl( name ) );
		Plan plan = new PlanImpl( person );
		person.setCarAvail( "sometimes" );
		fixtures.add( new Fixture( name , plan , true ) );
	}

	@Test
	public void testWhenConsideringCarAvailability() throws Exception {
		final List<String> modesWithCar = Arrays.asList( TransportMode.car , "rail" , "plane" );
		final List<String> modesWithoutCar = Arrays.asList( "rail" , "plane" );

		final PermissibleModesCalculator calculatorWithCarAvailability =
			new PermissibleModesCalculatorImpl(
					modesWithCar.toArray( new String[0] ),
					true);

		for (Fixture f : fixtures) {
			assertListsAreCompatible(
					f.name,
					f.carAvail ? modesWithCar : modesWithoutCar,
					calculatorWithCarAvailability.getPermissibleModes( f.plan ) );
		}
	}

	@Test
	public void testWhenNotConsideringCarAvailability() throws Exception {
		final List<String> modesWithCar = Arrays.asList( TransportMode.car , "rail" , "plane" );

		final PermissibleModesCalculator calculatorWithCarAvailability =
			new PermissibleModesCalculatorImpl(
					modesWithCar.toArray( new String[0] ),
					false);

		for (Fixture f : fixtures) {
			assertListsAreCompatible(
					f.name,
					modesWithCar,
					calculatorWithCarAvailability.getPermissibleModes( f.plan ) );
		}
	}

	private static void assertListsAreCompatible(
			final String fixtureName,
			final List<String> expected,
			final Collection<String> actual) {
		assertEquals(
				expected+" and "+actual+" have incompatible sizes for fixture "+fixtureName,
				expected.size(),
				actual.size());

		assertTrue(
				expected+" and "+actual+" are not compatible for fixture "+fixtureName,
				expected.containsAll( actual ));
	}
}

