
import java.util.ArrayList;
import java.util.List;

// Time:  O(n.2^n) where n is length of nums
// Space: O(n)
// Note:
// copy the selected list of result (result.add(new Array<>(selected))) takes O(n)

class Solution {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        explore(nums, result, 0, new ArrayList<>());
        return result;
    }

    public void explore(int[] nums, List<List<Integer>> result, int startIdx, List<Integer> selected) {
        result.add(new ArrayList<>(selected));

        for (int i = startIdx; i < nums.length; i++) {
            // select
            selected.add(nums[i]);
            // explore
            explore(nums, result, i + 1, selected);
            // backtrack
            selected.remove(selected.size() - 1);
        }
    }
}
