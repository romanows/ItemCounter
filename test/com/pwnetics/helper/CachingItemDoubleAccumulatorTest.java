/*
Copyright 2010 Brian Romanowski. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY BRIAN ROMANOWSKI ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BRIAN ROMANOWSKI OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors.
*/


package com.pwnetics.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CachingItemDoubleAccumulatorTest {

	@Test
	public void testSum() {
		// We do some testing of the original IC's count map being used in the CIC, here.

		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		CachingItemDoubleAccumulator<String> cic = CachingItemDoubleAccumulator.build(ic, false);
		assertTrue(ic.sum() == null);
		assertTrue(cic.sum() == null);

		ic.add("a", 1.0);
		assertTrue(ic.sum() == 1);
		assertTrue(cic.sum() == 1);  // Unlike the CachingItemCounter, this _will_ track changes in this case only, because the cached value is the same as the "not-yet-cached" placeholder!
		cic = CachingItemDoubleAccumulator.build(ic, false);
		assertTrue(cic.sum() == 1);
		ic.add("a", 2.0);
		assertTrue(ic.sum() == 3.0);
		assertTrue(cic.sum() == 1);  // Now we see the caching inconsistency show
		cic.invalidate();
		assertTrue(cic.sum() == 3.0);

		ic.add("a", -2.0); // undo the previous add of 2.0

		ic.set("a", 42);
		cic = CachingItemDoubleAccumulator.build(ic, true);
		assertTrue(ic.sum() == 42);
		assertTrue(cic.sum() == 42);

		ic.set("b", 11);
		assertTrue(ic.sum() == 42.0 + 11.0);
		assertTrue(cic.sum() == 42);
		cic.invalidate();
		assertTrue(cic.sum() == 42);  // The copy makes the cic and ic independent

		cic.set("b", 12);  // this is added to the cic's copy of the count, and invalidates the cache
		assertTrue(ic.sum() == 42 + 11);
		assertTrue(cic.sum() == 42 + 12);

		cic.set("a", -20);
		assertTrue(cic.sum() == -20 + 12);
	}

	@Test
	public void testMinMax() {
		// Just copied from the ItemDoubleAccumulatorTest; we're not concerned with the mutability issues, here.
		CachingItemDoubleAccumulator<String> ic = new CachingItemDoubleAccumulator<String>();
		ItemDoubleAccumulator<String>.KeyValuePair mn = ic.min();
		ItemDoubleAccumulator<String>.KeyValuePair mx = ic.max();

		assertTrue(mn.getKey() == null);
		assertTrue(mn.getValue() == null);
		assertTrue(mx.getKey() == null);
		assertTrue(mx.getValue() == null);

		ic.add("a",1.0);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1.0);
		assertTrue(mx.getKey() == "a");
		assertTrue(mx.getValue() == 1.0);

		ic.add("b",1.0);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1.0);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 1.0);

		ic.add("c",1.0);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1.0);
		assertTrue(mx.getKey() == "c");
		assertTrue(mx.getValue() == 1.0);

		ic.set("b",3.0);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "a");
		assertTrue(mn.getValue() == 1.0);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 3.0);

		ic.set("a",2.0);
		mn = ic.min();
		mx = ic.max();

		assertTrue(mn.getKey() == "c");
		assertTrue(mn.getValue() == 1.0);
		assertTrue(mx.getKey() == "b");
		assertTrue(mx.getValue() == 3.0);
	}

	@Test
	public void testMean() {
		// Just copied from the ItemDoubleAccumulatorTest; we're not concerned with the mutability issues, here.
		CachingItemDoubleAccumulator<String> ic = new CachingItemDoubleAccumulator<String>();
		assertTrue(ic.mean() == null);
		ic.add("a",1.0);
		assertTrue(ic.mean() == 1.0);
		ic.add("b",1.0);
		assertTrue(ic.mean() == 1.0);
		ic.add("c",1.0);
		assertTrue(ic.mean() == 1.0);
		ic.add("a",1.0);
		assertTrue(ic.mean() == (2+1+1)/3.0);
		ic.add("b",1.0);
		assertTrue(ic.mean() == (2+2+1)/3.0);
		ic.add("c",1.0);
		assertTrue(ic.mean() == 2.0);

		ic.set("a", Integer.MAX_VALUE);
		assertTrue(ic.mean() == 2147483651.0/3.0);
	}

	@Test
	public void testVariancePopulation() {
		// Just copied from the ItemDoubleAccumulatorTest; we're not concerned with the mutability issues, here.
		CachingItemDoubleAccumulator<String> ic = new CachingItemDoubleAccumulator<String>();
		assertTrue(ic.variancePopulation() == null);
		ic.add("a",1.0);
		assertTrue(ic.variancePopulation() == 0);
		ic.add("b",1.0);
		assertTrue(ic.variancePopulation() == 0);
		ic.add("c",1.0);
		assertTrue(ic.variancePopulation() == 0);
		ic.add("a",1.0);
		assertTrue(ic.variancePopulation() == 2.0 / 9.0);
		ic.add("b",1.0);
		assertTrue(ic.variancePopulation() == 2.0 / 9.0);
		ic.add("c",1.0);
		assertTrue(ic.variancePopulation() == 0.0);

		ic.set("a", Integer.MAX_VALUE);
		double expected = 9223372011084972050.0/9.0;
		assertEquals(expected, ic.variancePopulation(), expected * 1e-9);  // epsilon is relative to magnitude of expected value; arbitrarily chosen amount
	}

	@Test
	public void testVariance() {
		// Just copied from the ItemDoubleAccumulatorTest; we're not concerned with the mutability issues, here.
		CachingItemDoubleAccumulator<String> ic = new CachingItemDoubleAccumulator<String>();
		assertTrue(ic.variance() == null);
		ic.add("a",1.0);
		assertTrue(ic.variance() == 0);
		ic.add("b",1.0);
		assertTrue(ic.variance() == 0);
		ic.add("c",1.0);
		assertTrue(ic.variance() == 0);
		ic.add("a",1.0);
		assertTrue(ic.variance() == 1.0 / 3.0);
		ic.add("b",1.0);
		assertTrue(ic.variance() == 1.0 / 3.0);
		ic.add("c",1.0);
		assertTrue(ic.variance() == 0.0);

		ic.set("a", Integer.MAX_VALUE);
		double expected = 4611686005542486025.0/3.0;
		assertEquals(expected, ic.variance(), expected * 1e-9);  // epsilon is relative to magnitude of expected value; arbitrarily chosen amount
	}

	@Test
	public void testAsUnmodifiable() {
		// Just copied from the ItemDoubleAccumulatorTest; we're not concerned with the mutability issues, here.
		CachingItemDoubleAccumulator<String> ic = new CachingItemDoubleAccumulator<String>();
		ic.add("a", 1.0);
		ic.add("b", 1.0);
		ic.add("a", 1.0);
		ic.add("c", 1.0);

		ItemDoubleAccumulator<String> uc = ic.asUnmodifiable();
		assertTrue(uc.get("a") == 2.0);
		assertTrue(uc.get("b") == 1.0);
		try {
			uc.add("a", 1.0);
			fail("unmodifiable ItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
		try {
			uc.set("a",5.0);
			fail("unmodifiable ItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
	}
}
