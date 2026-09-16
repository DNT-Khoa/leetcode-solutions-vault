
import java.util.ArrayList;
import java.util.List;

// Time:  O(C(n, k)) where C is binomial coefficient
// Space: O(N)

class Solution {
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> result = new ArrayList();
        dfs(n, k, 1, new ArrayList<>(), result);
        return result;
    }

    void dfs(int n, int k, int start, List<Integer> selected, List<List<Integer>> result) {
        if (selected.size() == k) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int i = start; i <= n; i++) {
            selected.add(i);
            dfs(n, k, i + 1, selected, result);
            selected.remove(selected.size() - 1); 
        }
    }
}
