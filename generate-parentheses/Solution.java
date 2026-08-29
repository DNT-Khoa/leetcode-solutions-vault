// Time:  O(N.2^2N) -> O(N.4^N)
// Space: O(2N)

import java.util.ArrayList;
import java.util.List;

class Solution {
    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        explore(n * 2, 0, 0, new StringBuilder(), result);
        return result;
    }

    void explore(int max, int nOpen, int nClose, StringBuilder selected, List<String> result) {
        if (nClose > nOpen) return;
        if (nOpen + nClose == max) {
            if (nOpen == nClose) result.add(selected.toString());
            return;
        }

        selected.append('(');
        explore(max, nOpen + 1, nClose, selected, result);
        selected.deleteCharAt(selected.length() - 1);
        selected.append(')');
        explore(max, nOpen, nClose + 1, selected, result); 
        selected.deleteCharAt(selected.length() - 1);
    }
}
