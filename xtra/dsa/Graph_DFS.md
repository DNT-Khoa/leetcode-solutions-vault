# Graph DFS Cheatsheet

Personal reference for depth-first search on graphs and grids: recursive and iterative templates, the "mutate the grid" trick, cycle detection with three colors, and topological sort.

---

## 1. The template (grid, recursive)

```java
class Solution {
    private int ROWS, COLS;
    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    int dfs(char[][] grid, int r, int c) {
        if (r < 0 || c < 0 || r == ROWS || c == COLS) return 0;
        if (grid[r][c] == '0') return 0;

        grid[r][c] = '0';                 // mark visited (in-place)

        int area = 1;
        for (int[] d : DIRS) {
            area += dfs(grid, r + d[0], c + d[1]);
        }
        return area;
    }
}
```

Two invariants that make grid DFS bug-free:

1. **Bounds + water check first**, then mark. If you mark before the bounds check, you'll write out of the array.
2. **Mark visited before recursing**, not after. Otherwise a neighbor will recurse straight back into the current cell and you'll loop forever (or stack-overflow).

Hoist `DIRS` to a `static final` field — otherwise you re-allocate a 4×2 array on every recursive call.

---

## 2. The template (general graph, recursive)

```java
void dfs(int node, Map<Integer, List<Integer>> adj, Set<Integer> visited) {
    if (!visited.add(node)) return;              // add returns false if already present

    for (int neighbor : adj.getOrDefault(node, List.of())) {
        dfs(neighbor, adj, visited);
    }
}
```

`Set.add` doubles as the visited check and the mark — one call instead of `contains` + `add`.

---

## 3. In-place mark vs. `visited` array

| Approach                     | Pros                                     | Cons                                          |
|------------------------------|------------------------------------------|-----------------------------------------------|
| Mutate grid (`'1'` → `'0'`)  | O(1) extra space beyond recursion stack  | Destroys the input                            |
| Separate `boolean[][]`       | Preserves the input                      | O(M·N) extra space                            |
| Mutate + restore on backtrack| Preserves input, still O(1) extra        | Only correct when you need to enumerate paths (see §6) |

For counting/area problems (Number of Islands, Max Area of Island), mutate the grid. If the caller might use the grid again, use a `visited` array.

---

## 4. Iterative DFS (with an explicit stack)

Recursion depth on a grid can be `M·N` in the worst case (a snake-shaped island). If that risks a `StackOverflowError`, convert to an iterative stack:

```java
void dfsIterative(char[][] grid, int startR, int startC) {
    Deque<int[]> stack = new ArrayDeque<>();
    stack.push(new int[]{startR, startC});
    grid[startR][startC] = '0';

    while (!stack.isEmpty()) {
        int[] cell = stack.pop();
        for (int[] d : DIRS) {
            int nr = cell[0] + d[0], nc = cell[1] + d[1];
            if (nr < 0 || nc < 0 || nr == ROWS || nc == COLS) continue;
            if (grid[nr][nc] == '0') continue;
            grid[nr][nc] = '0';        // mark on PUSH, mirrors "mark on enqueue" for BFS
            stack.push(new int[]{nr, nc});
        }
    }
}
```

Use `ArrayDeque` as a stack (`push` / `pop`) — never `java.util.Stack`.

Iterative DFS does **not** visit nodes in the same order as recursive DFS unless you also reverse the neighbor list before pushing. Order only matters when you're producing traversal output (preorder listings, path enumeration); for connectivity / area / cycle detection it doesn't.

---

## 5. When DFS vs BFS (mirror of the BFS cheatsheet)

| Situation                                    | Prefer |
|----------------------------------------------|--------|
| Shortest path / fewest steps (unweighted)    | BFS    |
| Count connected components / island area     | Either |
| Enumerate all paths, backtrack               | DFS    |
| Cycle detection in a directed graph          | DFS (3-color) |
| Topological sort                             | DFS (post-order) or Kahn's BFS |
| Need parent / ancestor / entry-exit times    | DFS    |
| Very deep grid, worried about stack overflow | BFS or iterative DFS |

