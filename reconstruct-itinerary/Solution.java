
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Time:  O(E log E)
// Space: O(V + E)

class Solution {
    private List<String> result;
    private Map<String, PriorityQueue<String>> adjacencyList;

    public List<String> findItinerary(List<List<String>> tickets) {
        result = new LinkedList<>();
        adjacencyList = new HashMap<>();

        buildAdjacencyList(tickets);
        find("JFK");
        
        return result;
    }

    void find(String city) {
        var nextCities = adjacencyList.get(city);
        while (nextCities != null && !nextCities.isEmpty()) {
            find(nextCities.poll());
        }
        result.addFirst(city);
    }

    void buildAdjacencyList(List<List<String>> tickets) {
        for (List<String> ticket : tickets) {
            String from = ticket.get(0);
            String to = ticket.get(1);
            adjacencyList.computeIfAbsent(from, k -> new PriorityQueue<>()).offer(to);
        }
    }
}
