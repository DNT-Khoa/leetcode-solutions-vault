
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Time:  O(N) where N is number of characters in s
// Space: O(26)

class Solution {
    public List<Integer> partitionLabels(String s) {
        int n = s.length();
        Map<Character, Integer> characterLastIndex = new HashMap<>();
        for (int i = n - 1; i >= 0; i--) {
            if (!characterLastIndex.containsKey(s.charAt(i))) 
                characterLastIndex.put(s.charAt(i), i);
        }

        List<Integer> result = new ArrayList<>();

        int start = 0, end = 0;
        for (int i = 0; i < n; i++) {
            end = Math.max(end, characterLastIndex.get(s.charAt(i)));
            if (i == end) {
                result.add(end - start + 1);
                start = i + 1;
            }
        }

        return result;
    }
}
