// Time:  O(N.2^N)
// Space: O(N)

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        dfs(nums, 0, new ArrayList<>(), result);
        return result;
    }

    void dfs(int[] nums, int start, List<Integer> selected, List<List<Integer>> result) {
        result.add(new ArrayList<>(selected));

        for (int i = start; i < nums.length; i++) {
            selected.add(nums[i]);
            dfs(nums, i + 1, selected, result);
            selected.remove(selected.size() - 1);
        }
    }
}
