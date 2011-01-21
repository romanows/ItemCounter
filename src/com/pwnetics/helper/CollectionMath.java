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

import java.util.Collection;

/**
 * Miscellaneous math-related methods for collections.
 * @author romanows
 */
public class CollectionMath {

	/**
	 * Sum all elements of the given collection.
	 * @param c some collection
	 * @return the sum of the collection or zero if the collection is empty
	 * @throws NullPointerException if an element is null
	 */
	public static int sumInt(Collection<Integer> c) {
		Integer sum = 0;
		for(Integer n : c) {
			sum += n;
		}
		return sum;
	}


	/**
	 * Sum all elements of the given collection.
	 * @param c some collection
	 * @return the sum of the collection or zero if the collection is empty
	 * @throws NullPointerException if an element is null
	 */
	public static double sum(Collection<Double> c) {
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
	public static double sumKahan(Collection<Double> c) {
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
	 * Average elements in a collection.
	 * @param c some collection
	 * @return the average of elements
	 */
	public static double meanInt(Collection<Integer> c) {
		return sumInt(c)/(double)c.size();
	}


	/**
	 * Average elements in a collection.
	 * @param c some collection
	 * @return the average of the elements
	 */
	public static double mean(Collection<Double> c) {
		return sum(c)/c.size();
	}


	/**
	 * Compute the variance over elements in a collection.
	 * @param c some collection
	 * @return the variance over elements
	 */
	public static double varianceInt(Collection<Integer> c) {
		double mean = meanInt(c);
		double var = 0.0;
		for(Integer n : c) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / c.size();
	}


	/**
	 * Compute the variance over elements in a collection.
	 * @param c some collection
	 * @return the variance over elements
	 */
	public static double variance(Collection<Double> c) {
		double mean = mean(c);
		double var = 0.0;
		for(Double n : c) {
			double foo = mean - n;
			var += foo * foo;
		}

		return var / c.size();
	}


	/**
	 * Find the minimum value in a collection.
	 * @param c some collection
	 * @return the minimum value
	 */
	public static int minInt(Collection<Integer> c) {
		if(c.isEmpty()) {
			throw new IllegalArgumentException();
		}
		int min = Integer.MAX_VALUE;
		for(Integer n : c) {
			if(n < min) {
				min = n;
			}
		}
		return min;
	}


	/**
	 * Find the minimum value in a collection.
	 * @param c some collection
	 * @return the minimum value
	 */
	public static double min(Collection<Double> c) {
		if(c.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Double min = Double.MAX_VALUE;
		for(Double n : c) {
			if(n < min) {
				min = n;
			}
		}
		return min;
	}


	/**
	 * Find the maximum value in a collection.
	 * @param c some collection
	 * @return the maximum value
	 */
	public static int maxInt(Collection<Integer> c) {
		if(c.isEmpty()) {
			throw new IllegalArgumentException();
		}
		int max = Integer.MIN_VALUE;
		for(Integer n : c) {
			if(n > max) {
				max = n;
			}
		}
		return max;
	}


	/**
	 * Find the maximum value in a collection.
	 * @param c some collection
	 * @return the maximum value
	 */
	public static double max(Collection<Double> c) {
		if(c.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Double max = Double.MIN_VALUE;
		for(Double n : c) {
			if(n > max) {
				max = n;
			}
		}
		return max;
	}
}