// Time:  O(N.N!)
// Space: O(N)

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] visited = new boolean[nums.length];
        dfs(nums, new ArrayList<>(), visited, result);
        return result;
    }

    void dfs(int[] nums, List<Integer> selected, boolean[] visited, List<List<Integer>> result) {
        if (selected.size() == nums.length) {
            result.add(new ArrayList<>(selected));
        } 

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) continue;
            visited[i] = true;
            selected.add(nums[i]);
            dfs(nums, selected, visited, result);
            selected.remove(selected.size() - 1);
            visited[i] = false;
        }
    }
}
