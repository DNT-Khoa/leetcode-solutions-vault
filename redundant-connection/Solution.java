// Time:  O(V + E*alpha(V)) where V is the number of vertices and E is the number of edges
// Space: O(V)

import java.util.Arrays;

class Solution {
    public int[] findRedundantConnection(int[][] edges) {
        int n = edges.length + 1;
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }

        int[] size = new int[n];
        Arrays.fill(size, 1);

        for (int[] edge : edges) {
            if (!union(edge[0], edge[1], parent, size)) {
                return edge;
            } 
        }

        return new int[0];
    }

    private int findRoot(int node, int[] parent) {
        if (node != parent[node]) 
            parent[node] = findRoot(parent[node], parent);
        return parent[node];
    }

    private boolean union(int nodeA, int nodeB, int[] parent, int[] size) {
        int rootA = findRoot(nodeA, parent);
        int rootB = findRoot(nodeB, parent);

        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];

        return true;
    }
}
