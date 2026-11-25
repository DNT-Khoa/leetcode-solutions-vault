# Dijkstra: Lazy vs Eager

**Dijkstra's algorithm** finds the shortest distance from one source to every other node in a graph with **non-negative edge weights**. It repeatedly takes the closest node that isn't finished yet, locks in its distance, and uses it to improve its neighbors' distances.

There are two common ways to manage the priority queue. They differ in what happens when a node's distance improves while it's **already in the queue**:

- **Lazy**: push a new entry and leave the old one. Skip the outdated ("stale") entry when it's popped later.
- **Eager**: find the existing entry and **lower its priority in place** (decrease-key). Each node is in the queue at most once.

**Short version:** eager is *not* always better. Both have the same time complexity with a binary heap, and in Java the lazy version is usually as fast or faster, because Java's `PriorityQueue` has no decrease-key. Eager uses less memory. That's its real advantage.

## Contents

- [The core idea](#the-core-idea)
- [Lazy Dijkstra](#lazy-dijkstra)
- [Two lazy styles: skip on pop vs check on push](#two-lazy-styles-skip-on-pop-vs-check-on-push)
- [Eager Dijkstra](#eager-dijkstra)
- [The Java trap: `PriorityQueue.remove`](#the-java-trap-priorityqueueremove)
- [Is eager always better?](#is-eager-always-better)
- [Dense graphs: the O(V²) array version](#dense-graphs-the-ov²-array-version)
- [Which one to use](#which-one-to-use)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## The core idea

Keep `dist[v]`, the best distance to `v` found so far. Start with `dist[source] = 0` and everything else at infinity.

1. Take the unfinished node `u` with the smallest `dist`.
2. Mark it **finished**. Its distance is now final.
3. **Relax** each edge u → v with weight w: if `dist[u] + w < dist[v]`, update `dist[v]`.

**Why step 2 is safe:** all weights are ≥ 0, so any other route to `u` has to pass through a node that's at least as far away as `u`, and then add more non-negative weight. It can't come out shorter. That's also why Dijkstra **breaks with negative weights**: a later edge could make a "finished" distance smaller. For negative weights, use Bellman-Ford.

A heap gives you step 1 quickly. The lazy/eager question is about how the heap handles step 3.

## Lazy Dijkstra

Push `(distance, node)` every time a distance improves. A node can end up in the heap several times with different distances. Only the smallest one matters; the rest are stale.

```java
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;

PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
pq.offer(new int[]{0, src});

while (!pq.isEmpty()) {
    int[] cur = pq.poll();
    int d = cur[0], u = cur[1];
    if (d > dist[u]) continue;              // stale entry: a shorter one was already processed

    for (int[] e : adj.get(u)) {
        int v = e[0], w = e[1];
        int nd = d + w;
        if (nd < dist[v]) {                 // only push real improvements
            dist[v] = nd;
            pq.offer(new int[]{nd, v});
        }
    }
}
```

- **Heap size**: up to O(E) entries, because every successful relaxation adds one.
- **Time**: O(E log E). Since E ≤ V², log E ≤ 2 log V, so this is the same as **O(E log V)**.
- **What it relies on**: only the built-in `PriorityQueue`.

Stale entries cost one extra `poll` each, O(log E), and a comparison. That's the whole price of being lazy.

## Two lazy styles: skip on pop vs check on push

Your [743. Network Delay Time](../../network-delay-time/Solution.java) uses a slightly different lazy style, where a node is finished the **first time it's popped**:

```java
if (shortestPaths.containsKey(node)) continue;   // already finished
shortestPaths.put(node, d);                       // first pop = shortest
...
if (shortestPaths.containsKey(next)) continue;    // don't push to finished nodes
pq.offer(new int[]{d + w, next});
```

| | Finish on first pop (your 743) | `dist[]` + check on push (template above) |
|---|---|---|
| When it pushes | any neighbor that isn't finished | only when the distance **strictly improves** |
| How it skips stale entries | "already finished?" | `d > dist[u]` |
| Heap size | up to O(E) | up to O(E), usually fewer entries in practice |
| Time | O(E log E) | O(E log E) |

Both are correct and both count as lazy. The `dist[]` version pushes fewer entries, because it doesn't push a distance that's worse than one already known. When nodes are numbered `0..n-1` or `1..n`, an `int[]` is also faster than a `HashMap<Integer, Integer>`.

## Eager Dijkstra

Keep at most **one entry per node**. When `dist[v]` improves and `v` is already in the heap, move its entry up (decrease-key) instead of adding a second one.

For that, the heap has to know **where each node currently sits**, so it can find the entry and move it. That's an **indexed priority queue**. Java doesn't provide one, so you write it yourself:

```java
class IndexedMinPQ {
    private final int[] heap;   // heap[i]   = node at heap position i
    private final int[] pos;    // pos[node] = its position in heap, or -1 if not in the heap
    private final int[] key;    // key[node] = its current priority
    private int size;

    IndexedMinPQ(int n) {
        heap = new int[n]; pos = new int[n]; key = new int[n];
        Arrays.fill(pos, -1);
    }

    boolean isEmpty()       { return size == 0; }
    boolean contains(int v) { return pos[v] != -1; }

    void insert(int v, int k) {
        key[v] = k; heap[size] = v; pos[v] = size;
        siftUp(size++);
    }

    void decreaseKey(int v, int k) {
        key[v] = k;
        siftUp(pos[v]);
    }

    int pollMin() {
        int v = heap[0];
        swap(0, --size);
        pos[v] = -1;
        siftDown(0);
        return v;
    }

    private void siftUp(int i) {
        while (i > 0 && key[heap[i]] < key[heap[(i - 1) / 2]]) {
            swap(i, (i - 1) / 2);
            i = (i - 1) / 2;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = 2 * i + 1;
            if (l >= size) return;
            int c = (l + 1 < size && key[heap[l + 1]] < key[heap[l]]) ? l + 1 : l;
            if (key[heap[c]] >= key[heap[i]]) return;
            swap(i, c);
            i = c;
        }
    }

    private void swap(int i, int j) {
        int t = heap[i]; heap[i] = heap[j]; heap[j] = t;
        pos[heap[i]] = i;
        pos[heap[j]] = j;
    }
}
```

The Dijkstra loop around it:

```java
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;

IndexedMinPQ pq = new IndexedMinPQ(n);
pq.insert(src, 0);

while (!pq.isEmpty()) {
    int u = pq.pollMin();                    // never stale: one entry per node
    for (int[] e : adj.get(u)) {
        int v = e[0], w = e[1];
        int nd = dist[u] + w;
        if (nd < dist[v]) {
            dist[v] = nd;
            if (pq.contains(v)) pq.decreaseKey(v, nd);
            else                pq.insert(v, nd);
        }
    }
}
```

With non-negative weights, a finished node can never improve, so `insert` is never called on a node that was already popped.

- **Heap size**: O(V), at most one entry per node.
- **Time**: O(E log V). Each decrease-key and each poll is O(log V).
- **What it relies on**: about 50 lines of your own code.

### Eager with a `TreeSet` (built in, but awkward)

A `TreeSet` can remove any element in O(log n), so it can act as an eager queue without writing your own heap:

```java
TreeSet<int[]> set = new TreeSet<>((a, b) ->
        a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
set.add(new int[]{0, src});

while (!set.isEmpty()) {
    int u = set.pollFirst()[1];
    for (int[] e : adj.get(u)) {
        int v = e[0], nd = dist[u] + e[1];
        if (nd < dist[v]) {
            if (dist[v] != Integer.MAX_VALUE) set.remove(new int[]{dist[v], v});   // drop the old entry
            dist[v] = nd;
            set.add(new int[]{nd, v});
        }
    }
}
```

- The **tie-break on the node id is required**. Without it, two nodes at the same distance compare as equal, and the `TreeSet` silently drops one of them.
- `remove(new int[]{...})` works because a `TreeSet` finds elements with the comparator, not with `equals`.
- It's the same O(E log V) as the indexed heap, but a red-black tree with a node object per entry has a bigger constant factor than an array heap. In practice it's often **slower** than lazy `PriorityQueue`.

## The Java trap: `PriorityQueue.remove`

It's tempting to write "eager" Dijkstra like this:

```java
pq.remove(oldEntry);              // O(V): linear scan of the heap array
pq.offer(new int[]{nd, v});
```

`PriorityQueue.remove(Object)` **searches the whole heap**, which costs O(V) per call. That makes the whole algorithm **O(E · V)**, far worse than lazy. It also only finds the entry if you kept the exact same array object, since `int[]` uses reference equality.

Java's `PriorityQueue` has no decrease-key. Your choices are lazy, `TreeSet`, or your own indexed heap.

## Is eager always better?

No. Here's what each version really costs:

| | Lazy (binary heap) | Eager (indexed binary heap) |
|---|---|---|
| Time | O(E log E) = O(E log V) | O(E log V) |
| Heap size | O(E) | O(V) |
| Stale pops | up to E - V wasted polls | none |
| Code | built-in `PriorityQueue` | ~50 lines of your own |
| Constant factor | low: array heap, but allocates an `int[]` per push | low, but extra `pos[]` updates on every swap |

**Where eager wins:**
- **Memory** on graphs with many edges per node: the heap stays at V entries instead of up to E.
- **Many improvements per node**: every improvement is a cheap in-place move, not a new entry plus a wasted pop later.
- **Library code** that gets reused, where writing the indexed heap once pays off.

**Where lazy wins (most LeetCode problems):**
- Same Big-O, much less code, fewer places for bugs.
- On typical sparse inputs, stale entries are few, and benchmarks often show lazy binary heaps running as fast or faster.
- It's what interviewers expect to see. Writing a correct indexed heap under time pressure is risky.

**The theoretical best**, O(E + V log V), needs a **Fibonacci heap**. Its constants are so large that it's slower in practice, and nobody writes one in an interview.

## Dense graphs: the O(V²) array version

When the graph is **dense** (E close to V², such as a complete graph where every node connects to every other), drop the heap entirely:

```java
boolean[] done = new boolean[n];
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;

for (int iter = 0; iter < n; iter++) {
    int u = -1;
    for (int v = 0; v < n; v++) {                       // O(V) scan for the closest unfinished node
        if (!done[v] && (u == -1 || dist[v] < dist[u])) u = v;
    }
    if (dist[u] == Integer.MAX_VALUE) break;            // the rest are unreachable
    done[u] = true;
    for (int[] e : adj.get(u)) {
        int v = e[0], w = e[1];
        if (!done[v] && dist[u] + w < dist[v]) dist[v] = dist[u] + w;
    }
}
```

That's O(V²) for the scans plus O(E) for the relaxations, so **O(V² + E) = O(V²)** when E is about V². A heap version on the same graph costs O(V² log V). So on dense graphs, the **simplest version is the fastest**.

The same shape appears in **Prim's MST** on complete graphs, which is relevant to 1584. Min Cost to Connect All Points.

## Which one to use

| Situation | Use |
|---|---|
| Typical LeetCode / interview, sparse graph | **Lazy** with `dist[]` and the `d > dist[u]` skip |
| Dense or complete graph (E ≈ V²) | **O(V²) array** version |
| Memory-tight, many edges per node, reusable library | **Eager** with an indexed heap |
| You want eager without writing a heap | `TreeSet` with a node-id tie-break (usually not worth it) |
| Negative edge weights | **None of these**: use Bellman-Ford |

## Cost summary

| Version | Time | Extra space |
|---|---|---|
| Lazy, binary heap | O(E log V) | O(V + E): `dist` + heap of up to E entries |
| Eager, indexed binary heap | O(E log V) | O(V): `dist`, `heap`, `pos`, `key` |
| Eager, `TreeSet` | O(E log V), larger constant | O(V) |
| "Eager" with `PriorityQueue.remove` | **O(E · V)** | O(V) |
| Array, no heap | O(V² + E) | O(V) |
| Fibonacci heap (theory only) | O(E + V log V) | O(V) |

All of these need the adjacency list too, which is O(V + E).

## Pitfalls checklist

- [ ] All edge weights are **≥ 0**. If not, Dijkstra is wrong; use Bellman-Ford.
- [ ] Lazy: skip stale entries with `d > dist[u]` (or "already finished"). Without the skip, the answer is still right, but stale entries re-relax their edges and waste work.
- [ ] Lazy: only push when `nd < dist[v]` **strictly**.
- [ ] Never use `PriorityQueue.remove(Object)` as decrease-key: it's O(V).
- [ ] `TreeSet` eager: the comparator **must** tie-break on node id.
- [ ] Use `Integer.compare(a[0], b[0])`, not `a[0] - b[0]`, which can overflow with large distances.
- [ ] `Integer.MAX_VALUE` as infinity: never compute `dist[u] + w` while `dist[u]` is still infinity. With large weights or long paths, use `long`.
- [ ] Watch node labels: 1-indexed problems (like 743) need arrays of size n + 1.
- [ ] Dense or complete graph: consider the O(V²) array version before reaching for a heap.

## Problems for further practice

Solved in this repo: [743. Network Delay Time](../../network-delay-time/notes.md) (lazy, finish on first pop).

| Problem | Difficulty | What it adds |
|---|---|---|
| [1514. Path with Maximum Probability](https://leetcode.com/problems/path-with-maximum-probability/) | Medium | multiply instead of add; max-heap instead of min-heap |
| [1631. Path With Minimum Effort](https://leetcode.com/problems/path-with-minimum-effort/) | Medium | the path cost is the **max** edge, not the sum: Dijkstra on a grid |
| [1584. Min Cost to Connect All Points](https://leetcode.com/problems/min-cost-to-connect-all-points/) | Medium | Prim's MST, Dijkstra-shaped; complete graph, so try the O(V²) version |
| [778. Swim in Rising Water](https://leetcode.com/problems/swim-in-rising-water/) | Hard | minimax path cost on a grid |
| [787. Cheapest Flights Within K Stops](https://leetcode.com/problems/cheapest-flights-within-k-stops/) | Medium | a stop limit breaks plain Dijkstra; learn Bellman-Ford |
| [1976. Number of Ways to Arrive at Destination](https://leetcode.com/problems/number-of-ways-to-arrive-at-destination/) | Medium | count the shortest paths while relaxing; needs `long` |
| [882. Reachable Nodes In Subdivided Graph](https://leetcode.com/problems/reachable-nodes-in-subdivided-graph/) | Hard | Dijkstra, then count partial edges |

Related docs: [Heap & PriorityQueue](Heap_PriorityQueue.md), [Graph BFS](Graph_BFS.md) (unweighted shortest path, 0-1 BFS), [Union-Find](Union_Find.md).
