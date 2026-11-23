// Time:  O(V + E) where V is the numCourses and E is prerequisites.length
// Space: O(V + E)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Solution {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        int[] state = new int[numCourses];
        Map<Integer, List<Integer>> adjacencyList = buildAdjacencyList(prerequisites);

        for (int i = 0; i < numCourses; i++) {
            if (hasCycle(i, state, adjacencyList)) return false;
        }

        return true;
    }

    private boolean hasCycle(int i, int[] state, Map<Integer, List<Integer>> adjacencyList) {
        if (state[i] == 2) return false;
        if (state[i] == 1) return true;

        state[i] = 1;
        boolean result = false;
        for (int nextCourse : adjacencyList.getOrDefault(i, List.of())) {
            result |= hasCycle(nextCourse, state, adjacencyList);
        }

        state[i] = 2;
        return result;
    }

    private Map<Integer, List<Integer>> buildAdjacencyList(int[][] prerequisites) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int[] prerequisite : prerequisites) {
            result.computeIfAbsent(prerequisite[1], k -> new ArrayList<>()).add(prerequisite[0]);
        }

        return result;
    }
}
