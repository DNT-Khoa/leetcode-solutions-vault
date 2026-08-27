
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Time:  O(N.2^N)
// Space: O(N)

class Solution {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates);
        List<List<Integer>> result = new ArrayList<>();
        explore(candidates, target, result, new ArrayList<>(), 0, 0);
        return result;
    }

    void explore(int[] candidates, int target, List<List<Integer>> result, List<Integer> selected, int start, int sum) {
        if (sum > target) return;
        if (sum == target) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = start; i < candidates.length; i++) {
            if (i > start && candidates[i] == candidates[i - 1]) continue;
            // select
            selected.add(candidates[i]);
            // explore
            explore(candidates, target, result, selected, i + 1, sum + candidates[i]);
            // backtrack
            selected.remove(selected.size() - 1);
        }
    }
}
