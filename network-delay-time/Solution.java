
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Time:  O(ELog(E))
// Space: O(V + E) because shortest paths contains maximum V nodes and adjacency list contains maximum E edges

class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        // build adjacency list
        Map<Integer, List<int[]>> adjacencyList = buildAdjacencyList(times);
        
        // Run Dijkstra algorithm
        PriorityQueue<int[]> pq = new PriorityQueue<>((n1, n2) -> Integer.compare(n1[0], n2[0]));
        Map<Integer, Integer> shortestPaths = new HashMap<>();

        pq.offer(new int[]{0, k});
        while (!pq.isEmpty()) {
            int[] edge = pq.poll();
            int weight1 = edge[0], node1 = edge[1];

            if (shortestPaths.containsKey(node1)) continue;
            shortestPaths.put(node1, weight1);

            List<int[]> neighbors = adjacencyList.get(node1);
            if (neighbors != null) {
                for (int[] neighbor : neighbors) {
                    int weight2 = neighbor[1], node2 = neighbor[0];
                    if (shortestPaths.containsKey(node2)) continue;

                    pq.offer(new int[]{weight1 + weight2, node2});
                }
            }
        }

        // check if there's any node unreachable from k
        for (int i = 1; i <= n; i++) {
            if (!shortestPaths.containsKey(i)) return -1;
        }

        // find min among shortestPaths
        int res = 0;
        for (int node : shortestPaths.keySet()) {
            int weight = shortestPaths.get(node);
            res = Math.max(res, weight);
        }

        return res;
    }

    Map<Integer, List<int[]>> buildAdjacencyList(int[][] times) {
        Map<Integer, List<int[]>> adjacencyList = new HashMap<>();
        for (int[] edge : times) {
            int src = edge[0], target = edge[1], weight = edge[2];
            adjacencyList.computeIfAbsent(src, k -> new ArrayList<>()).add(new int[]{target, weight});
        }
        return adjacencyList;
    }
}
