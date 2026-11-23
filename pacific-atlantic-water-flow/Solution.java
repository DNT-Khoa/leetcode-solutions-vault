// Time:  O(R·C) where R is number of rows and C is number of columns
// Space: O(R·C)

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Solution {
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static int ROWS;
    private static int COLS;

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        ROWS = heights.length;
        COLS = heights[0].length;

        boolean[][] pacificReach = new boolean[ROWS][COLS];
        boolean[][] atlanticReach = new boolean[ROWS][COLS];

        Deque<int[]> pacificQueue = new ArrayDeque<>();
        Deque<int[]> atlanticQueue = new ArrayDeque<>();

        for (int r = 0; r < ROWS; r++) {
            pacificReach[r][0] = true;
            pacificQueue.offer(new int[]{r, 0});

            atlanticReach[r][COLS-1] = true;
            atlanticQueue.offer(new int[]{r, COLS-1});
        }

        for (int c = 0; c < COLS; c++) {
            pacificReach[0][c] = true;
            pacificQueue.offer(new int[]{0, c});

            atlanticReach[ROWS-1][c] = true;
            atlanticQueue.offer(new int[]{ROWS-1, c});
        }

        bfs(heights, pacificQueue, pacificReach);
        bfs(heights, atlanticQueue, atlanticReach);

        List<List<Integer>> result = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (pacificReach[r][c] && atlanticReach[r][c]) {
                    result.add(List.of(r, c));
                }
            }
        }

        return result;
    }

    private void bfs(int[][] heights, Deque<int[]> queue, boolean[][] reach) {
        while (!queue.isEmpty()) {
           int[] cell = queue.poll();

            for (int[] direction : DIRECTIONS) {
                int nextRow = cell[0] + direction[0];
                int nextCol = cell[1] + direction[1];

                if (nextRow < 0 || nextRow == ROWS || nextCol < 0 || nextCol == COLS) continue;
                if (reach[nextRow][nextCol] || heights[nextRow][nextCol] < heights[cell[0]][cell[1]]) continue;

                reach[nextRow][nextCol] = true;
                queue.offer(new int[]{nextRow, nextCol});
            }
        }
    }
}
