# Greedy

A **greedy** algorithm builds the answer **one choice at a time** and **never goes back**. At each step it takes the option that looks best right now — and the whole difficulty is knowing that option is **safe**: that it never rules out a correct answer.

The code is usually short. The thinking is in the *why*.

## Contents

- [The three signs of a greedy problem](#the-three-signs-of-a-greedy-problem)
- [Greedy vs DP vs backtracking](#greedy-vs-dp-vs-backtracking)
- [When greedy fails — Coin Change](#when-greedy-fails--coin-change)
- [How to convince yourself a greedy choice is safe](#how-to-convince-yourself-a-greedy-choice-is-safe)
- [Pattern 1 — Drop a negative running sum](#pattern-1--drop-a-negative-running-sum)
- [Pattern 2 — Move the goal backward](#pattern-2--move-the-goal-backward)
- [Pattern 3 — Zones with a growing end](#pattern-3--zones-with-a-growing-end)
- [Pattern 4 — Eliminate a whole stretch after a failure](#pattern-4--eliminate-a-whole-stretch-after-a-failure)
- [Pattern 5 — The forced choice (smallest first)](#pattern-5--the-forced-choice-smallest-first)
- [Pattern 6 — Filter out the unusable, then check](#pattern-6--filter-out-the-unusable-then-check)
- [Pattern 7 — Track a range of possibilities](#pattern-7--track-a-range-of-possibilities)
- [Pattern 8 — Sort, then pick by a key](#pattern-8--sort-then-pick-by-a-key)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## The three signs of a greedy problem

1. **The answer is built step by step** — pick a card, take a jump, choose a start station, cut a partition.
2. **Each step has an obviously best (or forced) option**, so you don't need to try the others.
3. **You never undo a choice.** One pass, no backtracking, no table of alternatives.

Sign 2 is the one that matters. Common shapes of "the best option":

- smallest / largest first
- earliest end / farthest reach
- drop it as soon as it's negative
- skip everything that can't possibly work

## Greedy vs DP vs backtracking

| | What it does with the options at each step | Typical cost |
|---|---|---|
| **Backtracking** | tries **every** option, undoes, tries the next | exponential |
| **Dynamic programming** | **records** the best result for every option / subproblem | polynomial, usually with a table |
| **Greedy** | takes **one** option and argues the others are never better | often O(N) or O(N log N), O(1) extra space |

Many greedy solutions are DP with the table collapsed to one or two variables, because only the previous step ever matters. Kadane (Pattern 1) is the classic example: "best sum ending at `i`" only needs "best sum ending at `i - 1`".

## When greedy fails — Coin Change

[322. Coin Change](../../coin-change/notes.md), with `coins = [1, 3, 4]` and `amount = 6`:

- **Greedy (biggest coin first):** `4 + 1 + 1` = 3 coins.
- **Optimal:** `3 + 3` = 2 coins.

Taking the 4 looked best but led somewhere worse, and there's no argument that "biggest coin is always safe". So Coin Change needs DP.

This is the most useful habit for greedy problems: **before trusting a rule, try to break it with a tiny example.**

## How to convince yourself a greedy choice is safe

You don't need formal proofs, but you do need *a reason*. Four kinds of reasons cover almost everything:

| Reason | The question to ask | Used in |
|---|---|---|
| **Forced** | Is there really only one option? | Hand of Straights — the smallest card can only *start* a group |
| **Never hurts** | Can this choice ever make what comes later worse? | Kadane — a negative prefix only lowers what follows |
| **Dominance** | Does the chosen option leave at least as much room as every alternative? | Non-overlapping Intervals — the earlier end leaves more room |
| **Elimination** | If this attempt failed, which other candidates are guaranteed to fail too? | Gas Station — every skipped station reaches the failure point with less gas |

And a practical check: **compare against brute force** on small random inputs. If a slow, obviously-correct version agrees on thousands of cases, the greedy rule is almost certainly right.

## Pattern 1 — Drop a negative running sum

From [53. Maximum Subarray](../../maximum-subarray/notes.md) (**Kadane's algorithm**).

Walk left to right with a running sum. **If the sum is negative, throw it away before adding the next number** — carrying a debt forward can only make every later subarray smaller. Remember the largest sum ever seen.

```java
int curSum = 0;
int maxSum = nums[0];          // not 0: an all-negative array's answer is negative
for (int num : nums) {
    if (curSum < 0) curSum = 0;
    curSum += num;
    maxSum = Math.max(maxSum, curSum);
}
```

- Why safe: **never hurts** — a negative prefix can only lower whatever comes after it.
- Cost: O(N) time, O(1) space.
- Next step: [152. Maximum Product Subarray](https://leetcode.com/problems/maximum-product-subarray/) — same idea, but track the **min** too, because a negative times a negative becomes large.

## Pattern 2 — Move the goal backward

From [55. Jump Game](../../jump-game/notes.md).

Start with the **goal** at the last index and walk **backward**. Whenever an index can reach the goal, **make that index the new goal**. At the end, the answer is whether the goal reached index 0.

```java
int targetIndex = nums.length - 1;
for (int i = targetIndex - 1; i >= 0; i--) {
    if (i + nums[i] >= targetIndex) targetIndex = i;
}
return targetIndex == 0;
```

- Why safe: **dominance** — any index that can reach a farther goal can also reach the nearer one, so moving the goal as little as possible never cuts off a path.
- Cost: O(N) time, O(1) space. Nested-looking versions can still be O(N) if their scans never overlap — every index is looked at once in total.

## Pattern 3 — Zones with a growing end

Walk left to right in **zones**. While walking a zone, keep track of **how far the next zone (or this one) must extend**; when `i` reaches the end of the current zone, close it.

From [45. Jump Game II](../../jump-game-ii/notes.md) — zones are "reachable with `k` jumps", like BFS without a queue:

```java
int count = 0, start = 0, end = 0;
while (end < nums.length - 1) {          // `<`, not `!=`: a jump can overshoot the end
    int nextEnd = end;
    while (start <= end) {               // walk the whole current zone
        nextEnd = Math.max(start + nums[start], nextEnd);
        start++;
    }
    count++;
    end = nextEnd;
}
return count;
```

From [763. Partition Labels](../../partition-labels/notes.md) — a zone is one partition, and its end grows to cover the last occurrence of every letter inside it:

```java
int start = 0, end = 0;
for (int i = 0; i < n; i++) {
    end = Math.max(end, lastIndex.get(s.charAt(i)));
    if (i == end) {                      // everything seen so far ends here
        result.add(end - start + 1);
        start = i + 1;
    }
}
```

| | Jump Game II | Partition Labels |
|---|---|---|
| Grow the end with | `i + nums[i]` | last index of `s[i]` |
| When the zone closes | count a jump | record the partition size |

- Why safe: Jump Game II — every index in a zone costs the same number of jumps, so only the **farthest** reach matters. Partition Labels — a cut is only possible where every letter seen so far has already ended, and cutting at the **first** such point gives the most parts.
- Partition Labels is also **merge intervals** in disguise: each letter is the interval `[first, last]`, and the partitions are the merged groups (see [Intervals](Intervals.md#pattern-2--merge-overlapping-intervals)).
- Cost: O(N) time, O(1) space.

**The trap:** the loop must follow the end **as it grows**, not the end it started with. And if you write Jump Game II as a single `for` loop, stop **before** the last index — a jump is counted when you need to *leave* a zone, and you never need to leave the destination.

## Pattern 4 — Eliminate a whole stretch after a failure

From [134. Gas Station](../../gas-station/notes.md).

Work with `diff[i] = gas[i] - cost[i]`, the change in the tank over the leg from station `i` to `i + 1`.

1. **Total check:** if the sum of all `diff` is negative, no start works → `-1`. The total doesn't depend on where you start — it's the tank after the full circle.
2. **Tank with resets:** walk once; when the tank goes negative on the leg out of station `B`, every station from the current start up to `B` is eliminated. Reset the tank to 0 and try `B + 1`.
3. The last surviving candidate is the answer.

```java
int runningTotal = 0, runningTankSum = 0, station = 0;
for (int i = 0; i < n; i++) {
    if (runningTankSum < 0) {
        runningTankSum = 0;
        station = i;
    }
    int diffNet = gas[i] - cost[i];
    runningTotal += diffNet;
    runningTankSum += diffNet;
}
return runningTotal >= 0 ? station : -1;
```

Why each part is safe:

- **Elimination:** starting at `A`, every station you reached on the way had leftover gas in the tank. Starting at one of them instead throws that gas away and drives into the same failing leg.
- **Wraparound:** the sum from the final candidate `S` to the end is exactly `R`, and the skipped stations before `S` add up to a negative deficit. `total = R + deficit`, so `total ≥ 0` is the same as "`R` covers the deficit". Checking the total *is* checking the wraparound.

Cost: O(N) time, O(1) space.

## Pattern 5 — The forced choice (smallest first)

From [846. Hand of Straights](../../hand-of-straights/notes.md).

The **smallest remaining card** can't be in the middle of a group — nothing smaller is left to come before it. So it **must** start a group `x, x+1, …, x + groupSize - 1`. Take it, remove that group, repeat; fail if any card is missing.

```java
if (hand.length % groupSize != 0) return false;
TreeMap<Integer, Integer> treeMap = new TreeMap<>();
for (int card : hand) treeMap.merge(card, 1, Integer::sum);

while (!treeMap.isEmpty()) {
    int firstCard = treeMap.firstKey();
    for (int i = firstCard; i < firstCard + groupSize; i++) {   // card VALUES, not a count
        if (!treeMap.containsKey(i)) return false;
        int count = treeMap.get(i);
        if (count == 1) treeMap.remove(i);                       // drop the key at 0
        else treeMap.put(i, count - 1);
    }
}
return true;
```

- Why safe: **forced** — there's never a real choice to make.
- Data structure: a sorted multiset (see [TreeMap](../java/TreeMap.md#pattern--sorted-multiset-value--count)). Sorting the array + a `HashMap` of counts also works.
- Cost: O(N log N) time, O(N) space.

## Pattern 6 — Filter out the unusable, then check

From [1899. Merge Triplets to Form Target Triplet](../../merge-triplets-to-form-target-triplet/notes.md).

Merging takes the **max** of each position, so a triplet with any value **above** the target in any position can never be used — it would push that position past the target. Ignore those. Among the rest, each position of the target needs **at least one** triplet that hits it exactly; merging all the safe ones then gives exactly the target.

- Why safe: using **every** safe triplet never hurts — none of them can push a position over the target.
- Cost: O(N) time; O(1) extra space if you check as you filter (three booleans) instead of storing the valid triplets.

## Pattern 7 — Track a range of possibilities

From [678. Valid Parenthesis String](../../valid-parenthesis-string/notes.md).

Each `*` can be `(`, `)` or empty, so the number of open brackets isn't one number but a **set of possibilities**. That set is always a range with no gaps, so two numbers describe it:

- `low` — the fewest possible open brackets
- `high` — the most possible open brackets

| char | low | high |
|---|---|---|
| `(` | `+1` | `+1` |
| `)` | `-1` | `-1` |
| `*` | `-1` (as `)`) | `+1` (as `(`) |

```java
int low = 0, high = 0;
for (char c : s.toCharArray()) {
    switch (c) {
        case '(' -> { low++; high++; }
        case ')' -> { low--; high--; }
        default  -> { low--; high++; }
    }
    if (high < 0) return false;   // every possibility is negative → none left
    if (low < 0) low = 0;         // cross out the impossible negative counts
}
return low == 0;                  // is 0 a possible count?
```

- Why safe: you never commit to what a `*` is. Clamping `low` just discards counts that can't exist.
- Cost: O(N) time, O(1) extra space (strictly O(N) because `toCharArray` copies the string; loop with `charAt` for true O(1)).

## Pattern 8 — Sort, then pick by a key

Some greedy rules only become obvious after sorting:

- **Earliest end first** — [435. Non-overlapping Intervals](../../non-overlapping-intervals/notes.md): when two intervals overlap, keep the one that ends earlier; it leaves the most room. Details in [Intervals, Pattern 3](Intervals.md#pattern-3--greedy-keep-the-one-that-ends-first).
- **Most frequent first** — [621. Task Scheduler](../../task-scheduler/notes.md): run the task with the most copies left, with a cooldown queue. Details in [Greedy Scheduling with Cooldown](Greedy_Scheduling_Cooldown.md).

## Cost summary

| Problem | Pattern | Time | Extra space |
|---|---|---|---|
| 53. Maximum Subarray | 1 | O(N) | O(1) |
| 55. Jump Game | 2 | O(N) | O(1) |
| 45. Jump Game II | 3 | O(N) | O(1) |
| 763. Partition Labels | 3 | O(N) | O(1) (≤ 26 letters) |
| 134. Gas Station | 4 | O(N) | O(1) |
| 846. Hand of Straights | 5 | O(N log N) | O(N) |
| 1899. Merge Triplets | 6 | O(N) | O(1) possible |
| 678. Valid Parenthesis String | 7 | O(N) | O(1) |
| 435. Non-overlapping Intervals | 8 | O(N log N) | O(N) for the sort |
| 621. Task Scheduler | 8 | O(N + T log K), K ≤ 26 | O(1) |

## Pitfalls checklist

- [ ] Tried to **break the rule** with a tiny example (like Coin Change's `[1, 3, 4]`, amount 6)?
- [ ] Initial values: Kadane's `maxSum` starts at `nums[0]`, not 0.
- [ ] Growing-end loops follow the **current** end, not the one they started with.
- [ ] Jump Game II single-loop version stops **before** the last index.
- [ ] A jump can **overshoot** the end — use `<` (or cap the end), not `!=`.
- [ ] Loop over **values** vs a **count**: Hand of Straights runs `i < firstCard + groupSize`, not `i < groupSize`.
- [ ] Multiset counts: **remove** a key at 0 instead of storing 0.
- [ ] Gas Station: move the start when the **running tank** goes negative, not when a single `diff` is negative.
- [ ] Every `while` loop moves its pointer forward.

## Problems for further practice

Solved in this repo: all 8 above plus [435](../../non-overlapping-intervals/notes.md) and [621](../../task-scheduler/notes.md).

Next, roughly in order of difficulty:

| Problem | Difficulty | Greedy idea |
|---|---|---|
| [455. Assign Cookies](https://leetcode.com/problems/assign-cookies/) | Easy | sort both; give each child the smallest cookie that satisfies them |
| [860. Lemonade Change](https://leetcode.com/problems/lemonade-change/) | Easy | when giving change, prefer the larger bill |
| [1005. Maximize Sum of Array After K Negations](https://leetcode.com/problems/maximize-sum-of-array-after-k-negations/) | Easy | flip the most negative numbers first |
| [409. Longest Palindrome](https://leetcode.com/problems/longest-palindrome/) | Easy | use every pair, plus at most one odd center |
| [452. Minimum Number of Arrows to Burst Balloons](https://leetcode.com/problems/minimum-number-of-arrows-to-burst-balloons/) | Medium | earliest end first (Pattern 8) |
| [1029. Two City Scheduling](https://leetcode.com/problems/two-city-scheduling/) | Medium | sort by the cost difference between the two cities |
| [406. Queue Reconstruction by Height](https://leetcode.com/problems/queue-reconstruction-by-height/) | Medium | place the tallest first, insert by index |
| [767. Reorganize String](https://leetcode.com/problems/reorganize-string/) | Medium | most frequent first, cooldown 1 |
| [402. Remove K Digits](https://leetcode.com/problems/remove-k-digits/) | Medium | greedy + monotonic stack: drop a digit when a smaller one follows |
| [316. Remove Duplicate Letters](https://leetcode.com/problems/remove-duplicate-letters/) | Medium | greedy + stack, using each letter's last index |
| [135. Candy](https://leetcode.com/problems/candy/) | Hard | two passes, left-to-right then right-to-left |
| [630. Course Schedule III](https://leetcode.com/problems/course-schedule-iii/) | Hard | sort by deadline; max-heap to drop the longest course |
| [871. Minimum Number of Refueling Stops](https://leetcode.com/problems/minimum-number-of-refueling-stops/) | Hard | pass stations, refuel from the biggest one seen only when you must |

Related docs: [Intervals](Intervals.md), [Greedy Scheduling with Cooldown](Greedy_Scheduling_Cooldown.md), [Heap & PriorityQueue](Heap_PriorityQueue.md), [TreeMap and TreeSet](../java/TreeMap.md).
