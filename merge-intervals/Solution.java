
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Time:  O(Nlog(N)) where N is the number of intervals
// Space: O(N)

class Solution {
    public int[][] merge(int[][] intervals) {
        // sort by start time in ascending order
        Arrays.sort(intervals, Comparator.comparingInt(i -> i[0]));

        List<int[]> result = new ArrayList<>();
        result.add(intervals[0]);

        for (int i = 1; i < intervals.length; i++) {
            int[] interval = intervals[i];
            int[] prevInterval = result.getLast();
            if (interval[0] <= prevInterval[1]) {
                prevInterval[1] = Math.max(prevInterval[1], interval[1]);
            } else {
                result.add(interval);
            }
        }

        return result.toArray(new int[0][]);
    }
}
