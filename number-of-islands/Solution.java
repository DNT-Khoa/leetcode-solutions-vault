// Time:  O(M.N) where M and N are the length of rows and cols respectively
// Space: O(M.N)

class Solution {
    private int ROWS;
    private int COLS;

    public int numIslands(char[][] grid) {
        this.ROWS = grid.length;
        this.COLS = grid[0].length;
        boolean[][] visited = new boolean[ROWS][COLS];
        int numOfIslands = 0;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!visited[i][j] && grid[i][j] == '1') {
                    numOfIslands++;
                    dfs(visited, grid, i, j);
                }
            }
        }
        return numOfIslands;
    }

    void dfs(boolean[][] visited, char[][] grid, int currentRow, int currentCol) {
        if (Math.min(currentRow, currentCol) < 0 || currentRow == ROWS || currentCol == COLS 
        || grid[currentRow][currentCol] == '0' || visited[currentRow][currentCol]) return;

        visited[currentRow][currentCol] = true;
        int[][] directions = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        for (int[] direction : directions) {
            dfs(visited, grid, currentRow + direction[0], currentCol + direction[1]);
        }
    }
}
