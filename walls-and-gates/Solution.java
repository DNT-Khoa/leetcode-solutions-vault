import java.util.ArrayDeque;
import java.util.Deque;

// Time:  O(R·C) where R is number of rows and C is number of columns
// Space: O(R·C) for the queue in the worst case

class Solution {

    public void wallsAndGates(int[][] rooms) {
        int ROWS = rooms.length;
        int COLS = rooms[0].length;
        int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        Deque<int[]> queue = new ArrayDeque<>();

        // find the gates 
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (rooms[i][j] == 0) {
                    // add gate to queue
                    queue.offer(new int[]{i, j});
                } 
            }
        }

        int level = 1;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int[] room = queue.poll();

                for (int[] direction : DIRECTIONS) {
                    int nextRow = room[0] + direction[0];
                    int nextCol = room[1] + direction[1];
                    if (nextRow < 0 || nextRow == ROWS || nextCol < 0 || nextCol == COLS) continue;
                    if (rooms[nextRow][nextCol] != Integer.MAX_VALUE) continue;

                    rooms[nextRow][nextCol] = level;
                    queue.offer(new int[]{nextRow, nextCol});
                }
            }
            level++;
        }
    }
}
