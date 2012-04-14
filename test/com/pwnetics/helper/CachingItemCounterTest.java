package com.pwnetics.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CachingItemCounterTest {

	@Test
	public void testSum() {
		// We do some testing of the original IC's count map being used in the CIC, here.

		ItemCounter<String> ic = new ItemCounter<String>();
		CachingItemCounter<String> cic = CachingItemCounter.build(ic, false);
		assertTrue(ic.sum() == 0);
		assertTrue(cic.sum() == ic.sum());

		ic.increment("a");
		assertTrue(ic.sum() == 1);
		assertTrue(cic.sum() == 0); // This should be the cached value from the original call, despite the fact that its count map has actually mutated!
		cic.invalidate();
		assertTrue(cic.sum() == 1); // Recalculated due to invalidation

		ic.set("a", 42);
		cic = CachingItemCounter.build(ic, true);
		assertTrue(ic.sum() == 42);
		assertTrue(cic.sum() == ic.sum());

		ic.set("b", 11);
		assertTrue(ic.sum() == 42 + 11);
		assertTrue(cic.sum() == 42);
		cic.invalidate();
		assertTrue(cic.sum() == 42);  // The copy makes the cic and ic independent

		cic.set("b", 12);  // this is added to the cic's copy of the count, and invalidates the cache
		assertTrue(ic.sum() == 42 + 11);
		assertTrue(cic.sum() == 42 + 12);

		cic.set("a", 0);
		assertTrue(cic.sum() == 12);
	}

	@Test
	public void testMinMax() {
		// Just copied from the ItemCounterTest; we're not concerned with the ItemCounter mutability issues, here.
		CachingItemCounter<String> ic = new CachingItemCounter<String>();
		ItemCounter<String>.KeyValuePair mn = ic.min();
		ItemCounter<String>.KeyValuePair mx = ic.max();

		assertTrue(mn.getKey() == null);
		assertTrue(mn.getValue() == 0);
		assertTrue(mx.getKey() == null);
		assertTrue(mx.getValue() == 0);

		ic.increment("a");
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1);
		assertTrue(mx.getKey() == "a");
		assertTrue(mx.getValue() == 1);

		ic.increment("b");
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 1);

		ic.increment("c");
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1);
		assertTrue(mx.getKey() == "c");
		assertTrue(mx.getValue() == 1);

		ic.set("b",3);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 3);

		ic.set("a",2);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "c");
		assertTrue(mn.getValue() == 1);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 3);
	}

	@Test
	public void testMean() {
		// Just copied from the ItemCounterTest; we're not concerned with the ItemCounter mutability issues, here.
		CachingItemCounter<String> ic = new CachingItemCounter<String>();
		assertTrue(ic.mean() == 0.0);
		ic.increment("a");
		assertTrue(ic.mean() == 1.0);
		ic.increment("b");
		assertTrue(ic.mean() == 1.0);
		ic.increment("c");
		assertTrue(ic.mean() == 1.0);
		ic.increment("a");
		assertTrue(ic.mean() == (2+1+1)/3.0);
		ic.increment("b");
		assertTrue(ic.mean() == (2+2+1)/3.0);
		ic.increment("c");
		assertTrue(ic.mean() == 2.0);

		ic.set("a", Integer.MAX_VALUE);
		assertTrue(ic.mean() == 2147483651.0/3.0);
	}

	@Test
	public void testVariancePopulation() {
		// Just copied from the ItemCounterTest; we're not concerned with the ItemCounter mutability issues, here.
		CachingItemCounter<String> ic = new CachingItemCounter<String>();
		assertTrue(ic.variancePopulation() == 0);
		ic.increment("a");
		assertTrue(ic.variancePopulation() == 0);
		ic.increment("b");
		assertTrue(ic.variancePopulation() == 0);
		ic.increment("c");
		assertTrue(ic.variancePopulation() == 0);
		ic.increment("a");
		assertTrue(ic.variancePopulation() == 2.0 / 9.0);
		ic.increment("b");
		assertTrue(ic.variancePopulation() == 2.0 / 9.0);
		ic.increment("c");
		assertTrue(ic.variancePopulation() == 0.0);

		ic.set("a", Integer.MAX_VALUE);
		double expected = 9223372011084972050.0/9.0;
		assertEquals(expected, ic.variancePopulation(), expected * 1e-9);  // epsilon is relative to magnitude of expected value; arbitrarily chosen amount
	}

	@Test
	public void testVariance() {
		// Just copied from the ItemCounterTest; we're not concerned with the ItemCounter mutability issues, here.
		CachingItemCounter<String> ic = new CachingItemCounter<String>();
		assertTrue(ic.variance() == 0);
		ic.increment("a");
		assertTrue(ic.variance() == 0);
		ic.increment("b");
		assertTrue(ic.variance() == 0);
		ic.increment("c");
		assertTrue(ic.variance() == 0);
		ic.increment("a");
		assertTrue(ic.variance() == 1.0 / 3.0);
		ic.increment("b");
		assertTrue(ic.variance() == 1.0 / 3.0);
		ic.increment("c");
		assertTrue(ic.variance() == 0.0);

		ic.set("a", Integer.MAX_VALUE);
		double expected = 4611686005542486025.0/3.0;
		assertEquals(expected, ic.variance(), expected * 1e-9);  // epsilon is relative to magnitude of expected value; arbitrarily chosen amount
	}

	@Test
	public void testAsUnmodifiable() {
		// Just copied from the ItemCounterTest; we're not concerned with the ItemCounter mutability issues, here.
		CachingItemCounter<String> ic = new CachingItemCounter<String>();
		ic.increment("a");
		ic.increment("b");
		ic.increment("a");
		ic.increment("c");

		ItemCounter<String> uc = ic.asUnmodifiable();
		assertTrue(uc.get("a") == 2);
		assertTrue(uc.get("b") == 1);
		try {
			uc.increment("a");
			fail("unmodifiable CachingItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
		try {
			uc.set("a",5);
			fail("unmodifiable CachingItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
	}
}
