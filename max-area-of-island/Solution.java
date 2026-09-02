// Time:  O(N.M) where N, M are length of rows and columns
// Space: O(N.M)

class Solution {
    private int ROWS;
    private int COLS;

    public int maxAreaOfIsland(int[][] grid) {
        this.ROWS = grid.length;
        this.COLS = grid[0].length;

        int maxArea = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == 0) continue;
                maxArea = Math.max(maxArea, dfs(grid, i, j));
            }
        }

        return maxArea;
    }

    int dfs(int[][] grid, int row, int col) {
        if (Math.min(row, col) < 0 
        || row == ROWS || col == COLS || grid[row][col] == 0) return 0;

        grid[row][col] = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        int area = 0;
        for (int[] direction : directions) {
            area += dfs(grid, row + direction[0], col + direction[1]);
        }
        return area + 1;
    }
}
