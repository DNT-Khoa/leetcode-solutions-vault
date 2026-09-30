// Time:  O(N.(E + V)) where N = queries
// Space: O(E + V)

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Solution {
    public double[] calcEquation(List<List<String>> equations, double[] values, List<List<String>> queries) {
        Map<String, Map<String, Double>> adjList = buildAdjacencyList(equations, values);
        double[] res = new double[queries.size()];

        for (int i = 0; i < queries.size(); i++) {
            List<String> query = queries.get(i);
            String src = query.get(0);
            String target = query.get(1);

            res[i] = bfs(adjList, src, target);
        }

        return res;
    }

    double bfs(Map<String, Map<String, Double>> adj, String src, String target) {
        if (!adj.containsKey(src) || !adj.containsKey(target)) {
            return -1;
        }
        if (src.equals(target)) return 1;

        Deque<String> q = new ArrayDeque<>();
        Map<String, Double> visitedToProduct = new HashMap<>();
        q.offer(src);
        visitedToProduct.put(src, 1.0);

        while(!q.isEmpty()) {
            int levelCount = q.size();

            for (int i = 0; i < levelCount; i++) {
                String node = q.poll();
                Map<String, Double> neighbors = adj.get(node);

                if (neighbors != null) {
                    for (String neighbor : neighbors.keySet()) {
                        if (visitedToProduct.containsKey(neighbor)) continue;
                        double weight = neighbors.get(neighbor);
                        visitedToProduct.put(neighbor, visitedToProduct.get(node) * weight);
                        if (neighbor.equals(target)) return visitedToProduct.get(neighbor);

                        q.offer(neighbor);
                    }
                }
            }
        }

        return -1;
    }

    Map<String, Map<String, Double>> buildAdjacencyList(List<List<String>> equations, double[] values) {
        Map<String, Map<String, Double>> adj = new HashMap<>();
        for (int i = 0; i < equations.size(); i++) {
            List<String> equation = equations.get(i);
            String src = equation.get(0), target = equation.get(1);

            adj.computeIfAbsent(src, k -> new HashMap<>()).put(target, values[i]);
            adj.computeIfAbsent(target, k -> new HashMap<>()).put(src, 1 / values[i]);
        }

        return adj;
    }
}
