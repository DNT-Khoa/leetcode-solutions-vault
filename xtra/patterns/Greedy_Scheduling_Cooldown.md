# Greedy Scheduling with Cooldown

Simulate discrete time steps. At each tick, greedily pick the highest-priority item that isn't on cooldown; recently-picked items sit in a waiting queue until they're ready again.

## When to reach for it

The problem has all three of:

1. **Discrete time steps** — you're placing items one slot at a time.
2. **A reuse delay** — after picking item X, you can't pick X again for some fixed duration (cooldown / minimum distance apart).
3. **A "most urgent" notion** — usually the item with the highest remaining frequency.

Goal is typically "minimize total time" or "produce the longest valid sequence".

## The two-container template

- **Max-heap** — items currently available to pick, keyed by priority (usually remaining count).
- **FIFO queue** — items on cooldown, each stored as `(state, readyTime)`.

Why a plain FIFO is enough for the cooldown side: entries are inserted in time order, each with `readyTime = currentTime + cooldown` where cooldown is **constant**. So `readyTime` values are already sorted in insertion order — the front is always the next to become ready. No second heap needed.

## Loop skeleton

```
time = 0
while heap not empty OR waitQueue not empty:
    time++

    // 1. release anything whose cooldown just ended
    while waitQueue.front.readyTime == time:
        heap.push(waitQueue.pop().state)

    // 2. pick the most urgent available item (if any)
    if heap not empty:
        item = heap.pop()
        // ... consume one unit of item ...
        if item still has work left:
            waitQueue.push(item, readyTime = time + cooldown + 1)
    // else: this tick is idle
```

**Order matters.** With `readyTime = time + cooldown + 1` (meaning "earliest tick this item can execute again"), release must happen *before* execute in the same iteration — otherwise the release doesn't take effect until the next tick and you burn an extra idle slot every time.

Alternative: keep execute-then-release order, but shift the formula to `readyTime = time + cooldown` so the release fires one tick "early" and the item sits ready in the heap for next tick. Pick whichever is easier to keep straight.

## Concrete trace — Task Scheduler (`AAABBB`, cooldown=2)

Start: heap = `[3, 3]` (counts of A and B; identities don't matter).

| t | heap before | action              | waitQueue after         |
|---|-------------|---------------------|-------------------------|
| 1 | [3, 3]      | pop 3 → push (2,4)  | [(2,4)]                 |
| 2 | [3]         | pop 3 → push (2,5)  | [(2,4), (2,5)]          |
| 3 | []          | idle                | [(2,4), (2,5)]          |
| 4 | [] → [2]    | release (2,4); pop 2 → push (1,7) | [(2,5), (1,7)] |
| 5 | [] → [2]    | release (2,5); pop 2 → push (1,8) | [(1,7), (1,8)] |
| 6 | []          | idle                | [(1,7), (1,8)]          |
| 7 | [] → [1]    | release (1,7); pop 1 → done       | [(1,8)] |
| 8 | [] → [1]    | release (1,8); pop 1 → done       | []      |

Answer: **8 ticks**, matching the schedule `A B _ A B _ A B`.

**Why we only store the count, not the letter.** Cooldown treats all tasks the same. Two tasks with count 3 are interchangeable — swapping their labels gives an equally valid schedule of the same length. The heap only needs to know "which pile is tallest right now", and that's a number.

## Cost

Let `N` = total items, `k` = distinct item types, `T` = final answer.

- **Time:** `O(N + T · log k)`. Each tick does at most one heap push and one pop, each `O(log k)`. `T` can grow to `O(N · cooldown)` in the worst case (one dominant task type).
- **Space:** `O(k)`. The heap and wait queue together hold at most `k` entries at any moment.

## Sibling problems

- **[Task Scheduler](https://leetcode.com/problems/task-scheduler/)** (LC 621) — the canonical version. See [leetcode/task-scheduler/](../../leetcode/task-scheduler/).
- **[Reorganize String](https://leetcode.com/problems/reorganize-string/)** (LC 767) — cooldown = 1; produce a valid string instead of counting ticks.
- **[Rearrange String k Distance Apart](https://leetcode.com/problems/rearrange-string-k-distance-apart/)** (LC 358) — cooldown = k. Direct generalization.
- **[Distant Barcodes](https://leetcode.com/problems/distant-barcodes/)** (LC 1054) — cooldown = 1, integers instead of chars.
- **[Longest Happy String](https://leetcode.com/problems/longest-happy-string/)** (LC 1405) — twist: forbid three consecutive of the same letter (cooldown-of-1 only after a run of 2).
- **[Single-Threaded CPU](https://leetcode.com/problems/single-threaded-cpu/)** (LC 1834) — different priority (shortest job) but the same "available heap + waiting queue" shape.
