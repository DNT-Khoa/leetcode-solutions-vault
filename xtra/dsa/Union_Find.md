# Union-Find (Disjoint Set Union)

**Union-find** keeps track of which items belong to the same group. It answers two questions, and it answers them fast:

- `find(x)`: which group is `x` in? (It returns the group's **root**.)
- `union(a, b)`: merge the groups of `a` and `b`. (It returns `false` if they were already in the same group.)

Each group is stored as a tree, and every node points at its parent. The root points at itself and stands for the whole group.

## Contents

- [When to reach for it](#when-to-reach-for-it)
- [Core template](#core-template)
- [Why both optimizations: how paths get long](#why-both-optimizations-how-paths-get-long)
- [What α(n) means](#what-αn-means)
- [Pattern 1: Cycle detection in an undirected graph](#pattern-1-cycle-detection-in-an-undirected-graph)
- [Pattern 2: Counting connected components](#pattern-2-counting-connected-components)
- [Pattern 3: Is it a valid tree? (the n - 1 edge trick)](#pattern-3-is-it-a-valid-tree-the-n---1-edge-trick)
- [Pattern 4: Find the edge that closes the cycle](#pattern-4-find-the-edge-that-closes-the-cycle)
- [Union-find vs DFS/BFS](#union-find-vs-dfsbfs)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## When to reach for it

- The problem is about **connectivity** in an **undirected** graph: "are these two connected?", "how many groups?", "does this edge create a cycle?"
- **Edges arrive one at a time** and you need an answer after each one. DFS would have to start over after every edge; union-find just processes the new one.
- You only need **which group** a node is in, not the path to it, the distance, or an order.

It is the wrong tool for directed graphs (use DFS with three states), for shortest paths (use BFS or Dijkstra), and for ordering (use topological sort).

## Core template

Path compression plus union by size:

```java
int[] parent = new int[n];
int[] size = new int[n];
for (int i = 0; i < n; i++) parent[i] = i;   // each node is its own root
Arrays.fill(size, 1);

int find(int x) {
    if (x != parent[x])
        parent[x] = find(parent[x]);   // path compression: point straight at the root
    return parent[x];                  // NOT x
}

boolean union(int a, int b) {
    int ra = find(a), rb = find(b);
    if (ra == rb) return false;        // already in the same group

    if (size[ra] < size[rb]) {         // hang the smaller tree under the larger one
        int t = ra; ra = rb; rb = t;
    }
    parent[rb] = ra;
    size[ra] += size[rb];
    return true;
}
```

Three details to get right:

1. **`find` returns `parent[x]`, not `x`.** After the `if`, `parent[x]` holds the root. Returning `x` makes `find` hand back its own input, so `ra == rb` is only true when `a == b`, and cycle detection never fires. Path compression also breaks, because each level stores the wrong value.
2. **Compare sizes, not node indices.** `if (ra < rb)` picks a root by its label. The `size` array then gets updated but never affects anything.
3. **Return a `boolean` from `union`.** Almost every pattern below needs to know whether the merge actually happened.

Union by **rank** (an upper bound on tree height) works the same way and gives the same complexity. Size is easier to reason about, and it also tells you how big each group is.

## Why both optimizations: how paths get long

A common question: `union` already calls `find`, which compresses paths, so how can a tree ever get tall?

The answer is that **compression only flattens the path `find` actually walks.** Nodes that `find` never visits keep their old parent pointers. And the linking step `parent[ra] = rb` makes the path **one step longer for every node under `ra`**, most of which were not on the path `find` just walked.

Here's an example with no union by size, always linking `a`'s root under `b`'s root:

```
union(1, 2)   find(1)=1, find(2)=2  →  parent[1] = 2      1 → 2
union(2, 3)   find(2)=2, find(3)=3  →  parent[2] = 3      1 → 2 → 3
union(3, 4)   find(3)=3, find(4)=4  →  parent[3] = 4      1 → 2 → 3 → 4
...
```

Every `find` was called on a node that was already a root, so it walked a path of length 0 and had nothing to compress. Node 1 is never touched and ends up at the bottom of a chain of length n - 1.

So the two optimizations fix different halves of the problem:

| | What it does | Cost per operation on its own |
|---|---|---|
| Path compression | Fixes depth **after the fact**, along the paths `find` walks | amortized O(log n) |
| Union by size/rank | Stops depth from **building up** when you link | O(log n) worst case (tree height stays at most log n) |
| Both | | amortized **O(α(n))** |

## What α(n) means

α is the **inverse Ackermann function**, written as the Greek letter alpha.

The **Ackermann function** climbs a ladder of operations: addition, then multiplication (repeated addition), then exponentiation (repeated multiplication), then towers of exponents, and so on. Each step up makes it explode:

```
A(1) ≈ 3
A(2) ≈ 7
A(3) ≈ 61
A(4) ≈ 2^2^2^...^2   (a tower of 2s about 65,536 high)
```

A(4) is far larger than the number of atoms in the universe (about 10^80).

The **inverse**, α(n), asks how far up that ladder you have to go before A(k) reaches n. Because A grows that fast, α grows extremely slowly:

```
n up to about 3    →  α(n) ≤ 1
n up to about 7    →  α(n) ≤ 2
n up to about 61   →  α(n) ≤ 3
n up to A(4)       →  α(n) ≤ 4
```

So **α(n) ≤ 4 for any input you could ever store.** Tarjan proved this bound in 1975, and it's tight. You don't need the proof.

What to keep in mind:

- It's **amortized**. A single `find` can still walk O(log n) steps on a tall path. That walk compresses the path, so later calls are cheap, and the average over a sequence of operations is O(α(n)).
- It's **not technically O(1)**, because α does grow with n. Write it as O(α(n)) and say "effectively constant."
- In an interview, "O(α(n)), the inverse Ackermann function, which is effectively constant" is enough.

To type α on a Mac, press Ctrl + Cmd + Space and search "alpha". In code comments, `alpha(n)` is fine.

## Pattern 1: Cycle detection in an undirected graph

Go through the edges in order. For each edge (u, v):

- `find(u) != find(v)`: u and v are in different groups, so this edge joins two separate pieces. Union them.
- `find(u) == find(v)`: u and v are already connected by some other path. Adding this edge **closes a loop**.

```java
for (int[] e : edges) {
    if (!union(e[0], e[1])) return true;   // cycle
}
return false;
```

This only works for **undirected** graphs. In a directed graph, "already in the same group" doesn't mean you can get back to where you started.

## Pattern 2: Counting connected components

Start with `n` groups, one per node. Every **successful** union merges two groups into one, so the count goes down by 1.

```java
int components = n;
for (int[] e : edges) {
    if (union(e[0], e[1])) components--;
}
return components;
```

Isolated nodes (nodes that appear in no edge) are counted automatically, because they start as their own group and nothing ever merges them.

Used in: 323. Number of Connected Components in an Undirected Graph.

## Pattern 3: Is it a valid tree? (the n - 1 edge trick)

A graph on n nodes is a tree when **both** of these hold:

1. **No cycle.**
2. **Connected**: everything is in one group.

"No cycle" alone isn't enough. With n = 4 and edges `[[0,1],[2,3]]`, there's no cycle, but there are two separate pieces. That's a forest, not a tree.

**The shortcut:** a tree on n nodes has exactly **n - 1 edges**. Once the edge count is n - 1, the two conditions imply each other:

- If there are n - 1 edges and every union succeeds, then exactly n - 1 merges happened. The group count goes n → 1, so the graph is connected.

So you only need to check one of them:

```java
if (edges.length != n - 1) return false;
for (int[] e : edges) {
    if (!union(e[0], e[1])) return false;   // cycle
}
return true;                                 // connected for free
```

A `components == 1` check at the end would always be true at that point, so it's dead code.

Used in: 261. Graph Valid Tree.

## Pattern 4: Find the edge that closes the cycle

In 684. Redundant Connection, the input is a tree plus **exactly one** extra edge, and you return the extra edge. If there are several answers, you return the one that comes **last in the input**.

That "last in the input" rule takes care of itself:

- There's exactly one cycle, and the answer is one of its edges.
- Every cycle edge before the last one joins two separate pieces, so its union succeeds.
- The cycle only closes once all of its edges are present. That happens on the cycle edge that comes last in the input, and it's the **first union that fails**.

```java
for (int[] e : edges) {
    if (!union(e[0], e[1])) return e;   // first failure = the answer
}
return new int[0];                       // never reached; needed to compile
```

**Watch the node labels.** Nodes in this problem are numbered **1 to n**, not 0 to n - 1. Size the arrays `edges.length + 1` so index n is valid. Index 0 just goes unused. With an array of size n, an edge like `[999, 1000]` throws `ArrayIndexOutOfBoundsException` on `parent[1000]`.

## Union-find vs DFS/BFS

| | Union-find | DFS / BFS |
|---|---|---|
| Time | O(n + E · α(n)) | O(n + E) |
| Space | O(n): just `parent` and `size` | O(n + E): adjacency list, `visited`, stack/queue |
| Big-O | Slightly worse (the α factor) | Strictly linear |
| Real runtime in Java | Usually a bit faster: two `int[]`, no adjacency list, no boxing to `Integer` | Builds `List<List<Integer>>`, many small allocations |
| Can stop early on a cycle? | Yes, on the first failed union | Only after building the full adjacency list |
| Edges arriving one at a time | Natural fit | Has to start over |
| Paths, distances, order | Can't do it | Natural fit |

Which one to use depends on the question, not on speed:

- **Union-find**: incremental connectivity, "does this edge create a cycle?", Kruskal's MST.
- **DFS/BFS**: you need the actual path, the distance, an order (topological sort), or the graph is directed.

## Cost summary

| Problem | Pattern | Time | Space |
|---|---|---|---|
| 261. Graph Valid Tree | 3 | O(n · α(n)), since E = n - 1 after the check | O(n) |
| 323. Number of Connected Components | 2 | O(n + E · α(n)) | O(n) |
| 684. Redundant Connection | 4 | O(n · α(n)), since E = n | O(n) |

The recursion in `find` adds stack depth, but union by size keeps trees at most O(log n) tall, so the stack stays within O(n).

## Pitfalls checklist

- [ ] `find` returns `parent[x]` (the root), **not `x`**.
- [ ] Union by size compares `size[ra]` and `size[rb]`, **not** `ra` and `rb`.
- [ ] `union` returns `boolean`, and the caller actually checks it.
- [ ] Node labels: **1-indexed** problems need arrays of size n + 1.
- [ ] Valid tree needs **no cycle AND connected**. With the n - 1 edge check, one implies the other.
- [ ] After adding the n - 1 shortcut, delete any counter that's now always 1.
- [ ] Union-find is for **undirected** graphs. Directed cycle detection needs DFS with three states (see [Graph DFS](Graph_DFS.md)).

## Problems for further practice

Solved in this repo: [684. Redundant Connection](../../redundant-connection/notes.md). Also solved: 261. Graph Valid Tree and 323. Number of Connected Components (both LeetCode Premium).

Next, roughly in order of difficulty:

| Problem | Difficulty | Union-find idea |
|---|---|---|
| [1971. Find if Path Exists in Graph](https://leetcode.com/problems/find-if-path-exists-in-graph/) | Easy | union all edges, then compare `find(source)` and `find(destination)` |
| [547. Number of Provinces](https://leetcode.com/problems/number-of-provinces/) | Medium | Pattern 2 on an adjacency matrix |
| [990. Satisfiability of Equality Equations](https://leetcode.com/problems/satisfiability-of-equality-equations/) | Medium | union all `==` first, then check every `!=` |
| [1319. Number of Operations to Make Network Connected](https://leetcode.com/problems/number-of-operations-to-make-network-connected/) | Medium | spare edges = failed unions; answer = components - 1 |
| [721. Accounts Merge](https://leetcode.com/problems/accounts-merge/) | Medium | union accounts that share an email |
| [1584. Min Cost to Connect All Points](https://leetcode.com/problems/min-cost-to-connect-all-points/) | Medium | Kruskal's MST: sort edges by cost, union while it succeeds |
| [128. Longest Consecutive Sequence](https://leetcode.com/problems/longest-consecutive-sequence/) | Medium | union `x` with `x + 1`; the answer is the largest `size` |
| [685. Redundant Connection II](https://leetcode.com/problems/redundant-connection-ii/) | Hard | directed version: handle a node with two parents separately |
| [778. Swim in Rising Water](https://leetcode.com/problems/swim-in-rising-water/) | Hard | add cells in order of height until top-left and bottom-right connect |

Related docs: [Graph DFS](Graph_DFS.md), [Graph BFS](Graph_BFS.md), [Topological Sort](Topological_Sort.md).
