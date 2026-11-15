
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Time:  O(R*C*3^L + W) where R and C are nums of rows and cols in board. L is the length of longest word. W is number of characters in words
// Space: O(R*C + W)

class Solution {
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        String word;
    }

    TrieNode root = new TrieNode();
    int ROWS;
    int COLS;
    char[][] BOARD;
    int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    List<String> result = new ArrayList<>();

    public List<String> findWords(char[][] board, String[] words) {
        ROWS = board.length;
        COLS = board[0].length;
        BOARD = board;

        // fill prefix Trie from words
        for (String word : words) {
            insert(word);
        }

        boolean[][] visited = new boolean[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                visited[row][col] = true;
                dfs(root, row, col, visited);
                visited[row][col] = false;    
            }
        }

        return result;
    }

    void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.word = word;
    }

    void dfs(TrieNode current, int row, int col, boolean[][] visited) {
        char character = BOARD[row][col];
        if (!current.children.containsKey(character)) return;
        current = current.children.get(character);

        if (current.word != null) {
            result.add(current.word);
            current.word = null;
        }

        for (int[] direction : DIRECTIONS) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];

            if (nextRow < 0 || nextRow == ROWS || nextCol < 0 || nextCol == COLS || visited[nextRow][nextCol]) continue;
            visited[nextRow][nextCol] = true;
            dfs(current, nextRow, nextCol, visited);
            visited[nextRow][nextCol] = false;
        }
    }
}
