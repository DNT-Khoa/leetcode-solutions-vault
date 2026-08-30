# Stack & Queue Patterns

Tricks for augmenting stacks and queues so they answer running queries (min, max, etc.) in O(1).

## Contents

- [Java implementations](#java-implementations)
- [Patterns](#patterns)
  - [Stack with O(1) running min / max](#stack-with-o1-running-min--max)
  - [Two stacks simulate a queue](#two-stacks-simulate-a-queue)
  - [Min / max queue from two augmented stacks](#min--max-queue-from-two-augmented-stacks)
  - [Monotonic stack](#monotonic-stack)
  - [Monotonic deque (sliding-window min / max)](#monotonic-deque-sliding-window-min--max)
- [Problems for further practice](#problems-for-further-practice)

## Java implementations

For CP, use `ArrayDeque<E>` for both stack and queue roles. It's a backing array with O(1) amortized add/remove at either end, and it implements both `Deque<E>` and `Queue<E>`.

### As a stack

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(5);            // add to top
int top = stack.peek();   // 5, no removal
int v   = stack.pop();    // 5, removes
boolean empty = stack.isEmpty();
```

### As a queue

```java
Queue<Integer> queue = new ArrayDeque<>();
queue.offer(5);            // enqueue at back
int front = queue.peek();  // 5, no removal
int v     = queue.poll();  // 5, dequeue from front
```

### Notes

`ArrayDeque` has two ends — **first** (front / head) and **last** (back / tail):

```
        FIRST                                  LAST
    (front / head)                        (back / tail)
         ↓                                      ↓
       ┌──────────────────────────────────────┐
       │   A    B    C    D    E    F         │
       └──────────────────────────────────────┘
         ↑                                      ↑
   pop / poll / peek                       offer / offerLast
   push / offerFirst                       pollLast / peekLast
   peekFirst / pollFirst
```

A **queue** flows `last → first`: enter via `offer`, leave via `poll`. A **stack** lives entirely at `first`: both push and pop act there.

Each operation has a throwing flavor and a sentinel-returning flavor. Stack mode (`Deque`) and queue mode (`Queue`) use different method names:

**Stack mode** — operates on `first`:

| Op   | Throws       | Returns sentinel       |
|------|--------------|------------------------|
| Push | `push(e)`    | `offerFirst(e)`        |
| Pop  | `pop()`      | `pollFirst()`          |
| Peek | `getFirst()` | `peekFirst()` / `peek()` |

**Queue mode** — enqueue at `last`, dequeue at `first`:

| Op         | Throws      | Returns sentinel |
|------------|-------------|------------------|
| Enqueue    | `add(e)`    | `offer(e)`       |
| Dequeue    | `remove()`  | `poll()`         |
| Peek front | `element()` | `peek()`         |

In CP, prefer the right column — null-checking is simpler than try/catch. (For `ArrayDeque`, insert never actually fails since it auto-grows; the distinction only matters for remove/examine.)

**Fully sentinel-returning stack:** the standard `push` / `pop` methods throw. To avoid that, declare the variable as `Deque<E>` (not `Queue<E>` — which doesn't expose `offerFirst` / `pollFirst`) and use those instead.

For `int` workloads, `ArrayDeque<Integer>` boxes every element. If hot, a manual `int[]` stack with an `int top` index is faster.

## Patterns

### Stack with O(1) running min / max

A normal stack can only tell you its min by scanning everything — O(N). The trick: store extra running statistics alongside each value, so the **top frame** always knows the min (or max) of the whole stack.

#### The rule

When pushing `x`, compute:
```
newMin = stack.empty() ? x : min(x, stack.top().min)
newMax = stack.empty() ? x : max(x, stack.top().max)
push (value=x, min=newMin, max=newMax)
```

Pop just removes the top frame — nothing to recompute, because the frame below already has its own correct running min/max baked in (it never depended on the popped one).

`getMin()` / `getMax()` = read top frame. O(1).

#### Concrete example — push 5, 3, 7, 2

| Step | Stack (bottom → top), `(value, min, max)` | getMin | getMax |
|------|-------------------------------------------|-------:|-------:|
| push 5 | `(5,5,5)` | 5 | 5 |
| push 3 | `(5,5,5) (3,3,5)` | 3 | 5 |
| push 7 | `(5,5,5) (3,3,5) (7,3,7)` | 3 | 7 |
| push 2 | `(5,5,5) (3,3,5) (7,3,7) (2,2,7)` | 2 | 7 |
| pop    | `(5,5,5) (3,3,5) (7,3,7)` | 3 | 7 |

The popped frame disappears; the new top `(7,3,7)` already encodes the right answers.

#### Key intuition

Each frame stores "the min/max of everything currently in the stack from the bottom up to me." That invariant is preserved by pushes (since you fold in the previous top) and trivially by pops (since older frames don't reference newer ones).

You can extend this to any **monoid** statistic — running sum, running gcd, etc.

#### Cost

| Operation | Time |
|-----------|------|
| push      | O(1) |
| pop       | O(1) |
| getMin / getMax | O(1) |

Space: O(N) for N elements (constant extra per frame).

### Two stacks simulate a queue

Stacks are LIFO; queues are FIFO. You can fake a queue with two stacks:

- `in` stack receives every enqueue.
- `out` stack serves every dequeue.

```
enqueue(x):
    in.push(x)

dequeue():
    if out.empty():
        while not in.empty():
            out.push(in.pop())   // drains in → out, reversing order
    return out.pop()
```

#### Why it works

Pushes to `in` arrive in insertion order: bottom = oldest of that batch, top = newest. Draining `in` onto `out` **reverses** that order, so the oldest element ends up on top of `out` — exactly what FIFO dequeue wants.

#### Concrete example — enq 1, enq 2, enq 3, deq, deq

| Op | `in` (bot→top) | `out` (bot→top) | dequeue returns |
|----|----------------|------------------|-----------------|
| enq 1 | `1` | empty | — |
| enq 2 | `1 2` | empty | — |
| enq 3 | `1 2 3` | empty | — |
| deq | empty | `3 2 1` *(drain)*, then pop | **1** |
| deq | empty | `3 2` | **2** |

#### Cost

| Operation | Time |
|-----------|------|
| enqueue   | O(1) |
| dequeue   | O(1) amortized (worst case O(N) when triggering a drain) |

Space: O(N) total across both stacks.

**Why dequeue is O(1) amortized:** a drain only happens when `out` is empty, and once an element moves from `in` to `out` it never moves again. So the total work across any sequence of N operations is bounded by ~3N (each element pushed once, moved once, popped once) → O(1) per op on average.

The drain only runs when `out` is empty. Don't drain while `out` still has things, or you'd interleave two batches and lose FIFO order on the older batch.

### Min / max queue from two augmented stacks

Combine the two tricks: each of `in` and `out` is an *augmented* stack (Section 1). You get a queue that supports enqueue, dequeue, **and** min/max queries — all O(1) amortized.

#### Structure

- `in`: stack of frames `(value, runningMin, runningMax)`
- `out`: stack of frames `(value, runningMin, runningMax)`

Every queue element lives in exactly one stack. So:

```
queue.min = min(in.top.runningMin, out.top.runningMin)
queue.max = max(in.top.runningMax, out.top.runningMax)
```

(Skip an empty side.)

#### Concrete example — enq 5, enq 3, enq 7, deq, enq 2

| Op | `in` (bot→top) | `out` (bot→top) | queue.min | queue.max |
|----|----------------|------------------|----------:|----------:|
| enq 5 | `(5,5,5)` | empty | 5 | 5 |
| enq 3 | `(5,5,5) (3,3,5)` | empty | 3 | 5 |
| enq 7 | `(5,5,5) (3,3,5) (7,3,7)` | empty | 3 | 7 |
| deq → 5 | empty | `(7,7,7) (3,3,7) (5,3,7)`, pop top | 3 | 7 |
| (after) | empty | `(7,7,7) (3,3,7)` | 3 | 7 |
| enq 2 | `(2,2,2)` | `(7,7,7) (3,3,7)` | min(2,3)=2 | max(2,7)=7 |

#### What happens during the drain

When `in` drains into `out`, each push onto `out` applies the Section 1 push rule fresh. So `out`'s running min/max correctly describes whatever's now in `out`, regardless of how it got there. The augmentation invariant only depends on each stack's *current contents* — not on the original insertion order in the queue.

#### How many stacks total

**Two.** One frame already carries both `runningMin` and `runningMax`, so a single augmented `in` / `out` pair handles both queries. Don't split into a separate min-queue + max-queue (4 stacks) — that's twice the work for nothing.

#### Cost

| Operation | Time |
|-----------|------|
| enqueue   | O(1) |
| dequeue   | O(1) amortized |
| getMin / getMax | O(1) |

Space: O(N) total across both stacks.

#### Use case

Sliding window min/max where the window grows on the right and shrinks on the left:

```
queue = MinMaxQueue()
left = 0
for right in 0..N-1:
    queue.enqueue(a[right])
    while queue.max() - queue.min() > k:
        queue.dequeue()
        left++
    // a[left..right] is now a valid window
```

O(N) total — each element is enqueued, drained, and dequeued at most once each.

### Monotonic stack

A stack whose values are kept in a fixed monotonic order — typically strictly decreasing from bottom to top. When a new value arrives, pop everything at the top that it dominates, then push it. Popped elements never return.

#### Use case

For each index `i`, find the nearest index `j` on one side with a strictly greater (or smaller) value. Classic: "next greater element to the right," "previous smaller to the left."

#### The rule (for "next greater to the right")

Stack stores **indices** still waiting for an answer.

```
for i in 0..N-1:
    while not stack.empty() and nums[stack.top()] < nums[i]:
        j = stack.pop()
        nextGreater[j] = i        // i is j's answer
    stack.push(i)
// anything left on the stack has no greater element to the right
```

#### Concrete example — `nums = [2, 1, 3]`

| i | nums[i] | action | stack (bot→top, by index) | nextGreater so far |
|---|---------|--------|---------------------------|--------------------|
| 0 | 2 | push 0 | `[0]` | `[?, ?, ?]` |
| 1 | 1 | `nums[0]=2 < 1`? No. push 1. | `[0, 1]` | `[?, ?, ?]` |
| 2 | 3 | `nums[1]=1 < 3`? Yes, pop 1 → ans[1]=2. `nums[0]=2 < 3`? Yes, pop 0 → ans[0]=2. push 2. | `[2]` | `[2, 2, ?]` |

Index 2 has no greater element to its right, so it stays unmatched on the stack.

#### Key intuition

When a bigger value shows up, every smaller index still on the stack is *resolved forever* — its answer is the current element. None of them need to wait any longer, so we pop them all in one sweep. Each index is pushed once and popped once → **O(N) total**.

Flip the comparator for "next smaller" or mirror the iteration direction (right-to-left) for "previous greater / smaller."

#### Cost

| Operation | Time |
|-----------|------|
| Full sweep | O(N) |

Space: O(N) for the stack.

### Monotonic deque (sliding-window min / max)

A monotonic stack with **two ends**. The back works exactly like a monotonic stack — pop dominated elements when a new one arrives. The front is used to **drop elements that aged out of the window**.

Stores **indices** (not values) so we can test window membership.

#### The rule (for sliding window max, window size `k`)

Deque of indices, kept so that `nums[index]` is **strictly decreasing from front to back**.

```
for right in 0..N-1:
    // 1. maintain monotonic invariant via the back
    while not dq.empty() and nums[dq.back()] <= nums[right]:
        dq.popBack()
    dq.pushBack(right)

    // 2. drop the front if it's expired
    if dq.front() <= right - k:
        dq.popFront()

    // 3. record answer once window is full
    if right >= k - 1:
        result[right - k + 1] = nums[dq.front()]
```

For sliding window **min**, flip the comparator: pop while `nums[back] >= nums[right]`. The invariant becomes strictly increasing.

#### Concrete example — `nums = [1, 3, -1, -3, 5, 3]`, `k = 3`

Deque shown as `[index(value), ...]` front→back.

| right | num | back-pop & push | front-drop | dq | window max |
|-------|-----|-----------------|------------|----|------------|
| 0 | 1  | push 0 | — | `[0(1)]` | — |
| 1 | 3  | pop 0 (1≤3), push 1 | — | `[1(3)]` | — |
| 2 | -1 | push 2 | — | `[1(3), 2(-1)]` | **3** |
| 3 | -3 | push 3 | — | `[1(3), 2(-1), 3(-3)]` | **3** |
| 4 | 5  | pop 3, 2, 1 (all ≤ 5), push 4 | — | `[4(5)]` | **5** |
| 5 | 3  | push 5 | — | `[4(5), 5(3)]` | **5** |

#### Key intuition

- **Why decreasing?** When `x` arrives and a smaller value `y` already sits in the deque, `y` is dominated by `x` for the entire remaining time `y` is in the window — toss it.
- **Why store indices?** Need to know whether the front still belongs to `[right - k + 1, right]`. Values alone can't tell you that.
- **Why the front is always the max:** after the back-popping step, every survivor older than any given index has a strictly greater value (otherwise it would have been popped). The oldest survivor is therefore the largest.

#### Cost

| Operation | Time |
|-----------|------|
| Full sweep | O(N) amortized |

Each index is pushed and popped at most once.

Space: O(K) — the deque holds at most one index per active window slot.

## Problems for further practice

### Min / max stack (augmented stack)

- [LeetCode 155 — Min Stack](https://leetcode.com/problems/min-stack/)
- [LeetCode 716 — Max Stack](https://leetcode.com/problems/max-stack/)

### Two-stack queue

- [LeetCode 232 — Implement Queue using Stacks](https://leetcode.com/problems/implement-queue-using-stacks/)

### Monotonic stack

- [LeetCode 496 — Next Greater Element I](https://leetcode.com/problems/next-greater-element-i/)
- [LeetCode 739 — Daily Temperatures](https://leetcode.com/problems/daily-temperatures/) — same pattern, returns the distance instead of the value.
- [LeetCode 503 — Next Greater Element II](https://leetcode.com/problems/next-greater-element-ii/) — circular array; iterate twice.
- [LeetCode 84 — Largest Rectangle in Histogram](https://leetcode.com/problems/largest-rectangle-in-histogram/) — for each bar, find nearest smaller on left and right.

### Sliding window min / max

- [LeetCode 239 — Sliding Window Maximum](https://leetcode.com/problems/sliding-window-maximum/) — fixed-size window.
- [LeetCode 1438 — Longest Continuous Subarray With Absolute Diff ≤ Limit](https://leetcode.com/problems/longest-continuous-subarray-with-absolute-diff-less-than-or-equal-to-limit/) — variable-size window with `max − min ≤ k`.
- [LeetCode 2762 — Continuous Subarrays](https://leetcode.com/problems/continuous-subarrays/) — count subarrays with `|max − min| ≤ 2`. Same pattern as 1438, just counting instead of measuring length.
- [LeetCode 2398 — Maximum Number of Robots Within Budget](https://leetcode.com/problems/maximum-number-of-robots-within-budget/) — variable-size window where the constraint combines window max and window sum.
- [LeetCode 1696 — Jump Game VI](https://leetcode.com/problems/jump-game-vi/) — DP where each `dp[i]` needs the max of the previous `k` dp values; sliding window max as a DP accelerator.
- [LeetCode 1425 — Constrained Subsequence Sum](https://leetcode.com/problems/constrained-subsequence-sum/) — same DP-accelerator pattern as 1696.
- [Codeforces EDU 307093F — Segments with Small Spread](https://codeforces.com/edu/course/2/lesson/9/2/practice/contest/307093/problem/F) — count segments with `max − min ≤ k`.
