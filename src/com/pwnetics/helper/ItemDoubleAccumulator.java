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


/**
 * Use this to accumulate floating point values associated with objects and calculate simple statistics.
 *
 * <p>To accumulate objects, consider using {@link ItemDoubleAccumulator#add(Object, double)}; this will automatically add the value associated with the given object, introducing it into the collection if necessary.
 * To query the accumulated value for an object, consider using {@link ItemDoubleAccumulator#get(Object)}; this will return the value associated with the given object.
 * For objects that have not been added to an ItemCounter, the reported count is always null.
 * </p>

 * <p>This class behaves differently from {@link ItemCounter}.
 * When {@link ItemDoubleAccumulator#get(Object)} is called for an object that was never added to {@link ItemDoubleAccumulator}, a "null" is returned.
 * This allows {@link ItemDoubleAccumulator} to accumulate values that wind up at 0.0, and also start accumulating at 0.0 when new objects are introduced.
 * </p>
 *
 * @author romanows
 *
 * @param <K> the type of object being used as the item
 */
public class ItemDoubleAccumulator<K extends Object> {

	/** (item, value) */
	protected final Map<K, Double> acc;


	/**  Holds key-value pairs for {@link ItemDoubleAccumulator#sortByValueKey(boolean)}. */
	public class KeyValuePair {
		private final K key;
		private final Double value;

		public KeyValuePair(Entry<K, Double> entry) {
			this(entry.getKey(), entry.getValue());
		}

		public KeyValuePair(K key, Double value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public Double getValue() {
			return value;
		}
	}


	/**
	 * Use with a map to compare first by value and second by key, if keys implement {@link Comparable}.
	 * @author romanows
	 */
	protected class ValueKeyAscendingComparator implements Comparator<KeyValuePair> {
		@Override
		public int compare(KeyValuePair a, KeyValuePair b) {
			int c = a.getValue().compareTo(b.getValue());
			if(c == 0) {
				if(a.getKey() instanceof Comparable<?>) {
					@SuppressWarnings("unchecked")
					Comparable<K> x = (Comparable<K>)a.getKey();
					return x.compareTo(b.getKey());
				}
			}
			return c;
		}
	}


	/**
	 * Use with a map to compare first by value and second by key, if keys implement {@link Comparable}.
	 * @author romanows
	 */
	protected class ValueKeyDescendingComparator implements Comparator<KeyValuePair> {
		@Override
		public int compare(KeyValuePair a, KeyValuePair b) {
			int c = b.getValue().compareTo(a.getValue());
			if(c == 0) {
				if(a.getKey() instanceof Comparable<?>) {
					@SuppressWarnings("unchecked")
					Comparable<K> x = (Comparable<K>)b.getKey();
					return x.compareTo(a.getKey());
				}
			}
			return c;
		}
	}


	/** Constructor */
	public ItemDoubleAccumulator() {
		acc = new HashMap<K, Double>();
	}


	/**
	 * Constructor.
	 * Allows use of an already-built map from keys to their accumulation values.
	 * Note that this map is used as is, without validating the map (it is possible it has zero or negative count values).
	 *
	 * @param acc an already-built map from keys to their accumulated values
	 * @param isCopying if false, directly references the given acc map; if true, will create a copy of the given acc map.
	 *   Note that copying the map is the safest usage, because it forces the modification of the map to only occur via
	 *   methods on this object.  The downside is that (at least) twice the memory is required to make the copy.
	 */
	protected ItemDoubleAccumulator(Map<K, Double> acc, boolean isCopying) {
		if(isCopying) {
			this.acc = new HashMap<K, Double>(acc);
		} else {
			this.acc = acc;
		}
	}


