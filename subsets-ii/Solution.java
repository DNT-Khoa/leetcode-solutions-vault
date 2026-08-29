// Time:  O(N.2^N)
// Space: O(N)

import java.util.List;

class Solution {
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        explore(nums, result, 0, new ArrayList<>());
        return result;
    }

    void explore(int[] nums, List<List<Integer>> result, int current, List<Integer> selected) {
        result.add(new ArrayList<>(selected));
        
        for (int i = current; i < nums.length; i++) {
            if (i > current && nums[i] == nums[i - 1]) continue;

            // select
            selected.add(nums[i]);
            // explore
            explore(nums, result, i + 1, selected);
            // backtrack
            selected.remove(selected.size() - 1);
        }
    }
}
