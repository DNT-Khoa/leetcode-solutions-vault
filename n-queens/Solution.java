
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Time:  O(N!.N) 
// Space: O(N^2)
// Notes: row 0 has N choices, row 1 has at most N-1, row 2 at most N-2, … so the recursion tree is bounded by N! paths
// At each node you also do O(N) work — the for col loop and buildRow
// For space complexity, board list holding up to N strings of length N: O(N²)

class Solution {
    private List<List<String>> result;

    public List<List<String>> solveNQueens(int n) {
        result = new ArrayList<>();
        dfs(0, new HashSet<>(), new HashSet<>(), new HashSet<>(), n, new ArrayList<>());
        return result;
    }

    void dfs(int row, Set<Integer> visitedCols, Set<Integer> visitedDiags, Set<Integer> visitedAntiDags, int n, List<String> board) {
        if (row == n) {
            result.add(new ArrayList<>(board));
            return;
        }

        for (int col = 0; col < n; col++) {
            if (visitedCols.contains(col) || visitedDiags.contains(row - col) || visitedAntiDags.contains(row + col)) 
                continue;
            visitedCols.add(col);
            visitedDiags.add(row - col);
            visitedAntiDags.add(row + col);
            board.add(buildRow(n, col));

            dfs(row + 1, visitedCols, visitedDiags, visitedAntiDags, n, board);

            visitedCols.remove(col);
            visitedDiags.remove(row - col);
            visitedAntiDags.remove(row + col);
            board.remove(board.size() - 1);
        }
    }

    String buildRow(int n, int col) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i == col) s.append('Q');
            else s.append('.');
        }
        return s.toString();
    }
}
