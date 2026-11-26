# Hierholzer's Algorithm (Eulerian Path)

An **Eulerian path** uses **every edge exactly once**. Nodes can be visited as many times as needed. If the path starts and ends at the same node, it's an **Eulerian circuit**. **Hierholzer's algorithm** finds one in O(E) time, or O(E log E) when you need the smallest order in dictionary order.

It's a DFS with two twists: you **use up edges** instead of marking nodes visited, and you **record each node in post-order** (when it has no edges left), then reverse.

Compare it with a **Hamiltonian path**, which visits every **node** exactly once. That one sounds similar but is NP-hard, with no fast algorithm known. Eulerian paths are easy.

## Contents

- [When to reach for it](#when-to-reach-for-it)
- [When an Eulerian path exists](#when-an-eulerian-path-exists)
- [Use up edges, not nodes](#use-up-edges-not-nodes)
- [Why greedy alone fails](#why-greedy-alone-fails)
- [The algorithm](#the-algorithm)
- [Trace](#trace)
- [Why it works](#why-it-works)
- [Recursive code (332)](#recursive-code-332)
- [Iterative code](#iterative-code)
- [Finding the start node](#finding-the-start-node)
- [Undirected graphs](#undirected-graphs)
- [Hierholzer vs topological sort](#hierholzer-vs-topological-sort)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## When to reach for it

- The input is a list of **things to chain together, each used exactly once**: tickets, dominoes, pairs `[a, b]` where the next one must start where the last one ended.
- The answer is the **order** to use them in.
- Words in the problem: "use all tickets once", "valid arrangement", "every edge exactly once", "sequence that contains every combination".

If each **node** must be used once instead, it's not this; look at [topological sort](Topological_Sort.md) or backtracking.

## When an Eulerian path exists

For a **directed** graph, count each node's out-degree minus in-degree:

| Node | out − in | Meaning |
|---|---|---|
| start | **+1** | leaves once without arriving |
| end | **−1** | arrives once without leaving |
| every other node | **0** | every arrival is followed by a departure |

- Exactly one `+1` node and one `−1` node: an Eulerian **path** from the `+1` node to the `−1` node.
- Every node `0`: an Eulerian **circuit**. It can start anywhere and ends where it started.
- Plus: all edges must be in one connected piece.

In 332 the problem **guarantees** a valid itinerary exists, so you don't check any of this. You start at `"JFK"`.

## Use up edges, not nodes

| | Most graph DFS | Hierholzer's |
|---|---|---|
| Each node | visited **at most once** | visited **any number of times** |
| Each edge | walked any number of times, or never | used **exactly once** |
| What you track | `visited` / `state` per node | which edges are left |

A `visited` set is **wrong** here. In 332, `JFK → ATL → JFK → SFO` visits JFK twice.

Store each node's outgoing edges in a container you **remove from** as you use them. Then the graph itself is the record of what's left. In 332 that's a `PriorityQueue<String>` per airport: `poll()` returns the smallest destination **and** uses up that ticket.

Duplicate edges (two `JFK → ATL` tickets) are fine. They're two entries in the queue, and each `poll()` uses one.

## Why greedy alone fails

The tempting approach is to always take the smallest unused edge and record nodes as you arrive. It can get stuck **too early**:

```
tickets: JFK→KUL, JFK→NRT, NRT→JFK

JFK → [KUL, NRT]
NRT → [JFK]
```

Greedy: `JFK → KUL`, then stuck. KUL has no tickets out, and 2 tickets are left unused.

Correct: `JFK → NRT → JFK → KUL`.

The key observation: the only place a full trip can get stuck is the **final node** (the `−1` node). So greedy didn't pick the wrong ending; `KUL` **is** the end. It just got there before using the loop `JFK → NRT → JFK`.

Backtracking (undo the flight and try the next one) fixes it, but it can take exponential time. Hierholzer's fixes it **without undoing anything**.

## The algorithm

```
dfs(node):
    while node has unused edges:
        next = take an unused edge from node     // removes it
        dfs(next)
    add node to route                             // post-order

dfs(start)
reverse(route)
```

Two differences from normal DFS:

- **`while`, not `for` or `if`.** The node's edge list shrinks during the recursive calls, so keep taking edges until there are none. A node can branch into more than one loop.
- **Add the node after the loop, not before.** A node is recorded only when it's stuck. That's what moves an early dead end to the **end** of the route.

## Trace

On the example that broke greedy:

```
dfs(JFK)
│  poll → KUL
│  dfs(KUL)
│  │  no edges
│  │  add KUL                     route: [KUL]
│  poll → NRT
│  dfs(NRT)
│  │  poll → JFK
│  │  dfs(JFK)
│  │  │  no edges left
│  │  │  add JFK                  route: [KUL, JFK]
│  │  add NRT                     route: [KUL, JFK, NRT]
│  add JFK                        route: [KUL, JFK, NRT, JFK]

reverse → JFK, NRT, JFK, KUL  ✓
```

1. Greedy went to KUL first and got stuck, the same as before. Instead of failing, it recorded KUL as the **last stop**.
2. Back at JFK, the `while` loop found an unused ticket and explored the loop `JFK → NRT → JFK` that greedy had skipped.
3. The loop's nodes were added **after** KUL, so after reversing they come **before** KUL. The skipped loop is placed where it belongs.

## Why it works

For the reversed route to be valid, each node added must have an edge **to the node added just before it**. Say `Y` was just added, and you return to `P`, the node that went to `Y`.

**Case 1: P has no edges left.** `P` is added next. The edge `P → Y` is the one you just used, so they're connected.

**Case 2: P still has edges.** You leave on a detour, and the next node added is wherever that detour gets stuck. It must be `P` itself:

1. **The first dead end is the end node.** You can only get stuck at a node with more arrivals than departures, and only the `−1` node is like that.
2. **After that first walk, the unused edges are balanced.** The first walk went from start to end. Removing its edges fixes the `+1` at start and the `−1` at end, and removes one in and one out from every node in between. So among unused edges, every node has **in = out**.
3. **A walk on balanced edges can only get stuck where it started.** Leave `P`. Every time you arrive at some node `X ≠ P`, you've used one of its in-edges, and since in = out, it still has an out-edge left. Only `P` has used more outs than ins, so the walk can only get stuck **back at P**.

So every detour is a **closed loop `P → … → P`**. When it ends, `P` is added, and `P` has an edge to `Y`. The same argument repeats inside each loop.

**In short:** the first dead end is the true end. After that, every leftover edge belongs to a loop that returns to where it branched off. Post-order plus reversing inserts each loop at its branch point. You never continue *from* a dead end; you back up to an earlier node that still has edges.

**Smallest order:** at every node you leave on the smallest edge first. The only time the route doesn't follow that order is when the smallest edge leads to a dead end, and then that branch is pushed to the end of the trip, which is where it has to go. Everything else keeps the smallest-first choices.

## Recursive code (332)

[332. Reconstruct Itinerary](../../reconstruct-itinerary/Solution.java):

```java
public List<String> findItinerary(List<List<String>> tickets) {
    Map<String, PriorityQueue<String>> graph = new HashMap<>();
    for (List<String> t : tickets) {
        graph.computeIfAbsent(t.get(0), k -> new PriorityQueue<>()).offer(t.get(1));
    }

    List<String> route = new ArrayList<>();
    dfs("JFK", graph, route);
    Collections.reverse(route);
    return route;
}

private void dfs(String airport, Map<String, PriorityQueue<String>> graph, List<String> route) {
    PriorityQueue<String> next = graph.get(airport);
    while (next != null && !next.isEmpty()) {
        dfs(next.poll(), graph, route);
    }
    route.add(airport);                        // post-order
}
```

- `String`s compare in dictionary order by default, so the `PriorityQueue` needs no comparator.
- A final destination like `"KUL"` may never be a key in the map. Check for `null` before polling.
- Instead of `add` + `Collections.reverse`, you can use a `LinkedList` and `addFirst`. `Collections.reverse` is O(n), so both are O(E) for this step.
- A `PriorityQueue` isn't stored sorted; it only guarantees `poll()` returns the smallest remaining item. Sorting each list once and removing from the end (sort in reverse) also works.

## Iterative code

The recursion can go E calls deep. For 332 (E ≤ 300) that's fine. For big inputs, use an explicit stack with the same post-order logic:

```java
Deque<String> stack = new ArrayDeque<>();
LinkedList<String> route = new LinkedList<>();
stack.push("JFK");

while (!stack.isEmpty()) {
    String top = stack.peek();
    PriorityQueue<String> next = graph.get(top);
    if (next != null && !next.isEmpty()) {
        stack.push(next.poll());               // follow an unused edge
    } else {
        route.addFirst(stack.pop());           // stuck: record it (post-order)
    }
}
return route;
```

The stack holds the current path. Pushing is "fly", popping is "this node is finished". It's the same as the recursive version with the call stack made explicit.

## Finding the start node

332 tells you to start at `"JFK"`. Problems like [2097. Valid Arrangement of Pairs](https://leetcode.com/problems/valid-arrangement-of-pairs/) don't, so you find it from the degree counts:

```java
Map<Integer, Integer> balance = new HashMap<>();       // out − in
for (int[] e : edges) {
    balance.merge(e[0], 1, Integer::sum);
    balance.merge(e[1], -1, Integer::sum);
}

int start = edges[0][0];                                // circuit: any node with an edge works
for (var entry : balance.entrySet()) {
    if (entry.getValue() == 1) { start = entry.getKey(); break; }   // path: must start at the +1 node
}
```

If you start anywhere other than the `+1` node on a path, the algorithm can't produce a valid route.

## Undirected graphs

The idea is the same, with two changes:

- **Existence:** an Eulerian path exists if **0 or 2 nodes have an odd degree**, and all edges are connected. With 0, it's a circuit. With 2, start at one of the odd ones.
- **Using an edge removes it from both ends.** An edge `u – v` is in both `u`'s and `v`'s lists. Give each edge an id and keep a `boolean[] used`. When you take an edge from `u`'s list, mark its id used, and skip used ids when you look at `v`'s list later.

## Hierholzer vs topological sort

Both are DFS with post-order and a reverse at the end. What they use up is different:

| | [Topological sort](Topological_Sort.md) (DFS) | Hierholzer's |
|---|---|---|
| Uses up | each **node** once (three states) | each **edge** once (remove on use) |
| Records a node | when its DFS call finishes | when it has no edges left |
| Loop over neighbors | `for` over all of them | `while` edges remain |
| Final step | reverse | reverse (or `addFirst`) |
| Cycles | mean there's no answer | are expected; they're the detours |

## Cost summary

| Problem | Approach | Time | Space |
|---|---|---|---|
| 332. Reconstruct Itinerary | recursive Hierholzer's, `PriorityQueue` per airport | O(E log E) | O(E) |
| Any Eulerian path, order doesn't matter | Hierholzer's with plain lists | O(V + E) | O(V + E) |

E = number of edges (tickets), V = number of nodes (airports).

- **Why log E and not log V in 332:** duplicate tickets are allowed, so one airport's queue can hold almost all E tickets. Without duplicates, a queue holds at most V − 1, and O(E log V) is also correct. Without duplicates, E ≤ V², so log E ≤ 2 log V; they're the same up to a constant.
- **Space:** map entries (E), route (E + 1), recursion stack (up to E). V ≤ E + 1 because every airport appears on some ticket, so O(E + V) simplifies to O(E).
- Comparing airport codes costs time too, but every code is 3 letters, so that's constant.

## Pitfalls checklist

- [ ] **No `visited` set.** Nodes can repeat. Use up edges instead.
- [ ] **`while`, not `if`**, around taking the next edge. A node can branch into several loops.
- [ ] **Add the node after the loop**, not before. Adding before is just greedy again, and the `KUL` example fails.
- [ ] **Reverse** the route at the end, or use `addFirst`.
- [ ] **Null check**: nodes with no outgoing edges may not be keys in the map.
- [ ] Need the smallest order? Take edges **smallest first** (`PriorityQueue`, or sort once).
- [ ] Duplicate edges are **separate** edges. Keep both entries.
- [ ] Not given a start node? Start at the node with **out − in = +1**, or any node with an edge if all are balanced.
- [ ] Undirected: mark each edge used **once by id**, so it's removed from both ends.
- [ ] Huge inputs: switch to the **iterative** stack version to avoid stack overflow.

## Problems for further practice

Solved in this repo: [332. Reconstruct Itinerary](../../reconstruct-itinerary/notes.md).

| Problem | Difficulty | What it adds |
|---|---|---|
| [2097. Valid Arrangement of Pairs](https://leetcode.com/problems/valid-arrangement-of-pairs/) | Hard | no given start: find the +1 node from degree counts; output edges, not nodes |
| [753. Cracking the Safe](https://leetcode.com/problems/cracking-the-safe/) | Hard | build the graph yourself (de Bruijn sequence): nodes are (n−1)-digit strings, edges are digits |

Related docs: [Graph DFS](Graph_DFS.md), [Topological Sort](Topological_Sort.md), [Heap & PriorityQueue](Heap_PriorityQueue.md).
