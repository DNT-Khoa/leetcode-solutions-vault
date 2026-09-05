// Time:  O(N.M) where N and M are the number of rows and columns
// Space: O(N.M)

import java.util.ArrayDeque;
import java.util.Deque;

class Solution {
    public int orangesRotting(int[][] grid) {
        int ROWS = grid.length;
        int COLS = grid[0].length;
        int numOfMinutes = -1;
        Deque<int[]> q = new ArrayDeque<>();
        int numOfFreshOranges = 0;

        // first add all the rotten oranges to the queue
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == 2) {
                    q.offer(new int[] {i, j});
                } else if (grid[i][j] == 1) {
                    numOfFreshOranges++;
                }
            }
        }

        // if there's already no fresh orange then it takes 0 minute
        if (numOfFreshOranges == 0) return 0;
        // if there's no rotten oranges then it's impossible 
        if (q.isEmpty()) return -1;

        // now use bfs to spread the rotten oranges every minute
        while (!q.isEmpty()) {
            int qLen = q.size();
            numOfMinutes++;

            for (int i = 0; i < qLen; i++) {
                int[] orange = q.poll();

                int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] direction : directions) {
                    int nRow = direction[0] + orange[0];
                    int nCol = direction[1] + orange[1];

                    if (Math.min(nRow, nCol) < 0 || nRow == ROWS || nCol == COLS || grid[nRow][nCol] != 1) 
                        continue;
                    grid[nRow][nCol] = 2;
                    numOfFreshOranges--;
                    q.offer(new int[]{nRow, nCol});
                }
            }
        }

        // check if there's still any unrotten oranges
        if (numOfFreshOranges > 0) return -1;

        return numOfMinutes;
    }
}
