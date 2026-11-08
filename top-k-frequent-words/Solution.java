// Time:  O(NLog(K)) where N is the number of words and K is k
// Space: O(N)

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class Solution {
    record WordFrequency(String word, Integer frequency) {}

    public List<String> topKFrequent(String[] words, int k) {
        Map<String, Integer> count = new HashMap<>();
        for (String word : words) {
            count.merge(word, 1, Integer::sum);
        }
        
        PriorityQueue<WordFrequency> pq = new PriorityQueue<>(Comparator.comparingInt(WordFrequency::frequency).thenComparing(WordFrequency::word, Comparator.reverseOrder()));
        for (String word : count.keySet()) {
            int frequency = count.get(word);
            pq.offer(new WordFrequency(word, frequency));
            if (pq.size() > k) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().word());
        }
        
        Collections.reverse(result);
        return result;
    }
}
