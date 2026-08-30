# Heap & PriorityQueue

A **heap** is a data structure that lets you ask "what's the smallest (or largest) thing in this collection?" in O(1), and add/remove elements in O(log N). Java exposes it as `PriorityQueue<E>`.

## Contents

- [What a heap is](#what-a-heap-is)
- [Why it's fast — sift up / sift down](#why-its-fast--sift-up--sift-down)
- [Java's PriorityQueue](#javas-priorityqueue)
  - [Min-heap (default)](#min-heap-default)
  - [Max-heap](#max-heap)
  - [Custom comparator](#custom-comparator)
- [Concrete example — trace](#concrete-example--trace)
- [Cost summary](#cost-summary)

## What a heap is

A heap is a **complete binary tree** (every level full except possibly the last, which is filled left-to-right) with the **heap property**:

- **Min-heap:** every parent ≤ its children. The smallest value is always at the root.
- **Max-heap:** every parent ≥ its children. The largest value is always at the root.

Note: only the parent/child relationship is constrained. Siblings can be in any order, and the heap is **not** sorted overall.

Because the tree is complete, it's stored as a plain array in **level order** (root at index 0, then left-to-right per level). For node at index `i`:

```
parent      = (i - 1) / 2
leftChild   = 2*i + 1
rightChild  = 2*i + 2
```

So a heap of N elements lives in an array of length N — no pointers, no tree nodes.

Example — min-heap holding `[1, 3, 2, 7, 5, 4]`:

```
index:  0   1   2   3   4   5
value:  1   3   2   7   5   4

tree:        1
           /   \
          3     2
         / \   /
        7   5 4
```

Check: 1 ≤ 3, 1 ≤ 2 (root vs children); 3 ≤ 7, 3 ≤ 5; 2 ≤ 4. ✅

## Why it's fast — sift up / sift down

Adding or removing the root would break the heap property. We restore it by swapping along **one root-to-leaf path**, which has length ⌊log₂ N⌋ — that's where O(log N) comes from.

**Insert (push):** put the new value at the end of the array, then **sift up**: while it's smaller than its parent (for a min-heap), swap with parent. Stops at the root or when the parent is already smaller.

**Extract-min (poll):** save the root (the answer), move the **last element** into the root slot, then **sift down**: while it's larger than its smaller child, swap with that child. Stops at a leaf or when both children are larger.

Each sift touches at most one node per level → O(log N) swaps.

`peek` just reads index 0 → O(1).

## Java's PriorityQueue

`java.util.PriorityQueue<E>` is a binary heap, **min-heap by default**, backed by an array that auto-grows.

### Min-heap (default)

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(5);            // insert
pq.offer(3);
pq.offer(7);
int min = pq.peek();    // 3, no removal
int v   = pq.poll();    // 3, removes
boolean empty = pq.isEmpty();
int size = pq.size();
```

| Op       | Throws       | Returns sentinel |
|----------|--------------|------------------|
| Insert   | `add(e)`     | `offer(e)`       |
| Remove   | `remove()`   | `poll()`         |
| Peek     | `element()`  | `peek()`         |

Prefer the sentinel-returning column in CP — null is easier to check than catching exceptions. (`offer` and `add` are effectively identical for `PriorityQueue` since it grows on demand.)

### Max-heap

Pass a reverse-order comparator:

```java
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
maxHeap.offer(5);
maxHeap.offer(3);
maxHeap.offer(7);
maxHeap.peek();   // 7
```

### Custom comparator

For arbitrary "priority," supply a `Comparator<E>` — the element that compares as **smallest** sits at the root.

```java
// Heap of int[] pairs (count, value), ordered by count ascending
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);

// Heap of strings, longest first
PriorityQueue<String> pq2 = new PriorityQueue<>((a, b) -> b.length() - a.length());
```

For `int` comparisons, prefer `Integer.compare(a, b)` over `a - b` to avoid overflow on large/negative inputs.

## Concrete example — trace

Min-heap. Operations: `offer 5, offer 3, offer 8, offer 1, poll, poll`.

| Op       | Array (level order) | Tree view | Returned |
|----------|---------------------|-----------|----------|
| offer 5  | `[5]`               | `5`                                        | — |
| offer 3  | `[3, 5]`            | `3` over `5`. Inserted 3 at idx 1, sifted up past 5. | — |
| offer 8  | `[3, 5, 8]`         | `3` over `5, 8`. 8 inserted at idx 2; parent `3 ≤ 8`, no swap. | — |
| offer 1  | `[1, 3, 8, 5]`      | `1` over `3, 8`; `5` under `3`. 1 inserted at idx 3, sifted up: swap with parent 5 → swap with parent 3. | — |
| poll     | `[3, 5, 8]`         | Returned 1. Moved last (5) to root, sifted down: swap with smaller child 3. | **1** |
| poll     | `[5, 8]`            | Returned 3. Moved last (8) to root, sifted down: swap with smaller child 5. | **3** |

Note how the array is **not sorted** — `[1, 3, 8, 5]` violates sorted order, but the heap property (parent ≤ children) holds throughout.

## Cost summary

| Operation              | Time     |
|------------------------|----------|
| `peek()`               | O(1)     |
| `offer(e)` / `add(e)`  | O(log N) |
| `poll()` / `remove()`  | O(log N) |
| `remove(Object o)`     | O(N)     ← linear scan to find `o`, then sift |
| `contains(Object o)`   | O(N)     |
| Build heap from N items via N `offer` calls | O(N log N) |
| Build heap from a `Collection` via the constructor `new PriorityQueue<>(coll)` | O(N) — heapify in place |

Space: O(N).
