# Intervals

Interval problems give you ranges `[start, end]` and ask you to merge them, insert into them, remove some of them, or answer questions about which ones cover a point. Almost every one of them starts the same way: **sort**, then make **one pass** where each interval only has to be compared with the one before it (or with a small heap of "still open" intervals).

## Contents

- [When to reach for it](#when-to-reach-for-it)
- [The overlap test (and the touching-endpoints trap)](#the-overlap-test-and-the-touching-endpoints-trap)
- [Sort by start or by end?](#sort-by-start-or-by-end)
- [Pattern 1 — Insert into a sorted list (three phases)](#pattern-1--insert-into-a-sorted-list-three-phases)
- [Pattern 2 — Merge overlapping intervals](#pattern-2--merge-overlapping-intervals)
- [Pattern 3 — Greedy: keep the one that ends first](#pattern-3--greedy-keep-the-one-that-ends-first)
- [Pattern 4 — Sweep with a min-heap (offline queries)](#pattern-4--sweep-with-a-min-heap-offline-queries)
- [Pattern 5 — How many overlap at once? (heap of ends / events)](#pattern-5--how-many-overlap-at-once-heap-of-ends--events)
- [Pattern 6 — Two sorted lists (two pointers)](#pattern-6--two-sorted-lists-two-pointers)
- [Java toolbox](#java-toolbox)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## When to reach for it

The input is a list of pairs that represent ranges — times, meetings, balloons, bookings — and the question is about how they **overlap**:

- combine overlapping ranges → merge
- minimum removals / maximum that fit without overlap → greedy by end
- how many are open at the same time (rooms, groups, capacity) → heap of ends or events
- for each point, which ranges contain it → sweep with a heap

If the intervals aren't already sorted, the first line of the solution is almost always a sort.

## The overlap test (and the touching-endpoints trap)

Two intervals `a` and `b` overlap when **each one starts before the other ends**:

```
a[0] <= b[1] && b[0] <= a[1]
```

After sorting by start (so `a[0] <= b[0]`), only half of that is needed: **`b[0] <= a[1]`** — the next interval starts before the previous one ends.

The trap is the **`<=` vs `<`**: whether intervals that only touch (`[1,2]` and `[2,3]`) count as overlapping **depends on the problem**. Read the statement and the examples every time.

| Problem | `[1,2]` and `[2,3]`… | Condition after sorting by start |
|---|---|---|
| [56. Merge Intervals](../../merge-intervals/notes.md) | overlap → merge into `[1,3]` | `next[0] <= last[1]` |
| [57. Insert Interval](../../insert-interval/notes.md) | overlap → merge | `intervals[i][0] <= newInterval[1]` |
| [435. Non-overlapping Intervals](../../non-overlapping-intervals/notes.md) | **don't** overlap → keep both | `next[0] < prevEnd` |
| [1851. Minimum Interval to Include Each Query](../../minimum-interval-to-include-each-query/notes.md) | — (point `q` is inside `[l, r]` when `l <= q <= r`, both ends included) | pop when `end < q` |

## Sort by start or by end?

| Sort by | Use when | Why |
|---|---|---|
| **start** | merging, inserting, sweeping points left to right | after sorting, anything that overlaps the current interval comes right after it — you never look backwards |
| **end** | "keep as many as possible" / "minimum removals" / "minimum arrows" | the interval that ends first leaves the most room for the rest — that's the greedy choice |

Sort-by-start also works for the greedy problems (that's how your 435 does it), but then you need `Math.min` on the end when two overlap. Sorting by end removes that step.

## Pattern 1 — Insert into a sorted list (three phases)

From [57. Insert Interval](../../insert-interval/notes.md). The input is already sorted and non-overlapping, so one pass in three phases does it — no sort needed:

```java
// 1. everything that ends before the new one starts: copy as is
while (i < n && intervals[i][1] < newInterval[0]) result.add(intervals[i++]);

// 2. everything that overlaps: grow the new interval
int start = newInterval[0], end = newInterval[1];
while (i < n && intervals[i][0] <= end) {
    start = Math.min(start, intervals[i][0]);
    end   = Math.max(end, intervals[i][1]);
    i++;
}
result.add(new int[]{start, end});

// 3. everything after: copy as is
while (i < n) result.add(intervals[i++]);
```

Phase 2's condition compares against `end`, the *growing* boundary, not the original `newInterval[1]`.

Each phase's loop must move `i` forward — forgetting `i++` in phase 3 is an infinite loop (a bug this repo's solution once had).

Cost: O(N) time, O(N) for the output.

## Pattern 2 — Merge overlapping intervals

From [56. Merge Intervals](../../merge-intervals/notes.md). Sort by start, then the interval you might merge into is always **the last one in the result**:

```java
Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
List<int[]> result = new ArrayList<>();
for (int[] interval : intervals) {
    if (!result.isEmpty() && interval[0] <= result.getLast()[1]) {
        int[] last = result.getLast();
        last[1] = Math.max(last[1], interval[1]);   // max, never just interval[1]
    } else {
        result.add(interval);
    }
}
return result.toArray(new int[0][]);
```

- Changing `last[1]` updates the list too: the list holds a **reference** to that same `int[]`.
- **`Math.max` is essential.** `[1,10]` followed by `[2,3]` must stay `[1,10]`; assigning `interval[1]` shrinks it to `[1,3]` and breaks every merge after it.
- The start never needs updating — sorting already put the smallest start first.

Cost: O(N log N) time for the sort, O(N) space.

## Pattern 3 — Greedy: keep the one that ends first

From [435. Non-overlapping Intervals](../../non-overlapping-intervals/notes.md): minimum number of intervals to remove so the rest don't overlap.

**Greedy rule:** when two intervals overlap, keep the one that **ends earlier** — it leaves the most room for everything after it. Only the previous kept interval's **end** matters:

```
sort by start
prevEnd = first interval's end
for each next interval:
    overlaps (start < prevEnd)  → remove one: count++, prevEnd = min(prevEnd, end)
    no overlap                  → keep it:    prevEnd = end
```

Sort-by-end variant: count how many you can **keep** (take an interval whenever it starts at or after the last kept end), and answer `n - kept`. No `min` needed.

Same shape:
- **Minimum Number of Arrows to Burst Balloons** — one arrow per group of overlapping balloons; touching *does* count as overlap there.
- **Maximum number of non-overlapping meetings** — the classic activity-selection problem.

Cost: O(N log N) time for the sort, O(1) extra besides it (Java's object sort itself can use O(N)).

## Pattern 4 — Sweep with a min-heap (offline queries)

From [1851. Minimum Interval to Include Each Query](../../minimum-interval-to-include-each-query/notes.md): for each query point, the size of the smallest interval containing it.

"Offline" means you can answer the queries in **any order you like** and put the answers back afterwards. Answer them **from smallest to largest**, moving a vertical line left to right:

```
sort intervals by start
sort queries (remember each one's original position)
min-heap of (size, end), smallest size on top
for each query q, smallest first:
    A. push every interval with start <= q        (pointer only moves forward)
    B. pop the top while its end < q              (it ended — and stays ended for later queries)
    C. answer = top's size, or -1 if heap is empty   ← peek, don't poll
```

Why it works:
- Queries only increase, so "has started" and "has ended" never get undone. Each interval is pushed once and popped at most once.
- **Lazy deletion:** only the top is cleaned. Dead intervals deeper down don't matter until they reach the top, where step B catches them. Same trick as the cooldown queue in [Greedy Scheduling with Cooldown](Greedy_Scheduling_Cooldown.md).
- **Step C must `peek`, not `poll`.** The smallest interval for this query may also be the answer for the next ones — removing it was this repo's bug before the fix.

Putting answers back in order — two options:
- Sort an `Integer[]` of **indices** by query value, and write `answer[idx]` (see [Java toolbox](#java-toolbox)).
- Or keep a `Map<queryValue, answer>` and fill the result from a copy of the original queries (what the solution here does). Duplicate query values are fine — same value, same answer.

Cost: O(N log N + Q log Q) time, O(N + Q) space.

## Pattern 5 — How many overlap at once? (heap of ends / events)

Questions like "minimum number of meeting rooms", "minimum number of groups", or "is the car ever over capacity" ask for the **maximum number of intervals open at the same moment**. Two standard ways to think about it:

- **Heap of end times:** sort by start; keep a min-heap of the end times of rooms in use. For each meeting, if the earliest-ending room has freed up, reuse it (pop); then push this meeting's end. The heap's largest size is the answer.
- **Events / line sweep:** turn each interval into two events, `+1` at the start and `-1` at the end. Sort the events and keep a running total — its peak is the answer. When a start and an end happen at the same time, the order you process them in encodes the touching-endpoints rule.
- **Difference array:** if the coordinates are small (like Car Pooling's `0..1000`), add at `start`, subtract at `end`, and take a prefix sum — no sort needed.

## Pattern 6 — Two sorted lists (two pointers)

When you get **two** lists that are each sorted and non-overlapping (e.g. two people's free times), walk both with one pointer each. The overlap of the current pair is `[max(starts), min(ends)]` — valid if that start ≤ end. Then advance the pointer whose interval **ends first**, because it can't overlap anything further in the other list.

## Java toolbox

```java
// sort by start (no overflow, unlike a[0] - b[0])
Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));

// sort by end, then by start
Arrays.sort(intervals, Comparator.<int[]>comparingInt(a -> a[1]).thenComparingInt(a -> a[0]));

// List<int[]> → int[][]
int[][] out = result.toArray(new int[0][]);

// last element of a list (Java 21+), or the classic form
int[] last = result.getLast();
int[] last2 = result.get(result.size() - 1);

// sort query indices by value, keeping original positions
Integer[] order = new Integer[queries.length];
for (int i = 0; i < order.length; i++) order[i] = i;
Arrays.sort(order, Comparator.comparingInt(i -> queries[i]));

// min-heap of int[] ordered by index 0
PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
```

**Why not `(a, b) -> a[0] - b[0]`?** Subtraction overflows when values are near `Integer.MIN_VALUE` / `MAX_VALUE`. Minimum Number of Arrows to Burst Balloons has coordinates across the full `int` range, and a subtracting comparator sorts it wrongly. `comparingInt` / `Integer.compare` never overflow.

**Changing input arrays.** Adding an input `int[]` to the result and then changing it (as in Pattern 2) also changes the input. LeetCode doesn't care; if you need the input untouched, add `interval.clone()` or `new int[]{a, b}` instead.

## Cost summary

`N` = number of intervals, `Q` = number of queries.

| Pattern | Time | Extra space |
|---|---|---|
| Insert into sorted list | O(N) | O(N) output |
| Merge | O(N log N) | O(N) output + sort |
| Greedy removal / keep max | O(N log N) | O(1) besides the sort |
| Sweep with heap (queries) | O(N log N + Q log Q) | O(N + Q) |
| Max overlap (heap / events) | O(N log N) | O(N) |
| Two sorted lists | O(N + M) | O(N + M) output |

The sort usually dominates. Java's `Arrays.sort` on an object array (`int[][]`, `Integer[]`) is TimSort, which can use O(N) extra memory — say so in the Space comment, or write "O(1) extra, excluding the sort".

## Pitfalls checklist

- [ ] Do touching intervals (`[1,2]`, `[2,3]`) overlap **in this problem**? Pick `<=` or `<` from the statement and examples.
- [ ] Sorted by the right key — start for merge/insert/sweep, end (or start + `min`) for greedy removal?
- [ ] Merging: the end is `Math.max(oldEnd, newEnd)`, never just `newEnd`.
- [ ] Every `while` loop moves its pointer forward.
- [ ] Heap answers: `peek` when the element might still be needed, `poll` only to discard.
- [ ] Offline queries: answers go back in the **original** order.
- [ ] Interval size with both ends included is `end - start + 1`.
- [ ] Comparator uses `comparingInt` / `Integer.compare`, not subtraction.

## Problems for further practice

Solved in this repo:

- [57. Insert Interval](../../insert-interval/notes.md) — Pattern 1.
- [56. Merge Intervals](../../merge-intervals/notes.md) — Pattern 2.
- [435. Non-overlapping Intervals](../../non-overlapping-intervals/notes.md) — Pattern 3.
- [1851. Minimum Interval to Include Each Query](../../minimum-interval-to-include-each-query/notes.md) — Pattern 4.

Next, roughly in order of difficulty:

| Problem | Difficulty | Pattern |
|---|---|---|
| [228. Summary Ranges](https://leetcode.com/problems/summary-ranges/) | Easy | build intervals from a sorted array (the reverse direction) |
| [252. Meeting Rooms](https://leetcode.com/problems/meeting-rooms/) | Easy (Premium) | sort by start; any overlap between neighbours? |
| [452. Minimum Number of Arrows to Burst Balloons](https://leetcode.com/problems/minimum-number-of-arrows-to-burst-balloons/) | Medium | Pattern 3 — and the comparator overflow trap |
| [1288. Remove Covered Intervals](https://leetcode.com/problems/remove-covered-intervals/) | Medium | sort by start asc, end desc; track the furthest end |
| [986. Interval List Intersections](https://leetcode.com/problems/interval-list-intersections/) | Medium | Pattern 6 — two pointers |
| [1094. Car Pooling](https://leetcode.com/problems/car-pooling/) | Medium | Pattern 5 — difference array or events |
| [2406. Divide Intervals Into Minimum Number of Groups](https://leetcode.com/problems/divide-intervals-into-minimum-number-of-groups/) | Medium | Pattern 5 — heap of ends (free version of Meeting Rooms II) |
| [253. Meeting Rooms II](https://leetcode.com/problems/meeting-rooms-ii/) | Medium (Premium) | Pattern 5 |
| [729. My Calendar I](https://leetcode.com/problems/my-calendar-i/) | Medium | overlap test against a `TreeMap` of bookings |
| [731. My Calendar II](https://leetcode.com/problems/my-calendar-ii/) | Medium | Pattern 5 — events in a `TreeMap` |
| [1834. Single-Threaded CPU](https://leetcode.com/problems/single-threaded-cpu/) | Medium | sort by arrival + min-heap, like Pattern 4 |
| [2402. Meeting Rooms III](https://leetcode.com/problems/meeting-rooms-iii/) | Hard | two heaps: free rooms and busy rooms by end time |
| [759. Employee Free Time](https://leetcode.com/problems/employee-free-time/) | Hard (Premium) | merge everyone's intervals, then read the gaps |
| [715. Range Module](https://leetcode.com/problems/range-module/) | Hard | keep merged intervals in a `TreeMap`; add / remove / query ranges |

Related docs: [Heap & PriorityQueue](Heap_PriorityQueue.md), [Greedy Scheduling with Cooldown](Greedy_Scheduling_Cooldown.md), [Sorting & Comparators](../java/Sorting_Comparators.md).
