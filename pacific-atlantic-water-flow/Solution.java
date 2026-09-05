// Time:  O(M.N) where M, N are num of rows and columns
// Space: O(M.N)

import java.util.ArrayList;

class Solution {
    private int ROWS;
    private int COLS;
    private int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        this.ROWS = heights.length;
        this.COLS = heights[0].length;
        List<List<Integer>> result = new ArrayList<>();
        boolean[][] pacificAdjacent = new boolean[ROWS][COLS];
        boolean[][] atlanticAdjacent = new boolean[ROWS][COLS];

        for (int i = 0; i < ROWS; i++) {
            dfs(heights, i, 0, pacificAdjacent);
            dfs(heights, i, COLS - 1, atlanticAdjacent);
        }

        for (int j = 0; j < COLS; j++) {
            dfs(heights, 0, j, pacificAdjacent);
            dfs(heights, ROWS - 1, j, atlanticAdjacent);
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (pacificAdjacent[i][j] && atlanticAdjacent[i][j]) result.add(List.of(i, j));
            }
        }

        return result;

    }

    void dfs(int[][] heights, int row, int col, boolean[][] visited) {
        if (visited[row][col]) return;
        visited[row][col] = true;

        for (int[] direction : directions) {
            int nRow = direction[0] + row;
            int nCol = direction[1] + col;

            if (nRow < 0 || nRow == ROWS || nCol < 0 || nCol == COLS || heights[nRow][nCol] < heights[row][col]) continue;
            dfs(heights, nRow, nCol, visited);
        } 
    } 
}
