
import java.util.ArrayList;
import java.util.List;

// Time:  O(N.2^N) where N is length of s
// Space: O(N)

class Solution {
    public List<List<String>> partition(String s) {
        List<List<String>> result = new ArrayList<>();
        explore(s, result, new ArrayList<>(), 0);
        return result;
    }

    void explore(String s, List<List<String>> result, List<String> selected, int start) {
        if (start == s.length()) {
            result.add(new ArrayList<>(selected));
            return;
        }

        for (int end = start + 1; end <= s.length(); end++) {
            if (isPalindrome(s, start, end - 1)) {
                selected.add(s.substring(start, end));
                explore(s, result, selected, end);
                selected.remove(selected.size() - 1);
            }
        }
    }

    boolean isPalindrome(String s, int lo, int hi) {
        while (lo < hi) {
            if (s.charAt(lo++) != s.charAt(hi--)) return false;
        }

        return true;
    }
}
