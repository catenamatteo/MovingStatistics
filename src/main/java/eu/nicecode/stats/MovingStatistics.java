/*******************************************************************************
 * Copyright 2018 Matteo Catena
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package eu.nicecode.stats;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;

/**
 * Compute statistics over a moving (rolling) time window.
 *
 * @author Matteo Catena
 *
 */
public class MovingStatistics {

	private Long2ObjectSortedMap<DoubleList> time2values;
	private DoubleList values;
	final private long nanoTimeOffset;
	private long timeWindow;
	private TimeUnit timeUnit;

	private double sum = 0;

	/**
	 * It computes sum, average, median, and percentile(s) statistics over a
	 * moving (rolling) time window.
	 * 
	 * @param timeWindow
	 */
	public MovingStatistics(long timeWindow, TimeUnit timeUnit) {

		time2values = new Long2ObjectAVLTreeMap<>();
		values = new DoubleArrayList();
		nanoTimeOffset = System.nanoTime();
		this.timeWindow = timeWindow;
		this.timeUnit = timeUnit;
	}

	private long updateTime() {

		long nanoCallTime = System.nanoTime();
		long callTime = timeUnit.convert(nanoCallTime - nanoTimeOffset, TimeUnit.NANOSECONDS);

		if (!time2values.isEmpty() && callTime - time2values.firstLongKey() > timeWindow) {

			// discard older values
			time2values = new Long2ObjectAVLTreeMap<>(time2values.tailMap(callTime - timeWindow));
			values.clear();
			sum = 0;

			// recreate the sorted value list
			for (DoubleList l : time2values.values())
				for (double d : l) {
					values.add(d);
					sum += d;
				}
			Collections.sort(values);
		}

		return callTime;
	}

	/**
	 * Add a new value. The insertion time is given by System.nanoTime() w.r.t.
	 * the constructor call time.
	 * 
	 * @param val
	 */
	public void add(double val) {

		long callTime = updateTime();

		if (!time2values.containsKey(callTime))
			time2values.put(callTime, new DoubleArrayList());
		time2values.get(callTime).add(val);
		int idx = Collections.binarySearch(values, val);
		if (idx < 0)
			idx = -idx - 1;
		values.add(idx, val);
		sum += val;
	}

	/**
	 * Get the index-th percentile over the time window. E.g., for the
	 * 99.9th-tile, .getPercentile(99.9)
	 * 
	 * @param index
	 * @return
	 */
	public double getPercentile(double index) {

		updateTime();

		if (values.isEmpty()) {

			return 0;

		} else {

			int idx = ((int) Math.ceil((index / 100.0) * values.size())) - 1;
			return values.getDouble(idx);

		}
	}

	/**
	 * Get the median value over the time window.
	 * 
	 * @return
	 */
	public double getMedian() {

		updateTime();

		if (values.isEmpty()) {

			return 0;

		} else {

			double median = 0.0;

			if (values.size() % 2 == 0)
				median = (values.getDouble(values.size() / 2) + values.getDouble(values.size() / 2 - 1)) / 2;
			else
				median = values.getDouble(values.size() / 2);

			return median;
		}
	}

	public double getSum() {

		updateTime();
		
		return sum;
	}

	/**
	 * Return the average value over the time window.
	 * 
	 * @return
	 */
	public double getAverage() {

		updateTime();

		if (values.isEmpty()) {

			return 0;

		} else {

			return sum / values.size();
		}
	}
}
