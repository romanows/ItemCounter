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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * Collection of items and their positive counts.
 * 
 * <p>Queries about objects that have not been added will return a count of zero, 
 * indicating they are not contained in this object.</p>
 * 
 * <p>An object's count will change when the object's count is {@link #set(Object, int)},
 * or when an object is passed to the {@link #increment(Object)} method such that the 
 * new object is equals() to the object already contained in the ItemCounter.</p>   
 *
 * @author romanows
 *
 * @param <K> the type of object being counted
 */
public class ItemCounter<K extends Object> {
	/*
	 * The Apache Collections library has a Bag type that may have much of the same functionality.
	 * Google Guava also has some nice bag and collections types.
	 */


	/** (item, count) */
	protected Map<K, Integer> count;


	/**
	 * Use with a map to compare by value instead of by key.
	 * @author romanows
	 */
	protected class ValueAscendingComparator implements Comparator<Map.Entry<K, Integer>> {
		@Override
		public int compare(Entry<K, Integer> a, Entry<K, Integer> b) {
			return a.getValue().compareTo(b.getValue());
		}
	}


	/**
	 * Use with a map to compare by value instead of by key.
	 * @author romanows
	 */
	protected class ValueDescendingComparator implements Comparator<Map.Entry<K, Integer>> {
		@Override
		public int compare(Entry<K, Integer> a, Entry<K, Integer> b) {
			return b.getValue().compareTo(a.getValue());
		}
	}


	/**
	 * Use with a map to compare first by value and second by key.
	 * Note that keys must implement the Comparable interface.
	 * @author romanows
	 */
	protected class KeyValueAscendingComparator implements Comparator<K> {
		protected Map<K, Integer> base;

		public KeyValueAscendingComparator(Map<K, Integer> base) {
			this.base = base;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(K a, K b) {
			int c = base.get(a).compareTo(base.get(b));
			if(c == 0) {
				return ((Comparable<K>)a).compareTo(b);
			}
			return c;
		}
	}


	/**
	 * Use with a map to compare first by value and second by key.
	 * Note that keys must implement the Comparable interface.
	 * @author romanows
	 */
	protected class KeyValueDescendingComparator implements Comparator<K> {
		protected Map<K, Integer> base;

		public KeyValueDescendingComparator(Map<K, Integer> base) {
			this.base = base;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(K a, K b) {
			int c = base.get(b).compareTo(base.get(a));
			if(c == 0) {
				return ((Comparable<K>)b).compareTo(a);
			}
			return c;
		}
	}


	/**
	 * Constructor.
	 */
	public ItemCounter() {
		count = new HashMap<K, Integer>();
	}


	/**
	 * Get the count of an item
	 * @param item the item whose count will be returned
	 * @return the item's count
	 */
	public Integer get(K item) {
		Integer c = count.get(item);
		if(c == null) {
			return 0;
		} else {
			return c;
		}
	}


	/**
	 * Set the count value for a given item.
	 * @param item an item
	 * @param count
	 */
	public void set(K item, int count) {
		if(count < 0) {
			throw new IllegalArgumentException("count must be non-negative");
		}
		Integer c = this.count.get(item);
		if(count == 0) {
			if(c != null) {
				this.count.remove(item);
			} else {
				// Do nothing; setting an item with zero count is the same as not adding it
			}
		} else {
			this.count.put(item,count);
		}
	}


	/**
	 * Increment the count on an item.
	 * @param item the item whose count to increment
	 * @return the new count of the given item
	 */
	public int increment(K item) {
		Integer c = count.get(item);
		if(c == null) {
			c = 1;
		} else {
			c += 1;
		}
		count.put(item, c);
		return c;
	}


	/**
	 * Get the total sum of all item counts in this collection.
	 * @return the total sum of all item counts, or zero if empty.
	 */
	public int sum() {
		return sumInt(count.values());
	}

	
	/**
	 * Sum all elements of the given collection.
	 * @param c some collection
	 * @return the sum of the collection or zero if the collection is empty
	 * @throws NullPointerException if an element is null
	 */
	protected static int sumInt(Collection<Integer> c) {
		Integer sum = 0;
		for(Integer n : c) {
			sum += n;
		}
		return sum;
	}


	/**
	 * Get the mean of all item counts in this collection.
	 * @return the mean of all item counts, or zero if empty.
	 */
	public double mean() {
		return meanInt(count.values());
	}


	/**
	 * Average elements in a collection.
	 * @param c some collection
	 * @return the average of elements
	 */
	protected static double meanInt(Collection<Integer> c) {
		return sumInt(c)/(double)c.size();
	}


	/**
	 * Get the variance of all item counts in this collection.
	 * @return the variance of all item counts, or zero if empty.
	 */
	public double variance() {
		return varianceInt(count.values());
	}


	/**
	 * Compute the variance over elements in a collection.
	 * @param c some collection
	 * @return the variance over elements
	 */
	protected static double varianceInt(Collection<Integer> c) {
		double mean = meanInt(c);
		double var = 0.0;
		for(Integer n : c) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / c.size();
	}


	/**
	 * Returns a representation of the count map with the keys sorted by their value.
	 * This is particularly useful when the key does not implement the Comparable interface.
	 * @see #sortByKeyValue(boolean)
	 * @param isAscending if true, will sort keys in ascending value; if false, will sort keys in descending value
	 * @return an unmodifiable List of (Key, Count) entries, sorted by the Count only.
	 */
	public List<Map.Entry<K, Integer>> sortByValue(boolean isAscending) {
		Comparator<Map.Entry<K, Integer>> vc;
		if(isAscending) {
			vc = new ValueAscendingComparator();
		} else {
			vc = new ValueDescendingComparator();
		}
		List<Map.Entry<K, Integer>> sortedList = new ArrayList<Map.Entry<K, Integer>>();
		sortedList.addAll(count.entrySet());
		Collections.sort(sortedList, vc);
		return Collections.unmodifiableList(sortedList);
	}


	/**
	 * Returns a representation of the count map with the keys sorted by their value.
	 * Keys <b>must</b> implement the Comparable interface.
	 * @see #sortByValue(boolean)
	 * @param isAscending if true, will sort keys in ascending value; if false, will sort keys in descending value
	 * @return an unmodifiable sorted TreeMap
	 */
	public Map<K, Integer> sortByKeyValue(boolean isAscending) {
		Comparator<K> vc;
		if(isAscending) {
			vc = new KeyValueAscendingComparator(count);
		} else {
			vc = new KeyValueDescendingComparator(count);
		}
		TreeMap<K, Integer> sortedMap = new TreeMap<K, Integer>(vc);
		sortedMap.putAll(count);
		return Collections.unmodifiableMap(sortedMap);
	}


	/**
	 * Get the count of the different counts in this item counter.
	 * @return the count of counts in this item counter
	 */
	public ItemCounter<Integer> countOfCounts() {
		ItemCounter<Integer> countOfCounts = new ItemCounter<Integer>();
		for(Integer c : count.values()) {
			countOfCounts.increment(c);
		}
		return countOfCounts;
	}


	/**
	 * Get the number of distinct items with non-zero count.
	 * @return the number of distinct items
	 */
	public int size() {
		return count.size();
	}


	/**
	 * Get all item keys in this counter.
	 * @return all item keys in an unmodifiable set
	 */
	public Set<K> getItems() {
		return Collections.unmodifiableSet(count.keySet());
	}


	/**
	 * Get a map of the items to their counts.
	 * @return an unmodifiable item to count map
	 */
	public Map<K, Integer> getMap() {
		return Collections.unmodifiableMap(count);
	}


	/**
	 * Get the contents of this item counter in a CSV (tab-separated) format.
	 * Constructed as "key.toString()\tcount\n"
	 * @return the contents of this item counter
	 */
	public String toCSV() {
		return toCSV("\t","\n");
	}


	/**
	 * Get the contents of this item counter in a CSV-like format.
	 * Constructed as key.toString() + columnDelimiter + count + rowDelimiter
	 * @return the contents of this item counter
	 */
	public String toCSV(String columnDelimiter, String rowDelimiter) {
		StringBuilder sb = new StringBuilder();
		for(K k : getItems()) {
			sb.append(k);
			sb.append(columnDelimiter);
			sb.append(count.get(k));
			sb.append(rowDelimiter);
		}
		return sb.toString();
	}


	/**
	 * Write the contents of this item counter in a CSV (tab-separated) format.
	 * Constructed as "key.toString()\tcount\n".
	 * Does not close the writer.
	 * @throws IOException
	 */
	public void writeCSV(Writer writer) throws IOException {
		writeCSV(writer,"\t","\n");
	}


	/**
	 * Write the contents of this item counter in a CSV-like format.
	 * Constructed as key.toString() + columnDelimiter + count + rowDelimiter.
	 * Does not close the writer.
	 * @throws IOException
	 */
	public void writeCSV(Writer writer, String columnDelimiter, String rowDelimiter) throws IOException {
		for(K k : getItems()) {
			writer.write(k.toString());
			writer.write(columnDelimiter);
			writer.write(count.get(k));
			writer.write(rowDelimiter);
		}
	}


	/**
	 * Get a view of this as an unmodifiable object.
	 * Methods {@link #increment(Object)} and {@link #set(Object, int)} will throw {@link UnsupportedOperationException} if called.
	 * @return an unmodifiable version of this object
	 */
	public ItemCounter<K> asUnmodifiable() {
		return new ItemCounter<K>() {

			@Override
			public int increment(K item) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(K item, int count) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer get(K item) {return super.get(item);}

			@Override
			public int sum() {return super.sum();}

			@Override
			public double mean() {return super.mean();}

			@Override
			public double variance() {return super.variance();}

			@Override
			public List<Map.Entry<K, Integer>> sortByValue(boolean isAscending) {return super.sortByValue(isAscending);}

			@Override
			public Map<K, Integer> sortByKeyValue(boolean isAscending) {return super.sortByKeyValue(isAscending);}

			@Override
			public ItemCounter<Integer> countOfCounts() {return super.countOfCounts();}

			@Override
			public int size() {return super.size();}

			@Override
			public Set<K> getItems() {return super.getItems();}

			@Override
			public Map<K, Integer> getMap() {return super.getMap();}

			@Override
			public String toCSV() {return super.toCSV();}

			@Override
			public String toCSV(String columnDelimiter, String rowDelimiter) {return super.toCSV(columnDelimiter, rowDelimiter);}
		};
	}
}