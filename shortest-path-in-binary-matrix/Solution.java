
import java.util.ArrayDeque;
import java.util.Deque;

// Time:  O(M * N) where M and N are the length of rows and columns
// Space: O(M * N)

class Solution {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int ROWS = grid.length;
        int COLS = grid[0].length;
        boolean[][] visited = new boolean[ROWS][COLS];
        Deque<int[]> q = new ArrayDeque<>();

        if (grid[0][0] == 1) return -1;
        q.offer(new int[]{0, 0});
        visited[0][0] = true;
        
        int currentPath = 0;
        while (!q.isEmpty()) {
            int qLen = q.size();
            currentPath++;

            for (int k = 0; k < qLen; k++) {
                int[] cell = q.pop();
                int row = cell[0], col = cell[1];
                if (row == ROWS - 1 && col == COLS - 1) return currentPath;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int nextRow = row + i, nextCol = col + j;
                        if (Math.min(nextRow, nextCol) < 0 || nextRow == ROWS || nextCol == COLS 
                        || grid[nextRow][nextCol] == 1 || visited[nextRow][nextCol]) continue;
                        
                        visited[nextRow][nextCol] = true;
                        q.offer(new int[]{nextRow, nextCol});
                    }
                }
            }
        }

        return -1;
    }
}
