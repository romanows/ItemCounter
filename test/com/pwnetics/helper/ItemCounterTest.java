package com.pwnetics.helper;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.pwnetics.helper.ItemCounter.KeyValuePair;

public class ItemCounterTest {

	@Test
	public void testSet() {
		ItemCounter<String> ic = new ItemCounter<String>();
		assertTrue(ic.get("a") == 0);
		ic.set("a", 42);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("a") != 41);
		ic.set("a", 42);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("b") == 0);
		ic.set("b", 11);
		assertTrue(ic.get("a") == 42);
		assertTrue(ic.get("b") == 11);
		ic.set("a", 0);
		assertTrue(ic.get("a") == 0);
	}

	@Test
	public void testIncrement() {
		ItemCounter<String> ic = new ItemCounter<String>();
		assertTrue(ic.get("a") == 0);
		assertTrue(ic.increment("a") == 1);
		assertTrue(ic.increment("a") == 2);
		assertTrue(ic.increment("a") == 3);
		ic.set("a", 42);
		assertTrue(ic.increment("a") == 43);
	}

	@Test
	public void testSum() {
		ItemCounter<String> ic = new ItemCounter<String>();
		assertTrue(ic.sum() == 0);
		ic.increment("a");
		assertTrue(ic.sum() == 1);
		ic.set("a", 42);
		assertTrue(ic.sum() == 42);
		ic.set("a", 42);
		assertTrue(ic.sum() == 42);
		ic.set("b", 11);
		assertTrue(ic.sum() == 42 + 11);
		ic.set("a", 0);
		assertTrue(ic.sum() == 11);
	}
	
	@Test
	public void testLargeSum() {
		ItemCounter<String> ic = new ItemCounter<String>();
		assertTrue(ic.sum() == 0);
		ic.set("a", Integer.MAX_VALUE);
		assertTrue(ic.sum() == Integer.MAX_VALUE);
		ic.set("b", Integer.MAX_VALUE);
		assertTrue(ic.sum() == (long)Integer.MAX_VALUE * 2L);
	}

	@Test
	public void testMinMax() {
		ItemCounter<String> ic = new ItemCounter<String>();
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
		ItemCounter<String> ic = new ItemCounter<String>();
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
		ItemCounter<String> ic = new ItemCounter<String>();
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
		ItemCounter<String> ic = new ItemCounter<String>();
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
	public void testSize() {
		ItemCounter<String> ic = new ItemCounter<String>();
		assertTrue(ic.size() == 0);
		ic.set("a", 42);
		assertTrue(ic.size() == 1);
		ic.set("a", 42);
		assertTrue(ic.size() == 1);
		ic.increment("a");
		assertTrue(ic.size() == 1);
		ic.increment("b");
		assertTrue(ic.size() == 2);
		ic.set("a", 0);
		assertTrue(ic.size() == 1);
	}

	@Test
	public void testSortByValueKey() {
		ItemCounter<String> ic = new ItemCounter<String>();
		ic.increment("a");
		ic.increment("b");
		ic.increment("a");
		ic.increment("c");
		List<ItemCounter<String>.KeyValuePair> descendingList = ic.sortByValueKey(false); 
		assertTrue(descendingList.get(0).getKey().equals("a"));
		assertTrue(descendingList.get(1).getKey().equals("c"));
		assertTrue(descendingList.get(2).getKey().equals("b"));
		
		List<ItemCounter<String>.KeyValuePair> ascendingList = ic.sortByValueKey(true);
		assertTrue(ascendingList.get(2).getKey().equals("a"));
		assertTrue(ascendingList.get(1).getKey().equals("c"));
		assertTrue(ascendingList.get(0).getKey().equals("b"));
	}
	
	@Test
	public void testAsUnmodifiable() {
		ItemCounter<String> ic = new ItemCounter<String>();
		ic.increment("a");
		ic.increment("b");
		ic.increment("a");
		ic.increment("c");
		
		ItemCounter<String> uc = ic.asUnmodifiable();
		assertTrue(uc.get("a") == 2);
		assertTrue(uc.get("b") == 1);
		try {
			uc.increment("a");
			fail("unmodifiable ItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
		try {
			uc.set("a",5);
			fail("unmodifiable ItemCounter should throw an exception");
		} catch(UnsupportedOperationException e) {
			// pass
		}
	}
}