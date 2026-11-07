// Time:  O(n) where n is the number of characters in s
// Space: O(n)

import java.util.LinkedList;
import java.util.List;

class Solution {
    public String reverseWords(String s) {
        List<String> words = new LinkedList<>();
        for (String word: s.trim().split("\\s+")) {
            words.addFirst(word);
        }

        return String.join(" ", words);
    }
}
