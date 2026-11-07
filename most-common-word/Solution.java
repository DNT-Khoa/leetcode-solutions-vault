// Time:  O(m + n) where m is the number of characters in paragraph and n is the total characters in banned list 
// Space: O(m + n)

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Solution {
    public String mostCommonWord(String paragraph, String[] banned) {
        Set<String> bannedSet = new HashSet<>();
        for (String s : banned) {
            bannedSet.add(s);
        }

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : paragraph.toLowerCase().split("[^A-Za-z]+")) {
            if (!word.isEmpty()) {
                wordCount.merge(word, 1, Integer::sum);
            }
        }

        return wordCount.entrySet().stream()
                .filter(e -> !bannedSet.contains(e.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }
}
