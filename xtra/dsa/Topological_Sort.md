# Topological Sort

A **topological order** of a directed graph lists every node so that **each edge points forward**: if there's an edge u → v, then u comes before v. Think of "take this course before that one" or "build this module before that one."

An order like that exists **only if the graph has no cycle** (a DAG, directed acyclic graph). If A needs B and B needs A, neither can go first. So every topological sort algorithm is also a cycle detector.

## Contents

- [When to reach for it](#when-to-reach-for-it)
- [Building the graph: get the edge direction right](#building-the-graph-get-the-edge-direction-right)
- [Approach 1: DFS post-order (three states)](#approach-1-dfs-post-order-three-states)
- [Approach 2: Kahn's algorithm (BFS on in-degree)](#approach-2-kahns-algorithm-bfs-on-in-degree)
- [DFS vs Kahn's](#dfs-vs-kahns)
- [Why three states in directed graphs](#why-three-states-in-directed-graphs)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## When to reach for it

- The input describes **dependencies**: prerequisites, "a must come before b", build order, a derived ordering of letters.
- The question is **"can all of this be done?"** (is there a cycle?) or **"in what order?"**
- The graph is **directed**. For undirected graphs, "order" doesn't mean anything; use [union-find](Union_Find.md) or DFS with a parent check.

## Building the graph: get the edge direction right

In 207/210, `prerequisites[i] = [a, b]` means "to take a, you must first take b." So the edge goes **b → a**: from the thing that has to come first to the thing that depends on it.

```java
Map<Integer, List<Integer>> adj = new HashMap<>();
for (int[] p : prerequisites) {
    adj.computeIfAbsent(p[1], k -> new ArrayList<>()).add(p[0]);   // b → a
}
```

If you flip it to a → b, cycle detection still works (a reversed cycle is still a cycle), but the order you get is **backwards**. Always ask: which one must come first? The edge points **out of** that one.

Use `new ArrayList<>()` with the diamond. Raw `new ArrayList()` compiles but gives an unchecked warning.

## Approach 1: DFS post-order (three states)

Each node is in one of three states:

| State | Meaning |
|---|---|
| 0 (unvisited) | not seen yet |
| 1 (visiting) | on the current DFS path |
| 2 (visited) | fully explored, along with everything it leads to |

Reaching a node in state **1** means you followed edges back onto your own path, so there's a **cycle**. Reaching a node in state **2** is fine; it was finished on an earlier branch.

A node is finished only after everything it points to is finished. So the **post-order** (order of finishing) puts each node *after* everything that depends on it. **Reverse it** to get the topological order.

The simple way to reverse it: fill the result array **from the back**.

```java
int[] state;
int[] order;
int idx;                                   // next free slot, counting down

public int[] findOrder(int n, int[][] prerequisites) {
    // build adj (b → a) ...
    state = new int[n];
    order = new int[n];
    idx = n - 1;

    for (int i = 0; i < n; i++) {
        if (hasCycle(i, adj)) return new int[0];
    }
    return order;
}

boolean hasCycle(int node, Map<Integer, List<Integer>> adj) {
    if (state[node] == 2) return false;    // already finished
    if (state[node] == 1) return true;     // back onto the current path: cycle

    state[node] = 1;
    for (int next : adj.getOrDefault(node, List.of())) {
        if (hasCycle(next, adj)) return true;    // stop right away
    }
    state[node] = 2;
    order[idx--] = node;                   // post-order, written from the back
    return false;
}
```

The outer loop runs over **every** node, because the graph can be in several pieces.

Return `true` as soon as a recursive call finds a cycle. Collecting results with `result |= hasCycle(...)` gives the right answer, but it keeps exploring after the answer is known, and in 210 it would keep writing into `order` after the result is already invalid.

Another way to reverse: push each finished node onto an `ArrayDeque` with `push`, then pop them all off. Filling from the back avoids the extra structure.

## Approach 2: Kahn's algorithm (BFS on in-degree)

The **in-degree** of a node is how many edges point into it, which is how many things it's still waiting on.

1. Count every node's in-degree.
2. Put every node with in-degree 0 in a queue. Those have nothing left to wait for.
3. Take a node off the queue and add it to the order. For each node it points to, lower that node's in-degree by 1. If one reaches 0, add it to the queue.
4. When the queue is empty: if the order has **all n nodes**, it's valid. If not, the remaining nodes are stuck in a cycle.

```java
int[] indegree = new int[n];
for (int[] p : prerequisites) indegree[p[0]]++;     // edge p[1] → p[0]

Deque<Integer> queue = new ArrayDeque<>();
for (int i = 0; i < n; i++) {
    if (indegree[i] == 0) queue.offer(i);
}

int[] order = new int[n];
int count = 0;
while (!queue.isEmpty()) {
    int node = queue.poll();
    order[count++] = node;
    for (int next : adj.getOrDefault(node, List.of())) {
        if (--indegree[next] == 0) queue.offer(next);
    }
}

return count == n ? order : new int[0];   // count < n → cycle
```

A cycle shows up as nodes whose in-degree **never reaches 0**. Each node in the cycle is waiting on another node in the cycle, so none of them ever enters the queue. You don't need a separate cycle check, only `count == n`.

For 207 (only "can you finish?"), skip the `order` array and just count.

## DFS vs Kahn's

| | DFS post-order | Kahn's |
|---|---|---|
| How it detects a cycle | Reaches a node in state 1 | `count < n` at the end |
| Order it produces | Reverse of finishing order | Order of removal, ready to use |
| Recursion | Yes, can overflow on long chains | No, iterative |
| Extra state | `state[]` | `indegree[]` + queue |
| Easier for | Already writing DFS, or need finish times | "Process in waves", smallest order with a min-heap, counting levels |

Both are O(V + E). Swapping the queue in Kahn's for a `PriorityQueue` gives the **smallest topological order in dictionary order**, which DFS can't do easily.

## Why three states in directed graphs

In a directed graph, reaching an already-visited node is **not always a cycle**. Take this diamond:

```
    A
   ↙ ↘
  B   C
   ↘ ↙
    D
```

DFS goes A → B → D and finishes D. Then it backtracks and goes A → C → D. D is already visited, but there's no cycle: you can never get from D back to A. Two paths just met at D.

So "visited" alone isn't enough. You need to know whether the node is **on the path you're currently exploring** (state 1, a real cycle) or **finished on an earlier branch** (state 2, safe).

Undirected graphs are different: there, plain `visited` plus a parent check is enough. See [Graph DFS](Graph_DFS.md#8-cycle-detection-in-an-undirected-graph-parent-check).

## Cost summary

| Problem | Approach | Time | Space |
|---|---|---|---|
| 207. Course Schedule | DFS three states (or Kahn's, counting only) | O(V + E) | O(V + E) |
| 210. Course Schedule II | DFS post-order filled from the back (or Kahn's) | O(V + E) | O(V + E) |

V = number of courses, E = number of prerequisites. Space covers the adjacency list, the state or in-degree array, and the recursion stack or queue.

## Pitfalls checklist

- [ ] **Edge direction**: `[a, b]` "a needs b" is the edge **b → a**.
- [ ] Directed cycle detection uses **three states**, not a plain `visited`.
- [ ] Set state 2 and record the node **after** the loop over neighbors, not before.
- [ ] The outer loop runs over **every** node, including ones with no edges.
- [ ] DFS: return as soon as a cycle is found. Don't keep writing into the result.
- [ ] DFS: the post-order is **reversed**. Fill from the back or pop from a stack.
- [ ] Kahn's: check `count == n` at the end. That's the cycle check.
- [ ] Nodes with no edges are missing from a `Map` adjacency list. Use `getOrDefault(node, List.of())`.

## Problems for further practice

Solved in this repo: [207. Course Schedule](../../course-schedule/notes.md), [210. Course Schedule II](../../course-schedule-ii/notes.md).

Next, roughly in order of difficulty:

| Problem | Difficulty | Idea |
|---|---|---|
| [1557. Minimum Number of Vertices to Reach All Nodes](https://leetcode.com/problems/minimum-number-of-vertices-to-reach-all-nodes/) | Medium | the answer is every node with in-degree 0 |
| [802. Find Eventual Safe States](https://leetcode.com/problems/find-eventual-safe-states/) | Medium | three-state DFS: safe means it finishes without reaching a cycle |
| [1462. Course Schedule IV](https://leetcode.com/problems/course-schedule-iv/) | Medium | topological order, then pass each node's prerequisite set forward |
| [310. Minimum Height Trees](https://leetcode.com/problems/minimum-height-trees/) | Medium | Kahn's-style peeling of leaves from the outside in (undirected) |
| [2115. Find All Possible Recipes from Given Supplies](https://leetcode.com/problems/find-all-possible-recipes-from-given-supplies/) | Medium | Kahn's with supplies as the starting nodes |
| [269. Alien Dictionary](https://leetcode.com/problems/alien-dictionary/) | Hard (Premium) | build edges from the first different letter of adjacent words, then sort |
| [329. Longest Increasing Path in a Matrix](https://leetcode.com/problems/longest-increasing-path-in-a-matrix/) | Hard | DFS with memo, or Kahn's by levels on the grid |
| [1203. Sort Items by Groups Respecting Dependencies](https://leetcode.com/problems/sort-items-by-groups-respecting-dependencies/) | Hard | two levels of topological sort: groups, then items |

Related docs: [Graph DFS](Graph_DFS.md), [Graph BFS](Graph_BFS.md), [Union-Find](Union_Find.md).
