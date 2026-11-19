// Time:  O(NlogN + QlogQ) where N is the number of intervals and Q is the number of queries
// Space: O(N + Q)

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

class Solution {
    public int[] minInterval(int[][] intervals, int[] queries) {
        int n = intervals.length;
        int[] result = queries.clone();
        Map<Integer, Integer> resultMap = new HashMap<>(); // link each query with its result
        
        // sort the intervals by start time in ascending order
        Arrays.sort(intervals, Comparator.comparingInt(i -> i[0]));

        // sort the query in ascending order
        Arrays.sort(queries);

        // [size, endTime]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(i -> i[0]));

        int i = 0;
        for (int j = 0; j < queries.length; j++) {
            int query = queries[j];
            
            // add all intervals with startTime <= query
            while (i < n && intervals[i][0] <= query) {
                minHeap.offer(new int[]{intervals[i][1] - intervals[i][0] + 1, intervals[i][1]});
                i++;
            }

            // poll all intervals with endTime < query
            while (!minHeap.isEmpty() && minHeap.peek()[1] < query) {
                minHeap.poll();
            }

            if (minHeap.isEmpty()) {
                resultMap.put(query, -1);
            } else {
                resultMap.put(query, minHeap.peek()[0]);
            }

        }

        for (int j = 0; j < result.length; j++) {
            result[j] = resultMap.get(result[j]);
        }

        return result;
    }
}
