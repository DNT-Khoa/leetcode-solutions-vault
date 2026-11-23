// Time:  O(R*C) where R is number of rows and C is number of cols
// Space: O(R*C)

class Solution {
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static int ROWS;
    private static int COLS;

    public void solve(char[][] board) {
        ROWS = board.length;
        COLS = board[0].length;

        // first find all the 'O' cells that are on the edge and run dfs to mark its region safe with 'S'
        for (int r = 0; r < ROWS; r++) {
            if (board[r][0] == 'O') dfs(r, 0, board);
            if (board[r][COLS-1] == 'O') dfs(r, COLS-1, board);
        }
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] == 'O') dfs(0, c, board);
            if (board[ROWS-1][c] == 'O') dfs(ROWS-1, c, board);
        }

        // now loop through the grid and flip all cells 'O' to 'X' and 'S' to 'O'
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == 'O') board[r][c] = 'X';
                if (board[r][c] == 'S') board[r][c] = 'O';
            }
        }
    }

    private void dfs(int row, int col, char[][] board) {
        board[row][col] = 'S';

        for (int[] direction : DIRECTIONS) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];

            if (nextRow < 0 || nextRow == ROWS || nextCol < 0 || nextCol == COLS) continue;
            if (board[nextRow][nextCol] != 'O') continue;
            dfs(nextRow, nextCol, board);
        }
    }
}
