// Time:  O(N.3^L) where N is the number of characters in board and L is the length of word
// Space: O(N)

class Solution {
    char[][] board;
    int ROWS;
    int COLS;
    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public boolean exist(char[][] board, String word) {
        this.ROWS = board.length;
        this.COLS = board[0].length;
        this.board = board;

        boolean[][] visited = new boolean[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (check(r, c, 0, word, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean check(int currentRow, int currentCol, int idx, String word, boolean[][] visited) {
        if (board[currentRow][currentCol] != word.charAt(idx)) return false;
        if (idx == word.length() - 1) return true;

        boolean exist = false;
        visited[currentRow][currentCol] = true;
        for (int[] direction : directions) {
            int nextRow = currentRow + direction[0];
            int nextCol = currentCol + direction[1];

            if (nextRow >= 0 && nextRow < ROWS && nextCol >= 0 && nextCol < COLS && !visited[nextRow][nextCol]) {
                exist |= check(nextRow, nextCol, idx + 1, word, visited);

                if (exist) break;
            }
        }

        visited[currentRow][currentCol] = false;
        return exist;
    }
}
