// Time:  O(R·C) where R is number of rows and C is number of columns 
// Space: O(R·C)

class Solution {
    int ROWS = 0;
    int COLS = 0;
    int[][] DIRCTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public int maxAreaOfIsland(int[][] grid) {
        int maxArea = 0;
        ROWS = grid.length;
        COLS = grid[0].length;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) continue;
                maxArea = Math.max(maxArea, dfs(r, c, grid));
            }
        }

        return maxArea;
    }

    private int dfs(int row, int col, int[][] grid) {
        
        grid[row][col] = 0;
        int count = 1;
        for (int[] direction : DIRCTIONS) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];

            if (nextRow < 0 || nextRow == ROWS || nextCol < 0 || nextCol == COLS) continue;
            if (grid[nextRow][nextCol] == 0) continue;
            count += dfs(nextRow, nextCol, grid);
        }

        return count;
    }
}
