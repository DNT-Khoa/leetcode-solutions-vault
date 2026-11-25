# Graph BFS Cheatsheet

Personal reference for breadth-first search on graphs and grids: templates, the "mark-when-enqueued" rule, level-by-level scanning, multi-source BFS, and implicit graphs where neighbors are generated on the fly.

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

## 6. Implicit-graph BFS (generate neighbors on the fly)

Sometimes the graph isn't given as edges. Nodes are **states** (words, lock combinations, board layouts), and two states are neighbors when one small change turns one into the other. `127. Word Ladder` is the classic example: words are nodes, and two words share an edge when they differ by exactly one letter.

### Don't compare every pair

The obvious way to find edges is to compare every word with every other word:

```
N words × N words × L letters per comparison = O(N² · L)
N = 5000, L = 10  →  about 250 million steps
```

**Generate the neighbors instead.** The alphabet is small and fixed (26 letters) while N can be large, so it's cheaper to go through the alphabet than through the word list. For each position, try all 26 letters and check whether the result is a real word:

```
"hot"
position 0:  aot, bot, cot, dot ✓, ..., lot ✓, ..., zot
position 1:  hat, hbt, ..., hzt
position 2:  hoa, hob, ..., hoz

26 × 3 = 78 candidates; only "dot" and "lot" are in the word set.
```

Put the word list in a `HashSet` so each check is O(1) on average.

### No adjacency list needed

An adjacency list is only a cache of "who are this node's neighbors?", and BFS asks that exactly once per node, when the node comes off the queue. So compute the answer right then:

- **No extra work up front.** BFS often reaches the target before touching every word; building the full graph first would pay for words BFS never visits.
- **No extra memory.** Just the set and the queue.
- **Same cost per word** either way, so precomputing only moves the work earlier.

(Precomputing is still valid. A common way is wildcard buckets: `"hot"` goes under `"*ot"`, `"h*t"` and `"ho*"`, and words in the same bucket are neighbors. Same time complexity.)

### The template

```java
Set<String> words = new HashSet<>(wordList);     // mutable copy: we remove from it
Deque<String> queue = new ArrayDeque<>();
queue.offer(beginWord);
words.remove(beginWord);
int steps = 1;                                   // Word Ladder counts WORDS, not edges

while (!queue.isEmpty()) {
    int size = queue.size();
    for (int k = 0; k < size; k++) {
        String word = queue.poll();
        if (word.equals(endWord)) return steps;

        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char original = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == original) continue;     // that's the word itself
                chars[i] = c;
                String candidate = new String(chars);
                if (words.remove(candidate)) {   // true if present: visited check + mark in one call
                    queue.offer(candidate);
                }
            }
            chars[i] = original;                 // restore before the next position
        }
    }
    steps++;
}
return 0;
```

The rule from §3 still applies: removing from `words` is the visited mark, and it happens **on enqueue**. Without it, `"hot" → "dot" → "hot" → ...` gets enqueued forever. If the target is unreachable, the queue never empties and the loop never ends.

### Changing one character: Java details

- **Convert to `char[]` once per word**, change one slot, then **restore it** after each position. If you forget to restore, position 1's candidates build on position 0's last letter (`'z'`), and you generate words two letters away.
- **Avoid `substring` concatenation.** `word.substring(0, i) + c + word.substring(i + 1)` creates several temporary strings per candidate. Changing the `char[]` and calling `new String(chars)` creates one.
- **`new String(chars)` or `String.valueOf(chars)`, never `chars.toString()`.** Arrays don't override `toString()`, so `chars.toString()` gives something like `"[C@1b6d3586"`. It compiles and runs, never matches a word, and BFS silently returns 0. (`Arrays.toString(chars)` gives `"[h, o, t]"`, which is only useful for debugging.)
- **`new HashSet<>(list)`, not `Set.copyOf(list)`.** `Set.copyOf` returns an immutable set, and `remove` throws `UnsupportedOperationException`.
- **`char` works as a loop variable**: `for (char c = 'a'; c <= 'z'; c++)`.

### Why O(N · 26 · L²) and not O(N · 26 · L)

```
for each word dequeued                    →  N      (each word enqueued at most once)
  for each position                       →  L
    for each letter                       →  26
      new String(chars)                   →  O(L)   copies L characters
      words.remove(candidate)             →  O(L)   hashing reads all L characters
```

The first L is how many **positions** there are. The second L is what **each candidate** costs: building a string and hashing it both read the whole string, so neither is O(1). Total: N × 26 · L × L = O(N · 26 · L²), often written O(N · L²) because 26 is a constant.

Space is O(N · L): the set and the queue hold up to N strings of length L.

---

## 7. BFS vs DFS on grids

| Situation                                    | Prefer |
|----------------------------------------------|--------|
| Shortest path / fewest steps (unweighted)    | BFS    |
| Count connected components / island area     | Either |
| Enumerate all paths, backtrack               | DFS    |
| Very deep grid, worried about stack overflow | BFS (iterative) |
| Need parent/ancestor info from recursion     | DFS    |

For `200. Number of Islands` and `695. Max Area of Island`, DFS and BFS both work; BFS is safer on huge grids because it uses a heap-allocated queue instead of the call stack.

---

## 8. Complexity

- **Time: O(V + E)** — each vertex enqueued once, each edge relaxed once.
  - Grid: `V = M·N`, `E ≤ 4·M·N`, so **O(M·N)**.
- **Space: O(V)** — the `visited` structure dominates.
  - Queue peak is the largest level (`O(min(M, N))` on a grid; `O(V)` worst case on a star graph), which fits inside the same bound.

---

## 9. Common pitfalls

1. **Marking on poll instead of enqueue** — see §3.
2. **Using `LinkedList` for the queue** — works, but slow. Use `ArrayDeque`.
3. **Level-by-level without the size snapshot** — reading `queue.size()` inside the inner loop mixes levels together and gives wrong distances.
4. **Applying plain BFS to weighted graphs** — the "first dequeue wins" invariant only holds when all edges have equal weight.
5. **Forgetting to mark the start visited** — otherwise a neighbor loops back and re-enqueues it.
6. **Implicit graphs: never marking visited at all** — an unreachable target means the queue never empties (§6).
7. **Comparing every pair to find implicit edges** — O(N² · L); generate candidates from the alphabet instead (§6).
8. **`chars.toString()` instead of `new String(chars)`** — returns `"[C@..."`, matches nothing, fails silently.
9. **`Set.copyOf` when you need to `remove`** — it's immutable; use `new HashSet<>(list)`.

Related docs: [Graph DFS](Graph_DFS.md), [Union-Find](Union_Find.md), [Topological Sort](Topological_Sort.md).
