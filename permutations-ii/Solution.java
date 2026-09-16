
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Time:  O(N.N!)
// Space: O(N)

class Solution {
    public List<List<Integer>> permuteUnique(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        boolean[] visited = new boolean[nums.length];
        dfs(nums, visited, new ArrayList<>(), result);
        return result;
    }

    void dfs(int[] nums, boolean[] visited, List<Integer> selected, List<List<Integer>> result) {
        if (selected.size() == nums.length) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i] || (i > 0 && nums[i] == nums[i - 1] && !visited[i - 1])) continue;

            visited[i] = true;
            selected.add(nums[i]);
            dfs(nums, visited, selected, result);
            selected.remove(selected.size() - 1);
            visited[i] = false;
        }
    }
}
