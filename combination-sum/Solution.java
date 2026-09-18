
import java.util.ArrayList;
import java.util.List;

// Time:  O(N^(T/M)) where T is target and M is smallest value in candidates
// Space: O(T/M)

class Solution {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        dfs(candidates, target, 0, 0, new ArrayList<>(), result);
        return result;
    }

    void dfs(int[] candidates, int target, int start, int sum, List<Integer> selected, List<List<Integer>> result) {
        if (sum > target) return;
        if (sum == target) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            selected.add(candidates[i]);
            dfs(candidates, target, i, sum + candidates[i], selected, result);
            selected.remove(selected.size() - 1);
        }
    }
}
