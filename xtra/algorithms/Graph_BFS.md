# Graph BFS Cheatsheet

Personal reference for breadth-first search on graphs and grids: templates, the "mark-when-enqueued" rule, level-by-level scanning, and multi-source BFS.

---

## 1. The template (grid)

```java
int bfs(char[][] grid, int startRow, int startCol) {
    int rows = grid.length, cols = grid[0].length;
    boolean[][] visited = new boolean[rows][cols];
    Queue<int[]> queue = new ArrayDeque<>();

    queue.offer(new int[]{startRow, startCol});
    visited[startRow][startCol] = true;   // mark on ENQUEUE, not on poll

    int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int count = 0;

    while (!queue.isEmpty()) {
        int[] cell = queue.poll();
        count++;
        for (int[] d : DIRS) {
            int nr = cell[0] + d[0], nc = cell[1] + d[1];
            if (nr < 0 || nc < 0 || nr == rows || nc == cols) continue;
            if (visited[nr][nc] || grid[nr][nc] == '0') continue;
            visited[nr][nc] = true;
            queue.offer(new int[]{nr, nc});
        }
    }
    return count;
}
```

Use `ArrayDeque`, not `LinkedList` — same `Queue` API, but faster and lower per-node overhead. Never use `java.util.Stack` or `Vector` for anything.

---

## 2. The template (general graph)

```java
void bfs(Map<Integer, List<Integer>> adj, int start) {
    Set<Integer> visited = new HashSet<>();
    Queue<Integer> queue = new ArrayDeque<>();

    queue.offer(start);
    visited.add(start);

    while (!queue.isEmpty()) {
        int node = queue.poll();
        for (int neighbor : adj.getOrDefault(node, List.of())) {
            if (!visited.add(neighbor)) continue;   // add returns false if present
            queue.offer(neighbor);
        }
    }
}
```

`Set.add` returns `false` if the element was already present — one call replaces the `contains` + `add` pair.

---

## 3. The critical rule: mark visited on **enqueue**, not on **poll**

If you only mark when polling, the same node can be enqueued many times before it's finally processed:

```
A -> B, A -> C, B -> D, C -> D
```

Enqueue A. Poll A, mark A visited, enqueue B and C. Poll B, mark B, enqueue D. Poll C, mark C — but D is already in the queue and not yet marked, so C enqueues D **again**. On dense graphs this blows up to O(V·E) work and can also break shortest-path correctness.

**Rule:** the moment a node enters the queue, mark it. It is now "claimed" — no future neighbor will re-enqueue it.

---

## 4. Level-by-level BFS (shortest path in unweighted graphs)

Snapshot the queue size at the top of each iteration — that's how many nodes belong to the current level:

```java
int shortestPath(int[][] grid, int[] start, int[] target) {
    Queue<int[]> queue = new ArrayDeque<>();
    // ... seed + mark start visited ...

    int steps = 0;
    while (!queue.isEmpty()) {
        int size = queue.size();          // freeze the current level
        for (int i = 0; i < size; i++) {
            int[] cell = queue.poll();
            if (cell[0] == target[0] && cell[1] == target[1]) return steps;
            // enqueue unvisited neighbors...
        }
        steps++;                          // finished one wavefront
    }
    return -1;
}
```

Why this works: BFS visits nodes in non-decreasing order of distance from the source. The first time you dequeue the target, `steps` equals its shortest distance in edges.

Only correct when **all edges have the same weight**. For weighted graphs use Dijkstra; for 0/1-weighted, use 0-1 BFS (`ArrayDeque` with `addFirst` for 0-weight edges).

---

## 5. Multi-source BFS

Seed the queue with **all** sources first, then run a single BFS. Every explored node is labeled with the distance to its nearest source, in one pass.

```java
Queue<int[]> queue = new ArrayDeque<>();
for (each source (r, c)) {
    queue.offer(new int[]{r, c});
    dist[r][c] = 0;
}
// then run standard level-by-level BFS
```

Classic problems: `994. Rotting Oranges`, `542. 01 Matrix`, `286. Walls and Gates`. The trick beats "BFS from each source separately" (O(sources · V)) down to a single O(V + E) pass.

---

## 6. BFS vs DFS on grids

| Situation                                    | Prefer |
|----------------------------------------------|--------|
| Shortest path / fewest steps (unweighted)    | BFS    |
| Count connected components / island area     | Either |
| Enumerate all paths, backtrack               | DFS    |
| Very deep grid, worried about stack overflow | BFS (iterative) |
| Need parent/ancestor info from recursion     | DFS    |

For `200. Number of Islands` and `695. Max Area of Island`, DFS and BFS both work; BFS is safer on huge grids because it uses a heap-allocated queue instead of the call stack.

---

## 7. Complexity

- **Time: O(V + E)** — each vertex enqueued once, each edge relaxed once.
  - Grid: `V = M·N`, `E ≤ 4·M·N`, so **O(M·N)**.
- **Space: O(V)** — the `visited` structure dominates.
  - Queue peak is the largest level (`O(min(M, N))` on a grid; `O(V)` worst case on a star graph), which fits inside the same bound.

---

## 8. Common pitfalls

1. **Marking on poll instead of enqueue** — see §3.
2. **Using `LinkedList` for the queue** — works, but slow. Use `ArrayDeque`.
3. **Level-by-level without the size snapshot** — reading `queue.size()` inside the inner loop mixes levels together and gives wrong distances.
4. **Applying plain BFS to weighted graphs** — the "first dequeue wins" invariant only holds when all edges have equal weight.
5. **Forgetting to mark the start visited** — otherwise a neighbor loops back and re-enqueues it.
