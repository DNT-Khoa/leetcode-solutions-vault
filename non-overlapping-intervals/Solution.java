import java.util.Arrays;
import java.util.Comparator;

// Time:  O(Nlog(N))
// Space: O(N)

class Solution {
    public int eraseOverlapIntervals(int[][] intervals) {
        // sort the intervals by start time
        Arrays.sort(intervals, Comparator.comparingInt(i -> i[0]));

        int result = 0;
        int prevIntervalEnd = intervals[0][1];
        for (int i = 1; i < intervals.length; i++) {
            int[] interval = intervals[i];

            if (interval[0] < prevIntervalEnd) {
                prevIntervalEnd = Math.min(prevIntervalEnd, interval[1]);
                result++;
            } else {
                prevIntervalEnd = interval[1];
            }
        }

        return result;
    }
}
