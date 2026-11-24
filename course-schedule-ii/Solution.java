// Time:  O(V + E) where V is the number of courses and E is the number of prerequisites
// Space: O(V + E)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Solution {
    private int counter;

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        int[] state = new int[numCourses];
        Map<Integer, List<Integer>> adjacencyList = buildAdjacencyList(prerequisites);
        int[] result = new int[numCourses];
        counter = numCourses - 1;

        for (int i = 0; i < numCourses; i++) {
            if (hasCycle(i, adjacencyList, state, result)) return new int[0];
        }

        return result;
    }

    private boolean hasCycle(int course, Map<Integer, List<Integer>> adjacencyList, int[] state, int[] result) {
        if (state[course] == 2) return false;
        if (state[course] == 1) return true;

        state[course] = 1;
        for (int nextCourse : adjacencyList.getOrDefault(course, List.of())) {
            if (hasCycle(nextCourse, adjacencyList, state, result)) return true;
        }

        state[course] = 2;
        result[counter--] = course;
        return false;   
    }

    private Map<Integer, List<Integer>> buildAdjacencyList(int[][] prerequisites) {
        Map<Integer, List<Integer>> adj = new HashMap<>();

        for (int[] pre : prerequisites) {
            adj.computeIfAbsent(pre[1], k -> new ArrayList<>()).add(pre[0]);
        }

        return adj;
    }
}
