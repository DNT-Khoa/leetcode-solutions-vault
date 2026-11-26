// Time:  O(ElogE) where E is times length and V is n
// Space: O(V + E)

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        Map<Integer, List<int[]>> adjList = buildAdjacencyList(times);

        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[k] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e[1]));
        pq.offer(new int[]{k, 0});

        while (!pq.isEmpty()) {
            int[] edge = pq.poll();
            int node = edge[0], distance = edge[1];

            if (distance > dist[node]) continue; // ignore stale longer path

            for (int[] neighborEdge :  adjList.getOrDefault(node, List.of())) {
                int neighbor = neighborEdge[0], newDistance = distance + neighborEdge[1];
                if (newDistance < dist[neighbor]) {
                    dist[neighbor] = newDistance;
                    pq.offer(new int[]{neighbor, newDistance}); // promising so add it to queue
                }
            }
        }

        int result = Integer.MIN_VALUE;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;
            result = Math.max(result, dist[i]);
        }
        return result;
    }

    private Map<Integer, List<int[]>> buildAdjacencyList(int[][] times) {
        Map<Integer, List<int[]>> adjList = new HashMap<>();
        for (int[] edge : times) {
            adjList.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(new int[]{edge[1], edge[2]});
        }

        return adjList;
    }
}
