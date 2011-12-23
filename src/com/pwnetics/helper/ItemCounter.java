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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Use this to count objects and calculate simple statistics.
 * 
 * <p>To count objects, consider using {@link ItemCounter#increment(Object)}; this will automatically increment the count associated with the given object.
 * To query the count for an object, consider using {@link ItemCounter#get(Object)}; this will return the count associated with the given object.
 * For objects that have not been added to an ItemCounter, the reported count is always zero.
 * </p>
 * 
 * <p>An object's count will change when the object's count is {@link #set(Object, int)},
 * or when an object is passed to the {@link #increment(Object)} method such that the 
 * new object is equals() to the object already contained in the ItemCounter.
 * Counts are always non-negative.</p>   
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
	protected final Map<K, Integer> count;


	/**  Holds key-value pairs for {@link ItemCounter#sortByValueKey(boolean)}. */
	public class KeyValuePair {
		private final K key;
		private final Integer value;

		public KeyValuePair(Entry<K, Integer> entry) {
			this(entry.getKey(), entry.getValue());
		}

		public KeyValuePair(K key, Integer value) {
			this.key = key;
			this.value = value;
		}
		
		public K getKey() {
			return key;
		}
		
		public Integer getValue() {
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
	 * @param item item whose count to increment
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
	 * Get the sum of all item counts in this collection.
	 * @return the total sum of all item counts, zero if empty.
	 */
	public long sum() {
		long sum = 0L;
		for(Integer n : count.values()) {
			sum += n;
		}
		return sum;
	}
	
	
	/**
	 * Returns the key/value pair with the lowest value (and secondarily with the "lowest" key).
	 * Not stable; if there are multiple "different" items that compare as equal (for example, if the keys don't implement {@link Comparable}),
	 * then it is possible for multiple calls to this method to return multiple answers.
	 * @return key/value pair with the lowest value, or <null,0> if the counter is empty 
	 */
	public KeyValuePair min() {
		if(count.isEmpty()) {
			return new KeyValuePair(null,0);
		}
		KeyValuePair pair = null;
		ValueKeyAscendingComparator vc = new ValueKeyAscendingComparator();
		for(Entry<K, Integer> entry : count.entrySet()) {
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
	 * @return key/value pair with the lowest value, or <null,0> if the counter is empty 
	 */
	public KeyValuePair max() {
		if(count.isEmpty()) {
			return new KeyValuePair(null,0);
		}
		KeyValuePair pair = null;
		ValueKeyDescendingComparator vc = new ValueKeyDescendingComparator();
		for(Entry<K, Integer> entry : count.entrySet()) {
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
	 * Get the mean of all item counts in this collection.
	 * @return the mean of all item counts, or zero if empty.
	 */
	public double mean() {
		if(count.size() == 0) {
			return 0;
		}
		return sum()/(double)count.size();

		/*
		 * The code below calculates an incremental average, which can prevent overflow in the accumulating variable. 
		 * 
		 * In LaTeX:
		 * A_{i+1} = A_i + \frac{ x_{i+1} - A_i }{ i+1 }
		 */
		
//		double mean = 0;
//		int i = 1;
//		
//		for(Integer c : count.values()) {
//			mean += (c - mean)/i++;
//		}
//		
//		return mean;
	}


	/**
	 * Get the sample variance of all item counts in this collection.
	 * Use this variance if you desire an estimate of the variance of some population of items, of which the current ItemCounter contains only an incomplete sample.
	 * Otherwise, see {@link #variancePopulation()}.
	 * 
	 * @return the sample variance of all item counts, or zero if empty.
	 */
	public double variance() {
		if(count.size() < 2) {
			return 0;
		}
		
		double mean = mean();
		double var = 0.0;
		
		for(Integer n : count.values()) {
			double foo = mean - n;
			var += foo * foo;
		}
	
		return var / (count.size() - 1);
	}


	/**
	 * Get the population variance of all item counts in this collection.
	 * Use this variance if you desire to know only the variance of the items in this ItemCounter, otherwise see {@link #variance()}.
	 * 
	 * <p>There is little difference between the sample and population variance when there are many distinct items.
	 * When there are few items, the difference is pronounced.
	 * In general, it is probably better to use the sample variance {@link #variance()}.
	 * </p>
	 *  
	 * @return the population variance of all item counts, or zero if empty.
	 */
	public double variancePopulation() {
		if(count.size() < 2) {
			return 0;
		}

		double mean = mean();
		double var = 0.0;
		
		for(Integer n : count.values()) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / count.size();
	}

	
	/**
	 * Returns a list of key-value pairs that is sorted first by item counts, then by item comparisons if those objects implement {@link Comparable}.
	 * The returned list is not backed by the item counter, so consider it a snapshot of the item counts.
	 * 
	 * <p>This sort is not stable; if there are multiple "different" items that compare as equal (for example, if the keys don't implement {@link Comparable}),
	 * then it is possible for multiple calls to this method to return various orderings.
	 * </p>
	 *
	 * @param isAscending if true, will sort keys in ascending value; if false, will sort keys in descending value
	 * @return an unmodifiable sorted list
	 */
	public List<KeyValuePair> sortByValueKey(boolean isAscending) {
		Comparator<KeyValuePair> vc;
		if(isAscending) {
			vc = new ValueKeyAscendingComparator();
		} else {
			vc = new ValueKeyDescendingComparator();
		}
		List<KeyValuePair> sortedList = new ArrayList<ItemCounter<K>.KeyValuePair>();
		for(Entry<K, Integer> entry : count.entrySet()) {
			sortedList.add(new KeyValuePair(entry));
		}
		Collections.sort(sortedList, vc);
		return Collections.unmodifiableList(sortedList);
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
	 * Note that the returned object is just a view of this modifiable ItemCounter, so it can change if the original item counter is changed.
	 * @return an unmodifiable version of this object
	 */
	public ItemCounter<K> asUnmodifiable() {
		return new UnmodifiableItemCounter(this);
	}
	
	
	/**
	 * Wraps the item counter and prevents modification, although the backing item counter can still be modified.
	 * @author romanows
	 */
	protected class UnmodifiableItemCounter extends ItemCounter<K> {
		private final ItemCounter<K> itemCounter;
		
		public UnmodifiableItemCounter(ItemCounter<K> itemCounter) {
			this.itemCounter = itemCounter;
		}
		
		@Override
		public Integer get(K item) {return itemCounter.get(item);}

		@Override
		public void set(K item, int count) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int increment(K item) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long sum() {return itemCounter.sum();}

		@Override
		public double mean() {return itemCounter.mean();}

		@Override
		public double variance() {return itemCounter.variance();}

		@Override
		public double variancePopulation() {return itemCounter.variancePopulation();}

		@Override
		public List<KeyValuePair> sortByValueKey(boolean isAscending) {return itemCounter.sortByValueKey(isAscending);}

		@Override
		public ItemCounter<Integer> countOfCounts() {return itemCounter.countOfCounts();}

		@Override
		public int size() {return itemCounter.size();}

		@Override
		public Set<K> getItems() {return itemCounter.getItems();}

		@Override
		public Map<K, Integer> getMap() {return itemCounter.getMap();}

		@Override
		public String toCSV() {return itemCounter.toCSV();}

		@Override
		public String toCSV(String columnDelimiter, String rowDelimiter) {return itemCounter.toCSV(columnDelimiter, rowDelimiter);}
		
		@Override
		public void writeCSV(Writer writer) throws IOException {itemCounter.writeCSV(writer);}
		
		@Override
		public void writeCSV(Writer writer, String columnDelimiter, String rowDelimiter) throws IOException {itemCounter.writeCSV(writer, columnDelimiter, rowDelimiter);}
		
		@Override
		public ItemCounter<K> asUnmodifiable() {return itemCounter.asUnmodifiable();}
	}
}