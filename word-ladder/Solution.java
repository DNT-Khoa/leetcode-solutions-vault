
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Time:  O(N*26*L^2) where N is the number of words and L is the length of longest word
// Space: O(N*L)

class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> words = new HashSet<>(wordList);

        Deque<String> queue = new ArrayDeque<>();
        queue.offer(beginWord);
        int count = 0;

        while (!queue.isEmpty()) {
            count++;
            int size = queue.size();
            for (int temp = 0; temp < size; temp++) {
                String word = queue.poll();
                if (word.equals(endWord)) return count;

                char[] chars = word.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char original = chars[i];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == original) continue;
                        chars[i] = c;
                        String candidate = new String(chars);
                        if (words.contains(candidate)) {
                            queue.offer(candidate);
                            words.remove(candidate);
                        } 
                    }
                    chars[i] = original;
                }
            }
        }

        return 0;
    }
}
