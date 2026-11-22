// Time:  O(R·C) where R is number of rows and C is number of columns
// Space: O(R·C)

class Solution {

    private int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public int numIslands(char[][] grid) {
        int islandsCount = 0;
        
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] == '0') continue;
                islandsCount++;
                dfs(r, c, grid);
            }
        }

        return islandsCount;
    }

    private void dfs(int row, int col, char[][] grid) {
        grid[row][col] = '0';
        for (int[] direction : directions) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];

            if (nextRow < 0 || nextRow == grid.length || nextCol < 0 || nextCol == grid[0].length) continue;
            if (grid[nextRow][nextCol] == '0') continue;
            dfs(nextRow, nextCol, grid);
        }
    }
}
