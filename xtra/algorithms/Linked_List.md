# Linked List Patterns

Core techniques for singly-linked list problems: the dummy-node pattern for uniform build-loops, and Floyd's tortoise-and-hare for finding middles, detecting cycles, and locating cycle starts.

## Contents

- [Java implementations](#java-implementations)
- [Patterns](#patterns)
  - [Dummy node](#dummy-node)
  - [Tortoise and hare — find the middle](#tortoise-and-hare--find-the-middle)
  - [Tortoise and hare — detect a cycle](#tortoise-and-hare--detect-a-cycle)
  - [Tortoise and hare — find the cycle start](#tortoise-and-hare--find-the-cycle-start)
- [Problems for further practice](#problems-for-further-practice)

## Java implementations

LeetCode's standard `ListNode`:

```java
public class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}
```

Every pattern below works against this shape. The only Java-specific gotcha is that `ListNode` uses default `Object` identity — `a == b` compares references, which is exactly what you want when checking "did two pointers land on the same node?"

## Patterns

### Dummy node

A **dummy** (a.k.a. sentinel) is an extra node placed **before** the real head that you build against. Every iteration of a build loop looks identical — no special case for "is this the first node yet?" — and the return is uniform.

```java
ListNode dummy = new ListNode();
ListNode tail = dummy;

while (someCondition) {
    tail.next = pickNextNode();
    tail = tail.next;
}

return dummy.next;   // skip past the sentinel
```

Two things the dummy buys:

1. **No null check** for "is this the first node?" — `tail` is always a real node, so `tail.next = X` is always safe.
2. **Trivial return.** `dummy.next` is either the real head (if you appended anything) or `null` (if you appended nothing).

Concrete example — merging `1 → 3` with `2 → 4`:

```
dummy → ?                              (start)
dummy → 1 → 3          tail = 1        (took 1 from list1)
dummy → 1 → 2 → 4      tail = 2        (took 2 from list2)
dummy → 1 → 2 → 3      tail = 3        (took 3 from list1)
dummy → 1 → 2 → 3 → 4  tail = 4        (took 4 from list2)
return dummy.next                      → 1 → 2 → 3 → 4
```

Without the dummy, the first attach needs a `head == null ? head = x : tail.next = x` branch. The dummy eliminates it.

Typical use cases:
- Merging sorted lists
- Removing nodes by predicate (the removal loop can eliminate the head just like any other node)
- Any "build a new list" pattern

Do **not** allocate a second sentinel and chain them (`dummy → temp → …`). It works, but the return has to say `dummy.next.next`, and you're paying for two allocations to solve the same problem one solves.

### Tortoise and hare — find the middle

Two pointers starting at `head`. `slow` moves one step per iteration; `fast` moves two. When `fast` runs off the end, `slow` is at the middle.

```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
// slow is now at the middle
```

Landing position depends on parity:

| List | slow ends at |
|------|-------------|
| `1 → 2 → 3 → 4 → 5` (odd) | node 3 (the true middle) |
| `1 → 2 → 3 → 4` (even) | node 3 (the **second** of the two middles) |

If you want `slow` on the **last node of the first half** (useful when splitting for reverse+merge), use a tighter condition:

```java
while (fast.next != null && fast.next.next != null) { ... }
```

Now `slow` lands on node 2 for `[1,2,3,4]` and node 3 for `[1,2,3,4,5]` — in both cases the last node of the first half, with `slow.next` being the head of the second half.

Time O(n), space O(1).

### Tortoise and hare — detect a cycle

Same two-pointer setup. If a cycle exists, `fast` **laps** `slow` inside the loop and they meet. If no cycle, `fast` falls off the end.

```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) return true;
}
return false;
```

**Why they meet inside the cycle:** once both pointers are inside the loop, `fast` gains one node per iteration on `slow`. The gap between them (measured going forward around the cycle) shrinks by 1 each step. A shrinking non-negative integer inside a bounded cycle must hit 0.

**Common bug** — a stopping-guard on `fast` inside the loop body instead of the loop condition:

```java
if (fast.next != null && fast.next.next != null) fast = fast.next.next;
```

When `fast` reaches the tail this freezes it in place while `slow` keeps moving; `slow` catches up to a stationary `fast` and returns a false positive. Termination for "no cycle" must live in the **loop condition**, not a body guard.

Time O(n), space O(1).

### Tortoise and hare — find the cycle start

Extends cycle detection with a second phase. After `slow` and `fast` meet, reset one pointer to `head` and advance **both** one step at a time. They meet again at the cycle's starting node.

```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) break;
}
if (fast == null || fast.next == null) return null;   // no cycle

ListNode p = head;
while (p != slow) {
    p = p.next;
    slow = slow.next;
}
return p;
```

**Why phase 2 works.** Picture:

```
      cycle start
           ↓
   1 → 2 → 3 → 4 → 5 → 6 → 7
           ↑_______________|
```

Define three distances:
- `a` = head → cycle start (here 2).
- `L` = cycle length (here 5).
- `b` = cycle start → meeting point, going forward around the cycle.

**Decompose each pointer's total distance at the moment they meet.**

Both start at head, both end at the meeting point. So both must have covered exactly `a` steps to reach the cycle start, then some number of complete laps around the cycle, then `b` more steps to reach the meeting point:

- `slowDist = a + x·L + b`
- `fastDist = a + y·L + b`

where `x` is how many complete laps slow did (≥ 0) and `y` is how many complete laps fast did (also ≥ 0, larger than `x` since fast walked more).

**Now plug in `fastDist = 2 · slowDist`:**

```
a + y·L + b = 2(a + x·L + b)
    y·L − 2·x·L = a + b
    (y − 2x)·L = a + b
    a = (y − 2x)·L − b
```

Read the last equation as a **walking recipe** starting from the meeting point:
- Walk `(y − 2x)·L` steps forward — a whole number of laps, which lands you right back on the meeting point.
- Then walk `−b` more (i.e., `b` steps backward).
- The meeting point is exactly `b` steps *forward* from the cycle start (that's how we defined `b`), so walking `b` backward from it lands on the **cycle start**.

So walking `a` steps forward from the meeting point ends at the cycle start.

**In phase 2:**
- `p` starts at `head`. After `a` steps, `p` is at the cycle start (by definition of `a`).
- `slow` starts at the meeting point. After `a` steps, `slow` is at the cycle start (by the equation).

Both move one step at a time, so they arrive together, at the cycle start.

**Numeric check on the picture** (`a = 2, L = 5`). From the simulation, slow walked 5 steps (`x = 0`), fast walked 10 (`y = 1`), meeting at node 6. So `b = 3`. Verify the equation: `a = (y − 2x)·L − b = (1 − 0)·5 − 3 = 2`. ✓

From the meeting node (6), walk 2 → `6 → 7 → 3`. From head (1), walk 2 → `1 → 2 → 3`. Both land on node 3, the cycle start. ✓

Time O(n), space O(1).

## Problems for further practice

### Dummy node

- [LeetCode 21 — Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/)
- [LeetCode 203 — Remove Linked List Elements](https://leetcode.com/problems/remove-linked-list-elements/) — the dummy lets the head be removed like any other node.
- [LeetCode 2 — Add Two Numbers](https://leetcode.com/problems/add-two-numbers/) — build the sum list against a dummy.

### Tortoise and hare — find the middle

- [LeetCode 876 — Middle of the Linked List](https://leetcode.com/problems/middle-of-the-linked-list/)

### Tortoise and hare — cycle detection

- [LeetCode 141 — Linked List Cycle](https://leetcode.com/problems/linked-list-cycle/) — detect only.
- [LeetCode 142 — Linked List Cycle II](https://leetcode.com/problems/linked-list-cycle-ii/) — return the cycle start (two-phase Floyd's).
- [LeetCode 287 — Find the Duplicate Number](https://leetcode.com/problems/find-the-duplicate-number/) — Floyd's on an implicit "next = nums[i]" list.
