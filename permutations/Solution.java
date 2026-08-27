
import java.util.ArrayList;
import java.util.List;

// Time:  O(N.N!) where N is nums.length
// Space: O(N)

class Solution {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] visited = new boolean[nums.length];
        explore(nums, new ArrayList<>(), visited, result);
        return result;
    }

    void explore(int[] nums, List<Integer> selected, boolean[] visited, List<List<Integer>> result) {
        if (selected.size() == nums.length) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) continue;

            // select
            visited[i] = true;
            selected.add(nums[i]);
            // explore
            explore(nums, selected, visited, result);
            // backtrack
            visited[i] = false;
            selected.remove(selected.size() - 1);
        }
    }
}
