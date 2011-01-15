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


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

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
	public void testSortByValue() {
		ItemCounter<String> ic = new ItemCounter<String>();
		ic.increment("a");
		ic.increment("b");
		ic.increment("a");
		ic.increment("c");
		List<Entry<String, Integer>> descending = ic.sortByValue(false);
		assertTrue(descending.get(0).getKey().equals("a"));
		
		List<Entry<String, Integer>> ascending = ic.sortByValue(true);
		assertTrue(ascending.get(2).getKey().equals("a"));
	}

	@Test
	public void testSortByKeyValue() {
		ItemCounter<String> ic = new ItemCounter<String>();
		ic.increment("a");
		ic.increment("b");
		ic.increment("a");
		ic.increment("c");
		Map<String, Integer> descendingMap = ic.sortByKeyValue(false);
		List<Entry<String, Integer>> descending = new ArrayList<Entry<String, Integer>>(descendingMap.entrySet()); 
		assertTrue(descending.get(0).getKey().equals("a"));
		assertTrue(descending.get(1).getKey().equals("c"));
		assertTrue(descending.get(2).getKey().equals("b"));
		
		Map<String, Integer> ascendingMap = ic.sortByKeyValue(true);
		List<Entry<String, Integer>> ascending = new ArrayList<Entry<String, Integer>>(ascendingMap.entrySet());
		assertTrue(ascending.get(2).getKey().equals("a"));
		assertTrue(ascending.get(1).getKey().equals("c"));
		assertTrue(ascending.get(0).getKey().equals("b"));
	}
}
