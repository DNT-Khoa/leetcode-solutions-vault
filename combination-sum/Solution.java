// Time:  O(N^(T/M + 1)) where T is target, M is min(candidates), and N is size of candidates
// Space: O(T/M)

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        explore(candidates, target, result, new ArrayList<>(), 0, 0);
        return result;
    }

    void explore(
        int[] candidates,
        int target,
        List<List<Integer>> result,
        List<Integer> selected,
        int startIdx,
        int sum
    ) {
        if (sum > target) return;
        if (sum == target) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = startIdx; i < candidates.length; i++) {
            // select
            selected.add(candidates[i]);
            // explore
            explore(candidates, target, result, selected, i, sum + candidates[i]);
            // backtrack
            selected.remove(selected.size() - 1);
        }
    }
}
