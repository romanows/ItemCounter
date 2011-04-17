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
 * Accumulates values associated with items.
 * 
 * <p>The behavior of this class differs conceptually from {@link ItemCounter}.
 * In {@link ItemCounter}, any object that is not contained in the {@link ItemCounter} 
 * would be reported to have a count of zero.
 * In the {@link ItemDoubleAccumulator}, an object that has never been added to the 
 * accumulator will cause {@link #get(Object)} to return a null.
 * An object that has been added with a value of zero 
 * will cause {@link #get(Object)} to return zero.</p>
 * 
 * @author romanows
 *
 * @param <K> the type of object being used as the item
 */
public class ItemDoubleAccumulator<K extends Object> {

	/** (item, value) */
	protected Map<K, Double> acc;


	/**
	 * Use with a map to compare by value instead of by key.
	 * @author romanows
	 */
	protected class ValueAscendingComparator implements Comparator<Map.Entry<K, Double>> {
		@Override
		public int compare(Entry<K, Double> a, Entry<K, Double> b) {
			return a.getValue().compareTo(b.getValue());
		}
	}


	/**
	 * Use with a map to compare by value instead of by key.
	 * @author romanows
	 */
	protected class ValueDescendingComparator implements Comparator<Map.Entry<K, Double>> {
		@Override
		public int compare(Entry<K, Double> a, Entry<K, Double> b) {
			return b.getValue().compareTo(a.getValue());
		}
	}


	/**
	 * Use with a map to compare first by value and second by key.
	 * Note that keys must implement the Comparable interface.
	 * @author romanows
	 */
	protected class KeyValueAscendingComparator implements Comparator<K> {
		protected Map<K, Double> base;

		public KeyValueAscendingComparator(Map<K, Double> base) {
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
		protected Map<K, Double> base;

		public KeyValueDescendingComparator(Map<K, Double> base) {
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
	public ItemDoubleAccumulator() {
		acc = new HashMap<K, Double>();
	}


	/**
	 * Get the accumulated value of an item
	 * @param item the item whose accumulated value will be returned
	 * @return the item's accumulated value or null if the requested item has never been added to this set
	 */
	public Double get(K item) {
		return acc.get(item);
	}


	/**
	 * Set the value for a given item.
	 * @param item an item
	 * @param value the value for that item
	 */
	public void set(K item, double value) {
		this.acc.put(item,value);
	}


	/**
	 * Add the value to the accumulated value for an item.
	 * @param item the item whose value to accumulate
	 * @return the new value of the given item
	 */
	public double add(K item, double value) {
		Double c = acc.get(item);
		if(c == null) {
			c = value;
		} else {
			c += value;
		}
		acc.put(item, c);

		return c;
	}


	/**
	 * Add the items and accumulated values from another {@link ItemDoubleAccumulator}.
	 * @param a another {@link ItemDoubleAccumulator}
	 */
	public void add(ItemDoubleAccumulator<K> a) {
		if(this == a) {
			throw new IllegalArgumentException();
		}
		for(K k : a.getItems()) {
			add(k, a.get(k));
		}
	}


	/**
	 * Get the total sum of all item counts in this collection.
	 * @return the total sum of all item counts, or null if nothing has been accumulated.
	 */
	public Double sum() {
		if(acc.isEmpty()) {
			return null;
		}
		return sum(acc.values());
	}


	/**
	 * Sum all elements of the given collection.
	 * @param c some collection
	 * @return the sum of the collection or zero if the collection is empty
	 * @throws NullPointerException if an element is null
	 */
	protected static double sum(Collection<Double> c) {
		Double sum = 0.0;
		for(Double n : c) {
			sum += n;
		}
		return sum;
	}


	/**
	 * Sum all elements of the given collection with the Kahan summation algorithm.
	 * The Kahan algorithm reduces sum errors caused by adding floating point numbers that vary significantly in magnitude.
	 * See http://en.wikipedia.org/w/index.php?title=Kahan_summation_algorithm&oldid=407779143
	 *
	 * This should be slightly slower than {@link #sum(Collection)}.
	 * For most sums, use of double prevents many round-off errors, and this method is not needed.
	 * Also, there are more accurate algorithms than this one (see discussion on Wikipedia).
	 * This would likely be more useful when summing collections of lower-precision floats.
	 *
	 * @param c some collection
	 * @return the sum of the collection or zero if the collection is empty
	 * @throws NullPointerException if an element is null
	 */
	protected static double sumKahan(Collection<Double> c) {
		Double sum = 0.0;
		double compensate = 0.0;
		for(Double n : c) {
			double y = n - compensate; // Next number to add, with correction
			double t = sum + y; // Running sum, which may lose the lower bits introduced by a small y.
			compensate = (t - sum) - y; // Recover what was added from y, then subtract to leave what _wasn't_ added to y (this will be negative, which is why we subtract the compensation value, above).
			sum = t;
		}
		return sum;
	}


	/**
	 * Get the mean of all item counts in this collection.
	 * @return the mean of all item counts, or zero if empty.
	 */
	public double mean() {
		return mean(acc.values());
	}


	/**
	 * Average elements in a collection.
	 * @param c some collection
	 * @return the average of the elements
	 */
	protected static double mean(Collection<Double> c) {
		return sum(c)/c.size();
	}


	/**
	 * Get the variance of all item counts in this collection.
	 * @return the varniance of all item counts, or zero if empty.
	 */
	public double variance() {
		return variance(acc.values());
	}


	/**
	 * Compute the variance over elements in a collection.
	 * @param c some collection
	 * @return the variance over elements
	 */
	protected static double variance(Collection<Double> c) {
		double mean = mean(c);
		double var = 0.0;
		for(Double n : c) {
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
	 * @return an unmodifiable List of (Key, Accumulated) entries, sorted by the Accumulated value only.
	 */
	public List<Map.Entry<K, Double>> sortByValue(boolean isAscending) {
		Comparator<Map.Entry<K, Double>> vc;
		if(isAscending) {
			vc = new ValueAscendingComparator();
		} else {
			vc = new ValueDescendingComparator();
		}
		List<Map.Entry<K, Double>> sortedList = new ArrayList<Map.Entry<K, Double>>();
		sortedList.addAll(acc.entrySet());
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
	public Map<K, Double> sortByKeyValue(boolean isAscending) {
		Comparator<K> vc;
		if(isAscending) {
			vc = new KeyValueAscendingComparator(acc);
		} else {
			vc = new KeyValueDescendingComparator(acc);
		}
		TreeMap<K, Double> sortedMap = new TreeMap<K, Double>(vc);
		sortedMap.putAll(acc);
		return Collections.unmodifiableMap(sortedMap);
	}


	/**
	 * Get the number of distinct items that have been added to this accumulator.
	 * Includes items with zero values.
	 * @return the number of distinct items
	 */
	public int size() {
		return acc.size();
	}


	/**
	 * Get all items in this counter.
	 * @return all item keys
	 */
	public Set<K> getItems() {
		return Collections.unmodifiableSet(acc.keySet());
	}


	/**
	 * Get a map of the items to their accumulated values
	 * @return an unmodifiable item to accumulated value map
	 */
	public Map<K, Double> getMap() {
		return Collections.unmodifiableMap(acc);
	}


	/**
	 * Get contents in a CSV (tab-separated) format.
	 * Constructed as "key.toString()\tvalue\n"
	 * @return the contents of this item accumulator
	 */
	public String toCSV() {
		return toCSV("\t","\n");
	}


	/**
	 * Get contents in a CSV-like format.
	 * Constructed as key.toString() + columnDelimiter + value + rowDelimiter
	 * @return the contents of this item accumulator
	 */
	public String toCSV(String columnDelimiter, String rowDelimiter) {
		StringBuilder sb = new StringBuilder();
		for(K k : getItems()) {
			sb.append(k);
			sb.append(columnDelimiter);
			sb.append(acc.get(k));
			sb.append(rowDelimiter);
		}
		return sb.toString();
	}


	/**
	 * Write contents in a CSV (tab-separated) format.
	 * Constructed as "key.toString()\tvalue\n".
	 * Does not close the writer.
	 * @throws IOException
	 */
	public void writeCSV(Writer writer) throws IOException {
		writeCSV(writer,"\t","\n");
	}


	/**
	 * Write contents in a CSV-like format.
	 * Constructed as key.toString() + columnDelimiter + value + rowDelimiter
	 * Does not close the writer.
	 * @throws IOException
	 */
	public void writeCSV(Writer writer, String columnDelimiter, String rowDelimiter) throws IOException {
		for(K k : getItems()) {
			writer.write(k.toString());
			writer.write(columnDelimiter);
			writer.write(acc.get(k).toString());
			writer.write(rowDelimiter);
		}
	}


	/**
	 * Get a view of this as an unmodifiable object.
	 * Methods {@link #add(ItemDoubleAccumulator)}, {@link #add(Object, double)}, and {@link #set(Object, double)} will throw {@link UnsupportedOperationException} if called.
	 * @return an unmodifiable version of this object
	 */
	public ItemDoubleAccumulator<K> asUnmodifiable() {
		return new ItemDoubleAccumulator<K>() {
			@Override
			public Double get(K item) {return super.get(item);}

			@Override
			public void set(K item, double value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public double add(K item, double value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(ItemDoubleAccumulator<K> a) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Double sum() {return super.sum();}

			@Override
			public double mean() {return super.mean();}

			@Override
			public double variance() {return super.variance();}

			@Override
			public List<Map.Entry<K, Double>> sortByValue(boolean isAscending) {return super.sortByValue(isAscending);}

			@Override
			public Map<K, Double> sortByKeyValue(boolean isAscending) {return super.sortByKeyValue(isAscending);}

			@Override
			public int size() {return super.size();}

			@Override
			public Set<K> getItems() {return super.getItems();}

			@Override
			public Map<K, Double> getMap() {return super.getMap();}

			@Override
			public String toCSV() {return super.toCSV();}

			@Override
			public String toCSV(String columnDelimiter, String rowDelimiter) {return super.toCSV(columnDelimiter, rowDelimiter);}
		};
	}
}