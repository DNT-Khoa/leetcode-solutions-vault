# Graph DFS Cheatsheet

Personal reference for depth-first search on graphs and grids: recursive and iterative templates, the "mutate the grid" trick, cycle detection (three colors for directed, parent check for undirected), counting components, and topological sort.

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

An edge to a **BLACK** node is fine in a directed graph. Here's why a plain visited flag isn't enough:

```
    A
   ↙ ↘
  B   C
   ↘ ↙
    D
```

DFS goes A → B → D and finishes D (BLACK). Then A → C → D: D is already visited, but there's no cycle, because you can never get from D back to A. Two paths just met at D. Only an edge to a **GRAY** node (one still on the current path) means you've looped back.

Undirected graphs work differently; see §8.

---

## 8. Cycle detection in an undirected graph (parent check)

In an undirected graph, reaching **any** visited node is a cycle, with one exception: the node you just came from.

### Why the parent is the exception

An undirected edge u - v is stored in **both** directions: `adj[u]` has v and `adj[v]` has u. So from v you always see u, and u is always visited, because you just came from there.

Take the smallest tree there is, `0 - 1`, and check cycles with only `visited`:

1. `dfs(0)` marks 0 and goes to 1.
2. `dfs(1)` marks 1 and sees neighbor 0.
3. 0 is visited, so it reports a cycle. **Wrong.** It's the same edge seen from the other end.

So pass the parent and skip it:

```java
boolean hasCycle(int node, int parent, List<List<Integer>> adj, boolean[] visited) {
    visited[node] = true;
    for (int nb : adj.get(node)) {
        if (nb == parent) continue;                 // the edge we came in on
        if (visited[nb]) return true;               // a second way back: real cycle
        if (hasCycle(nb, node, adj, visited)) return true;
    }
    return false;
}
```

`visited` tells you a node was seen, not **why**. The parent check tells the two cases apart:

| Visited neighbor is... | Meaning |
|---|---|
| Your parent | the same edge seen from the other end, so ignore it |
| Any other node | a second path back to it, so it's a real cycle |

(If the input can contain the same edge twice, e.g. `[[0,1],[1,0]]`, skipping by node would hide that 2-node cycle. Skip by edge index in that case. LeetCode tree problems normally rule this out.)

### Why two states are enough here but not in directed graphs

In the directed diamond in §7, a **separate branch** reached a node that was already finished, and that was harmless. In an undirected graph that can't happen: if a later branch can reach a node, that node can also reach the branch, so DFS would have gone there **before** finishing the node. Any visited node you reach (other than your parent) is one you can get back to, which means a cycle.

| | Directed | Undirected |
|---|---|---|
| Can a separate branch reach a finished node? | Yes (the diamond) | No |
| What counts as a cycle | an edge to a GRAY node | an edge to any visited node except the parent |
| State needed | 3 colors | `visited` + parent |

### When you don't need the parent check: counting components

The parent check exists only so cycle detection doesn't report a false cycle. If you're **not** looking for cycles, you don't need it. In 323. Number of Connected Components, a visited neighbor just gets skipped, whether it's the parent or not, so plain `visited` is enough:

```java
int count = 0;
for (int node = 0; node < n; node++) {
    if (visited[node]) continue;
    dfs(node, adj, visited);     // marks the whole component
    count++;                     // one DFS = one component
}
```

The outer loop goes over **every** node, so isolated nodes (no edges) still count as components. This is Number of Islands with an adjacency list in place of a grid.

### Valid tree with DFS (261)

A tree needs **no cycle and connected**. Check `edges.length == n - 1` first; then the two imply each other, so you only need to check connectivity: DFS from node 0 and make sure every node got visited. No parent check needed. See [Union-Find](Union_Find.md#pattern-3-is-it-a-valid-tree-the-n---1-edge-trick) for why.

---

## 9. Topological sort (DFS post-order)

For a DAG, a valid topological order is the **reverse of the DFS post-order**: run the three-color DFS from §7 and record each node when it turns BLACK. Fill the result array from the back (or push onto a stack) to reverse it.

Full details, Kahn's algorithm (BFS on in-degree), and the edge-direction trap are in [Topological Sort](Topological_Sort.md).

---

## 10. Complexity

- **Time: O(V + E)** — each vertex marked once, each edge examined once.
  - Grid: `V = M·N`, `E ≤ 4·M·N`, so **O(M·N)**.
- **Space: O(V)** for the visited structure (or `O(1)` extra if you mutate the grid).
  - Recursion stack is also `O(V)` worst case — snake-shaped island, long path — so total space is `O(V)` either way unless you go iterative.

---

## 11. Common pitfalls

1. **Marking after recursion instead of before** — infinite recursion between adjacent cells.
2. **Forgetting the bounds check** — `ArrayIndexOutOfBoundsException` at the grid edges.
3. **Re-allocating `DIRS` inside the recursion** — a `static final` field is essentially free.
4. **Mutating the grid in a function whose caller still needs it** — surprises everyone.
5. **Using two-color visited on a directed graph for cycle detection** — misses back-edges through already-finished nodes; use the three-color scheme in §7.
6. **Backtracking without undoing state** — path enumeration must `remove` the node on exit; forgetting is the #1 backtracking bug.
7. **Undirected cycle detection without the parent check** — every edge looks like a cycle, so every tree fails (§8).
8. **Adding undirected edges in one direction only** — `adj[u].add(v)` needs a matching `adj[v].add(u)`.
9. **Looping only over nodes that appear in edges** — isolated nodes are components too; loop `0..n-1`.

Related docs: [Graph BFS](Graph_BFS.md), [Union-Find](Union_Find.md), [Topological Sort](Topological_Sort.md).
