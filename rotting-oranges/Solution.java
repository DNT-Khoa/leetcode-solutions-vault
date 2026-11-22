
import java.util.ArrayDeque;
import java.util.Deque;

// Time:  O(R·C) where R is number of rows and C is number of columns
// Space: O(R·C)

class Solution {
    public int orangesRotting(int[][] grid) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int rows = grid.length;
        int cols = grid[0].length;
        
        // add all current rotting oranges to a queue
        Deque<int[]> queue = new ArrayDeque<>();
        int numOfOranges = 0;
        int numOfRottenOranges = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 2) {
                    queue.offer(new int[]{r, c});
                    numOfRottenOranges++;
                } 
                if (grid[r][c] != 0) numOfOranges++;
            }
        }

        if (numOfOranges == numOfRottenOranges) return 0;

        int minute = -1;
        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                int[] rottenOrange = queue.poll();

                for (int[] direction : directions) {
                    int nextRow = rottenOrange[0] + direction[0];
                    int nextCol = rottenOrange[1] + direction[1];

                    if (nextRow < 0 || nextRow == rows || nextCol < 0 || nextCol == cols) continue;
                    if (grid[nextRow][nextCol] != 1) continue;

                    grid[nextRow][nextCol] = 2;
                    numOfRottenOranges++;
                    queue.offer(new int[]{nextRow, nextCol});
                }
            }
            minute++;
        }

        if (numOfOranges != numOfRottenOranges) return -1;
        return minute;
    }
}
