// Time:  O(ElogE) where E is the number of tickets
// Space: O(E)

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class Solution {
    public List<String> findItinerary(List<List<String>> tickets) {
        var adjMap = buildAdjacencyMap(tickets);
        List<String> itinerary = new ArrayList<>();
        dfs("JFK", adjMap, itinerary);
        Collections.reverse(itinerary);
        return itinerary;
    }

    private void dfs(String source, Map<String, PriorityQueue<String>> adjMap, List<String> itinerary) {
        while (adjMap.containsKey(source) && !adjMap.get(source).isEmpty()) {
            String target = adjMap.get(source).poll();
            dfs(target, adjMap, itinerary);
        }
        itinerary.add(source);
    }

    private Map<String, PriorityQueue<String>> buildAdjacencyMap(List<List<String>> tickets) {
        Map<String, PriorityQueue<String>> adjMap = new HashMap<>();
        for (var ticket : tickets) {
            adjMap.computeIfAbsent(ticket.get(0), k -> new PriorityQueue<>()).offer(ticket.get(1));
        }

        return adjMap;
    }
}
