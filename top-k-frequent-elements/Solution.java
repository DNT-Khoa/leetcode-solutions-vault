
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Time:  O(N) where N is nums length
// Space: O(N)

class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int num : nums) {
            count.merge(num, 1, Integer::sum);
        }

        List<Integer>[] bucket = new ArrayList[nums.length + 1];
        for (int num : count.keySet()) {
            int freq = count.get(num);
            
            if (bucket[freq] == null) bucket[freq] = new ArrayList<>();
            bucket[freq].add(num);
        }

        int[] result = new int[k];
        int i = 0;
        outer: 
        for (int j = bucket.length - 1; j >= 0; j--) {
            if (bucket[j] != null) {
                for (int num : bucket[j]) {
                    result[i] = num;
                    i++;
                    if (i == k) break outer;
                }
            }
        }

        return result;
    }
}
