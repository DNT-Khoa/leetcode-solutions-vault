
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Time:  O(Elog(V))
// Space: O(V + E) for adjacency list

class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        // build adjacency list
        Map<Integer, List<int[]>> adjacencyList = new HashMap<>();
        for (int[] time : times) {
            adjacencyList.computeIfAbsent(time[0], key -> new ArrayList<>()).add(new int[]{time[2], time[1]});
        }

        // run Dijkstra algorithm 
        Map<Integer, Integer> shortestPaths = new HashMap<>();
        PriorityQueue<int[]> heap = new PriorityQueue<>((n1, n2) -> Integer.compare(n1[0], n2[0]));
        heap.offer(new int[]{0, k});

        while (!heap.isEmpty()) {
            int[] current = heap.poll();
            int w1 = current[0], n1 = current[1];

            if (shortestPaths.containsKey(n1)) continue;
            shortestPaths.put(n1, w1);

            for (int[] edge : adjacencyList.getOrDefault(n1, new ArrayList<>())) {
                int w2 = edge[0], n2 = edge[1];
                if (!shortestPaths.containsKey(n2)) {
                    heap.offer(new int[] {w1 + w2, n2});
                }
            }
        }

        // now loop through the shortest path and check the max values
        if (shortestPaths.size() != n) return -1;
        int result = -1;
        for (int node : shortestPaths.keySet()) {
            result = Math.max(result, shortestPaths.get(node));
        }

        return result;
    }
}