---

## 6. Path enumeration (DFS with backtracking)

When you need every path (not just "does one exist"), the mark-and-leave-marked trick fails — you'd never revisit a node from a different path. Instead, mark on entry and **unmark on exit**:

```java
void allPaths(int node, int target, List<Integer> path, Set<Integer> onPath,
              Map<Integer, List<Integer>> adj, List<List<Integer>> out) {
    path.add(node);
    onPath.add(node);

    if (node == target) {
        out.add(new ArrayList<>(path));       // COPY — path is mutated on backtrack
    } else {
        for (int nb : adj.getOrDefault(node, List.of())) {
            if (!onPath.contains(nb)) allPaths(nb, target, path, onPath, adj, out);
        }
    }

    path.remove(path.size() - 1);             // backtrack
    onPath.remove(node);
}
```

`onPath` is not "have I ever visited this node?" — it's "is this node currently on the recursion stack?" That's what prevents cycles without blocking legitimate re-use across paths.

---

## 7. Cycle detection in a directed graph (three-color DFS)

Every node is in one of three states:

- **WHITE (0)** — not yet visited
- **GRAY  (1)** — currently on the recursion stack
- **BLACK (2)** — fully explored

An edge to a **GRAY** node is a back-edge → cycle.

```java
int[] color;   // 0 = WHITE, 1 = GRAY, 2 = BLACK

boolean hasCycle(int node, List<List<Integer>> adj) {
    color[node] = 1;                       // GRAY: on the stack
    for (int nb : adj.get(node)) {
        if (color[nb] == 1) return true;   // back-edge → cycle
        if (color[nb] == 0 && hasCycle(nb, adj)) return true;
    }
    color[node] = 2;                       // BLACK: done
    return false;
}
```

Two colors (visited / not-visited) are enough for **undirected** graphs — but you must also pass the parent to skip the trivial "back to where I came from" edge. Three colors are required for directed graphs, where an edge to a BLACK node is fine (it's a cross-edge or forward-edge, not a cycle).

---

## 8. Topological sort (DFS post-order)

For a DAG, a valid topological order is the **reverse of the DFS post-order**.

```java
void topoDfs(int node, List<List<Integer>> adj, int[] color, Deque<Integer> stack) {
    color[node] = 1;
    for (int nb : adj.get(node)) {
        if (color[nb] == 1) throw new IllegalStateException("cycle");
        if (color[nb] == 0) topoDfs(nb, adj, color, stack);
    }
    color[node] = 2;
    stack.push(node);            // push on FINISH → pop yields reverse post-order
}
```

Loop `topoDfs` over every unvisited node, then drain `stack` — that's your topological order. Kahn's algorithm (BFS on in-degree zero) is the iterative alternative and gives the same guarantees.

---

## 9. Complexity

- **Time: O(V + E)** — each vertex marked once, each edge examined once.
  - Grid: `V = M·N`, `E ≤ 4·M·N`, so **O(M·N)**.
- **Space: O(V)** for the visited structure (or `O(1)` extra if you mutate the grid).
  - Recursion stack is also `O(V)` worst case — snake-shaped island, long path — so total space is `O(V)` either way unless you go iterative.

---

## 10. Common pitfalls

1. **Marking after recursion instead of before** — infinite recursion between adjacent cells.
2. **Forgetting the bounds check** — `ArrayIndexOutOfBoundsException` at the grid edges.
3. **Re-allocating `DIRS` inside the recursion** — a `static final` field is essentially free.
4. **Mutating the grid in a function whose caller still needs it** — surprises everyone.
5. **Using two-color visited on a directed graph for cycle detection** — misses back-edges through already-finished nodes; use the three-color scheme in §7.
6. **Backtracking without undoing state** — path enumeration must `remove` the node on exit; forgetting is the #1 backtracking bug.
