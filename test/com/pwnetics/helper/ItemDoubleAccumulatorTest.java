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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

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
	public void testIncrement() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		assertTrue(ic.get("a") == null);
		assertTrue(ic.add("a", 0.25) == 0.25);
		assertTrue(ic.add("a", 0.5) == 0.25 + 0.5);
		assertTrue(ic.add("a", -0.5) == 0.25);
		ic.set("a", 42);
		assertTrue(ic.add("a",0.42) == 42.0 + 0.42);
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
	public void testSortByValue() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		ic.add("a", 1.0);
		ic.add("b", 1.0);
		ic.add("a", 1.0);
		ic.add("c", 1.0);
		List<Entry<String, Double>> descending = ic.sortByValue(false);
		assertTrue(descending.get(0).getKey().equals("a"));
		List<Entry<String, Double>> ascending = ic.sortByValue(true);
		assertTrue(ascending.get(2).getKey().equals("a"));
	}

	@Test
	public void testSortByKeyValue() {
		ItemDoubleAccumulator<String> ic = new ItemDoubleAccumulator<String>();
		ic.add("a", 1.0);
		ic.add("b", 1.0);
		ic.add("a", 1.0);
		ic.add("c", 1.0);
		Map<String, Double> descendingMap = ic.sortByKeyValue(false);
		List<Entry<String, Double>> descending = new ArrayList<Entry<String, Double>>(descendingMap.entrySet()); 
		assertTrue(descending.get(0).getKey().equals("a"));
		assertTrue(descending.get(1).getKey().equals("c"));
		assertTrue(descending.get(2).getKey().equals("b"));
		
		Map<String, Double> ascendingMap = ic.sortByKeyValue(true);
		List<Entry<String, Double>> ascending = new ArrayList<Entry<String, Double>>(ascendingMap.entrySet());
		assertTrue(ascending.get(2).getKey().equals("a"));
		assertTrue(ascending.get(1).getKey().equals("c"));
		assertTrue(ascending.get(0).getKey().equals("b"));
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
			normalSum.add(ItemDoubleAccumulator.sum(c));
			saferSum.add(ItemDoubleAccumulator.sumKahan(c));
			Collections.shuffle(c,shuffleRnd);
		}
//		System.out.println((CollectionMath.variance(normalSum) - 7e-20) + "\t" + CollectionMath.variance(saferSum));
		assertTrue(ItemDoubleAccumulator.variance(normalSum) - 7e-20 > ItemDoubleAccumulator.variance(saferSum));
	}
}
