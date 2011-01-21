package com.pwnetics.helper;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class CollectionMathTest {

	@Test
	public void testSumInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSum() {
		fail("Not yet implemented");
	}

	@Test
	public void testSumSafer() {
		Random valRnd = new Random(42);
		Random shuffleRnd = new Random(42);
		List<Double> c = new ArrayList<Double>();

		for(int i=0; i<100000; i++) {
			c.add(valRnd.nextDouble());
		}

		List<Double> normalSum = new ArrayList<Double>();
		List<Double> saferSum = new ArrayList<Double>();
		for(int j=0; j<10; j++) {
			normalSum.add(CollectionMath.sum(c));
			saferSum.add(CollectionMath.sumKahan(c));
			Collections.shuffle(c,shuffleRnd);
		}
//		System.out.println((CollectionMath.variance(normalSum) - 7e-20) + "\t" + CollectionMath.variance(saferSum));
		assertTrue(CollectionMath.variance(normalSum) - 7e-20 > CollectionMath.variance(saferSum));
	}

	@Test
	public void testMeanInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testMean() {
		fail("Not yet implemented");
	}

	@Test
	public void testVarianceInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testVariance() {
		fail("Not yet implemented");
	}

	@Test
	public void testMinInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testMaxInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testMax() {
		fail("Not yet implemented");
	}

}
