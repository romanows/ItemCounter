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

import java.util.List;

import org.junit.Test;

public class ItemDoubleAccumulatorTest {

	@Test
	public void testSet() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		assertTrue(ic.get("a") == null);
		ic.set("a", 42);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("a") != 41);
		ic.set("a", 42);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("b") == null);
		ic.set("b", 11.42);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("b") == 11.42);
		ic.set("a", 0);
		assertTrue(ic.get("a") == 0);
	}

	@Test
	public void testAdd1() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		assertTrue(ic.get("a") == null);
		assertTrue(ic.add("a", 0.25) == 0.25);
		assertTrue(ic.add("a", 0.5) == 0.25 + 0.5);
		assertTrue(ic.add("a", -0.5) == 0.25);
		ic.set("a", 42);
		assertTrue(ic.add("a",0.42) == 42.0 + 0.42);
	}

	@Test
	public void testAdd2() {
		ItemDoubleAccumulator<String> ic1 = new ItemDoubleAccumulator<String>();
		ItemDoubleAccumulator<String> ic2 = new ItemDoubleAccumulator<String>();
		ic1.add("a",1.0);
		ic1.add("a",1.0);
		ic1.add("b",2.0);
		ic1.add("b",3.0);
		ic1.add("c",5.0);
		ic2.add("b",-5.0);
		ic2.add("c",-8.0);
		ic2.add("d",-13.0);
		ic1.add(ic2);
		assertTrue(ic1.get("a") == 2.0);
		assertTrue(ic1.get("b") == 0.0);
		assertTrue(ic1.get("c") == -3.0);
		assertTrue(ic1.get("d") == -13.0);
	}

	@Test
	public void testSum() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		assertTrue(ic.sum() == null);
		ic.add("a", 1.0);
		assertTrue(ic.sum() == 1);
		ic.set("a", 42);
		assertTrue(ic.sum() == 42);
		ic.set("a", 42);
		assertTrue(ic.sum() == 42);
		ic.set("b", 11);
		assertTrue(ic.sum() == 42.0 + 11.0);
		ic.set("a", -20);
		assertTrue(ic.sum() == -9.0);
	}

	@Test
	public void testMinMax() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
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
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
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
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
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
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
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
	public void testSize() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		assertTrue(ic.size() == 0);
		ic.set("a", 42);
		assertTrue(ic.size() == 1);
		ic.set("a", 42);
		assertTrue(ic.size() == 1);
		ic.add("a",42.0);
		assertTrue(ic.size() == 1);
		ic.add("b",66.6);
		assertTrue(ic.size() == 2);
		ic.set("a", 0);
		assertTrue(ic.size() == 2);
	}

	@Test
	public void testSortByKeyValue() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		ic.add("a", 1.0);
		ic.add("b", 1.0);
		ic.add("a", 1.0);
		ic.add("c", 1.0);
		List<ItemDoubleAccumulator<String>.KeyValuePair> descending = ic.sortByValueKey(false);
		assertTrue(descending.get(0).getKey().equals("a"));
		assertTrue(descending.get(1).getKey().equals("c"));
		assertTrue(descending.get(2).getKey().equals("b"));

		List<ItemDoubleAccumulator<String>.KeyValuePair> ascending = ic.sortByValueKey(true);
		assertTrue(ascending.get(2).getKey().equals("a"));
		assertTrue(ascending.get(1).getKey().equals("c"));
		assertTrue(ascending.get(0).getKey().equals("b"));
	}

	@Test
	public void testAsUnmodifiable() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
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
