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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MovingStatisticsTest {

	static double getPercentile(List<Double> values, double index) {


		if (values.isEmpty()) {

			return 0;

		} else {

			int idx = ((int) Math.ceil((index / 100.0) * values.size())) - 1;
			return values.get(idx);

		}
	}

	static double getMedian(List<Double> values) {


		if (values.isEmpty()) {

			return 0;

		} else {

			double median = 0.0;

			if (values.size() % 2 == 0)
				median = (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
			else
				median = values.get(values.size() / 2);

			return median;
		}
	}

	static double getSum(List<Double> values) {

		double sum = 0;
		for (double d : values) sum+=d;
		return sum;
	}

	static double getAverage(List<Double> values) {


		if (values.isEmpty()) {

			return 0;

		} else {

			return getSum(values) / values.size();
		}
	}
	
	@Test
	public void basicTest() {
		
		List<Double> list = new ArrayList<>();
		MovingStatistics ms = new MovingStatistics(5, TimeUnit.SECONDS);
		for (int i = 0; i < 101; i++) {
			ms.add(i);
			list.add((double) i);
		}
		
		assertEquals(getSum(list), ms.getSum(), 0);
		assertEquals(getAverage(list), ms.getAverage(), 0);
		assertEquals(getMedian(list), ms.getMedian(), 0);
		assertEquals(getPercentile(list, 90), ms.getPercentile(90), 0);		
		
	}
	
	@Test
	public void timedTest() {
				
		try {
			
			List<Double> list1 = new ArrayList<>();
			MovingStatistics ms = new MovingStatistics(5, TimeUnit.SECONDS);
			for (int i = 0; i < 101; i++) {
				ms.add(i);
				list1.add((double)i);
			}
			
			List<Double> list2 = new ArrayList<>();
			Thread.sleep(TimeUnit.SECONDS.toMillis(4));
			
			for (int i = 101; i < 201; i++) {
				ms.add(i);
				list1.add((double)i);
				list2.add((double)i);
			}
			assertEquals(getSum(list1), ms.getSum(), 0);
			assertEquals(getAverage(list1), ms.getAverage(), 0);
			assertEquals(getMedian(list1), ms.getMedian(), 0);
			assertEquals(getPercentile(list1,90), ms.getPercentile(90), 0);
			
			//some values are going to expire (out of window)
			Thread.sleep(TimeUnit.SECONDS.toMillis(2));
			
			assertEquals(getSum(list2), ms.getSum(), 0);
			assertEquals(getAverage(list2), ms.getAverage(), 0);
			assertEquals(getMedian(list2), ms.getMedian(), 0);
			assertEquals(getPercentile(list2,90), ms.getPercentile(90), 0);
			
			//reinsert the values and test
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
			for (int i = 0; i < 101; i++) {
				ms.add(i);
				list2.add((double)i);
			}
			Collections.sort(list2);
			assertEquals(getSum(list2), ms.getSum(), 0);
			assertEquals(getAverage(list2), ms.getAverage(), 0);
			assertEquals(getMedian(list2), ms.getMedian(), 0);
			assertEquals(getPercentile(list2,90), ms.getPercentile(90), 0);
			

			
		} catch (InterruptedException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	

	@Test
	public void expirationTest() {
				
		try {
			
			MovingStatistics ms = new MovingStatistics(2, TimeUnit.SECONDS);
			for (int i = 0; i < 101; i++) {
				ms.add(i);
			}
			
			Thread.sleep(TimeUnit.SECONDS.toMillis(3));
			
			assertEquals(0, ms.getSum(), 0);
			assertEquals(0, ms.getAverage(), 0);
			assertEquals(0, ms.getMedian(), 0);
			assertEquals(0, ms.getPercentile(90), 0);
			
		} catch (InterruptedException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Test
	public void testAnotherTimeUnit() {
				
		try {
			
			List<Double> list1 = new ArrayList<>();
			MovingStatistics ms = new MovingStatistics(500, TimeUnit.MILLISECONDS);
			for (int i = 0; i < 101; i++) {
				ms.add(i);
				list1.add((double)i);
			}
			
			List<Double> list2 = new ArrayList<>();
			Thread.sleep(400);
			
			for (int i = 101; i < 201; i++) {
				ms.add(i);
				list1.add((double)i);
				list2.add((double)i);
			}
			assertEquals(getSum(list1), ms.getSum(), 0);
			assertEquals(getAverage(list1), ms.getAverage(), 0);
			assertEquals(getMedian(list1), ms.getMedian(), 0);
			assertEquals(getPercentile(list1,90), ms.getPercentile(90), 0);
			
			//some values are going to expire (out of window)
			Thread.sleep(200);
			
			assertEquals(getSum(list2), ms.getSum(), 0);
			assertEquals(getAverage(list2), ms.getAverage(), 0);
			assertEquals(getMedian(list2), ms.getMedian(), 0);
			assertEquals(getPercentile(list2,90), ms.getPercentile(90), 0);
			
			//reinsert the values and test
			Thread.sleep(100);
			for (int i = 0; i < 101; i++) {
				ms.add(i);
				list2.add((double)i);
			}
			Collections.sort(list2);
			assertEquals(getSum(list2), ms.getSum(), 0);
			assertEquals(getAverage(list2), ms.getAverage(), 0);
			assertEquals(getMedian(list2), ms.getMedian(), 0);
			assertEquals(getPercentile(list2,90), ms.getPercentile(90), 0);
			

			
		} catch (InterruptedException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	
}