	/**
	 * Get the accumulated value of an item.
	 * @param item item whose accumulated value will be returned
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
	 * Get the total sum of all item values in this collection.
	 * @return the total sum of all item values, or null if nothing has been accumulated.
	 */
	public Double sum() {
		if(acc.isEmpty()) {
			return null;
		}
		Double sum = 0.0;
		for(Double n : acc.values()) {
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
	 * Returns the key/value pair with the lowest value (and secondarily with the "lowest" key).
	 * Not stable; if there are multiple "different" items that compare as equal (for example, if the keys don't implement {@link Comparable}),
	 * then it is possible for multiple calls to this method to return multiple answers.
	 * @return key/value pair with the lowest value, or <null,null> if the accumulator is empty
	 */
	public KeyValuePair min() {
		if(acc.isEmpty()) {
			return new KeyValuePair(null,null);
		}
		KeyValuePair pair = null;
		ValueKeyAscendingComparator vc = new ValueKeyAscendingComparator();
		for(Entry<K, Double> entry : acc.entrySet()) {
			KeyValuePair entryPair = new KeyValuePair(entry);
			if(pair == null) {
				pair = entryPair;
			} else {
				if(vc.compare(pair, entryPair) > 0) {
					pair = entryPair;
				}
			}
		}
		return pair;
	}


	/**
	 * Returns the key/value pair with the largest value (and secondarily with the "largest" key).
	 * Not stable; if there are multiple "different" items that compare as equal (for example, if the keys don't implement {@link Comparable}),
	 * then it is possible for multiple calls to this method to return multiple answers.
	 * @return key/value pair with the lowest value, or <null,null> if the accumulator is empty
	 */
	public KeyValuePair max() {
		if(acc.isEmpty()) {
			return new KeyValuePair(null,null);
		}
		KeyValuePair pair = null;
		ValueKeyDescendingComparator vc = new ValueKeyDescendingComparator();
		for(Entry<K, Double> entry : acc.entrySet()) {
			KeyValuePair entryPair = new KeyValuePair(entry);
			if(pair == null) {
				pair = entryPair;
			} else {
				if(vc.compare(pair, entryPair) > 0) {
					pair = entryPair;
				}
			}
		}
		return pair;
	}


	/**
	 * Get the mean of all item values in this collection.
	 * @return the mean of all item values, or null if empty.
	 */
	public Double mean() {
		if(acc.isEmpty()) {
			return null;
		}
		return sum()/acc.size();
	}


	/**
	 * Get the sample variance of all item values in this collection.
	 * Use this variance if you desire an estimate of the variance of some population of items, of which the current ItemDoubleAccumulator contains only an incomplete sample.
	 * Otherwise, see {@link #variancePopulation()}.
	 *
	 * @return the sample variance of all item values, or null if empty.
	 */
	public Double variance() {
		if(acc.isEmpty()) {
			return null;
		}
		if(acc.size() < 2) {
			return 0.0;
		}
		double mean = mean();
		double var = 0.0;
		for(Double n : acc.values()) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / (acc.size()-1);
	}


	/**
	 * Get the population variance of all item values in this collection.
	 * Use this variance if you desire to know only the variance of the items in this ItemDoubleAccumulator only, otherwise see {@link #variance()}.
	 *
	 * <p>There is little difference between the sample and population variance when there are many distinct items.
	 * When there are few items, the difference is pronounced.
	 * In general, it is probably better to use the sample variance {@link #variance()}.
	 * </p>
	 *
	 * @return the population variance of all item values, or null if empty.
	 */
	public Double variancePopulation() {
		if(acc.isEmpty()) {
			return null;
		}
		double mean = mean();
		double var = 0.0;
		for(Double n : acc.values()) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / (acc.size());
	}


	/**
	 * Returns a representation of the item-to-accumulated-value map with the keys sorted by their value, and then sorted by keys if the keys implement {@link Comparable}.
	 *
	 * <p>This sort is not stable; if there are multiple "different" items that compare as equal (for example, if the keys don't implement {@link Comparable}),
	 * then it is possible for multiple calls to this method to return various orderings.
	 * </p>
	 *
	 * @param isAscending if true, will sort values and keys in ascending value; if false, will sort values and keys in descending value
	 * @return an unmodifiable sorted list
	 */
	public List<KeyValuePair> sortByValueKey(boolean isAscending) {
		Comparator<KeyValuePair> vc;
		if(isAscending) {
			vc = new ValueKeyAscendingComparator();
		} else {
			vc = new ValueKeyDescendingComparator();
		}
		List<KeyValuePair> sortedList = new ArrayList<ItemDoubleAccumulator<K>.KeyValuePair>();
		for(Entry<K, Double> entry : acc.entrySet()) {
			sortedList.add(new KeyValuePair(entry));
		}
		Collections.sort(sortedList, vc);
		return Collections.unmodifiableList(sortedList);
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
	 * Get all items in this accumulator.
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
	 * Note that the returned object is just a view of this modifiable ItemDoubleAccumulator, so the new "unmodifiable" object can actually be changed by modifying the original accumulator.
	 * @return an unmodifiable version of this object
	 */
	public ItemDoubleAccumulator<K> asUnmodifiable() {
		return new UnmodifiableItemDoubleAccumulator(this, false);
	}


	/**
	 * Wraps the item counter and prevents modification, although the backing item accumulator can still be modified.
	 * @author romanows
	 */
	protected class UnmodifiableItemDoubleAccumulator extends ItemDoubleAccumulator<K> {

		public UnmodifiableItemDoubleAccumulator(ItemDoubleAccumulator<K> acc, boolean isCopying) {
			super(acc.acc, isCopying);
		}

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
	}
}