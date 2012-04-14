/*
Copyright 2012 Brian Romanowski. All rights reserved.

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

import java.util.Map;


/**
 * An {@link ItemDoubleAccumulator} that caches values for some of the moderately computationally expensive method calls.
 *
 * <p>On the first call to a cacheable method, or whenever the accumulator has changed, the original expensive method will be called.
 * Thereafter, until the accumulator is changed, the cached value will be returned.
 * </p>
 *
 * <p>While it is possible to use this class as a mutable item accumulator, every set or add operation incurs a cache-invalidation cost.
 * The recommended usage is to build the object data using the normal {@link ItemDoubleAccumulator}, then use {@link #build(ItemDoubleAccumulator, boolean)} to produce an object
 * of this class for analysis purposes.
 * </p>
 *
 * @author romanows
 *
 * @param <K> the type of object being accumulated
 */
public class CachingItemDoubleAccumulator<K> extends ItemDoubleAccumulator<K> {

	private boolean isSomethingCached;
	private Double sum;
	private KeyValuePair min;
	private KeyValuePair max;
	private Double mean;
	private Double variance;
	private Double variancePopulation;


	/**
	 * Factory method, recommended way to create a {@link CachingItemDoubleAccumulator}.
	 * One can quickly build an accumulator using the non-caching {@link ItemDoubleAccumulator}, then use this to create a {@link CachingItemDoubleAccumulator} with which to perform analysis.
	 *
	 * <p>Accumulated data is shared with the given {@link ItemDoubleAccumulator}, which can be a source of bugs.
	 * Modifications to the given item accumulator can cause the caching item accumulator to return inconsistent values.
	 * To guard against this, one can set isCopying to true, which makes the caching item accumulator independent from the given item accumulator.
	 * </p>
	 *
	 * @param a {@link ItemDoubleAccumulator} to "convert" to a {@link CachingItemDoubleAccumulator}
	 * @param isCopying if false, references the internal state of the given item accumulator; if true, will become independent of the future operations performed on the given item accumulator.
	 *   Setting this to "true" is safest, but does incur a memory storage and copy cost.
	 * @return a new instance of a caching item accumulator that is either a dependent view or an independent snapshot of the given item accumulator
	 */
	public static <K> CachingItemDoubleAccumulator<K> build(ItemDoubleAccumulator<K> a, boolean isCopying) {
		return new CachingItemDoubleAccumulator<K>(a.acc, isCopying);
	}

	/**
	 * Constructor.
	 * Consider using {@link #build(ItemDoubleAccumulator, boolean)}, instead.
	 */
	public CachingItemDoubleAccumulator() {
		super();
		invalidate();
	}

	protected CachingItemDoubleAccumulator(Map<K, Double> acc, boolean isCopying) {
		super(acc, isCopying);
		invalidate();
	}

	/** Marks all cacheable values as invalid. */
	protected void invalidate() {
		sum = null;
		min = null;
		max = null;
		mean = null;
		variance = null;
		variancePopulation = null;
		isSomethingCached = false;
	}

	/**
	 * Loads all cacheable values, so this is fairly computationally expensive.
	 * However, future calls to cacheable methods won't incur an initial performance hit, so long as the item accumulator is not modified after calling this cacheAll() method.
	 */
	public void cacheAll() {
		if(sum == null) {
			sum();
		}
		if(min == null) {
			min();
		}
		if(max == null) {
			max();
		}
		if(mean == null) {
			mean();
		}
		if(variance == null) {
			variance();
		}
		if(variancePopulation == null) {
			variancePopulation();
		}
	}

	@Override
	public void set(K item, double value) {
		if(isSomethingCached) {
			invalidate();
		}
		super.set(item, value);
	}

	@Override
	public double add(K item, double value) {
		if(isSomethingCached) {
			invalidate();
		}
		return super.add(item, value);
	}

	@Override
	public void add(ItemDoubleAccumulator<K> a) {
		if(isSomethingCached) {
			invalidate();
		}
		super.add(a);
	}

	@Override
	public Double sum() {
		if(sum == null) {
			sum = super.sum();
			isSomethingCached = true;
		}
		return sum;
	}

	@Override
	public KeyValuePair min() {
		if(min == null) {
			min = super.min();
			isSomethingCached = true;
		}
		return min;
	}

	@Override
	public KeyValuePair max() {
		if(max == null) {
			max = super.max();
			isSomethingCached = true;
		}
		return max;
	}

	@Override
	public Double mean() {
		if(mean == null) {
			mean = super.mean();
			isSomethingCached = true;
		}
		return mean;
	}

	@Override
	public Double variance() {
		if(variance == null) {
			variance = super.variance();
			isSomethingCached = true;
		}
		return variance;
	}

	@Override
	public Double variancePopulation() {
		if(variancePopulation == null) {
			variancePopulation = super.variancePopulation();
			isSomethingCached = true;
		}
		return variancePopulation;
	}

	@Override
	public ItemDoubleAccumulator<K> asUnmodifiable() {
		return new UnmodifiableCachingItemDoubleAccumulator(this, false);
	}

	/**
	 * Wraps the item accumulator and prevents modification, although the backing item accumulator can still be modified.
	 * @author romanows
	 */
	protected class UnmodifiableCachingItemDoubleAccumulator extends CachingItemDoubleAccumulator<K> {
		public UnmodifiableCachingItemDoubleAccumulator(CachingItemDoubleAccumulator<K> a, boolean isCopying) {
			super(a.acc, isCopying);
			sum = a.sum;
			min = a.min;
			max = a.max;
			mean = a.mean;
			variance = a.variance;
			variancePopulation = a.variancePopulation;
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