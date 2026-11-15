# Binary Search on the Answer (Boundary Template)

Finding the **boundary** of a monotonic yes/no predicate by binary searching over a *value range*, not array positions. The `while (right - left > 1)` "gap-of-2" style: keep two pointers on opposite sides of the boundary and squeeze them until they're adjacent.

## Contents

- [The trigger](#the-trigger)
- [The invariant (the whole idea)](#the-invariant-the-whole-idea)
- [Template A — smallest value that passes → `return right`](#template-a--smallest-value-that-passes--return-right)
- [Template B — largest value that passes → `return left`](#template-b--largest-value-that-passes--return-left)
- [Worked examples](#worked-examples)
- [Picking the initial `left` and `right` (sentinels)](#picking-the-initial-left-and-right-sentinels)
- [Why this template](#why-this-template)
- [Problems for further practice](#problems-for-further-practice)

## The trigger

> You can phrase the answer as: **"the smallest / largest value `v` for which some condition is true"**, and that condition is **monotonic** — once it flips, it stays flipped.

Monotonic means the predicate looks like one of:

```
v:      … small  →  large …
p(v):   F F F F | T T T T      ← flips false→true once   (want first T)
p(v):   T T T T | F F F F      ← flips true→false once    (want last  T)
```

The boundary is the single flip point. Binary search finds it in `O(log(range))` predicate calls.

## The invariant (the whole idea)

Keep `left` and `right` on **opposite sides** of the flip, and never let them cross:

- one pointer always sits on the **passing** side,
- the other always sits on the **failing** side,
- the loop shrinks the gap until `right == left + 1` — at which point the flip is pinned exactly between them.

`mid` is always strictly between `left` and `right`, so each step you test a fresh middle value and move whichever pointer keeps the invariant true.

The only decision left is **which pointer holds the answer** — and that's the difference between the two templates.

## Template A — smallest value that passes → `return right`

Predicate flips `F … F | T … T`. You want the **first T**. The passing side is on the right, so the answer is `right`.

```java
int left  = /* a value known to FAIL  (just below the range) */;
int right = /* a value known to PASS  (top of the range)     */;

while (right - left > 1) {
    int mid = left + (right - left) / 2;
    if (passes(mid)) right = mid;   // mid passes → answer is mid or smaller
    else             left  = mid;   // mid fails  → answer is bigger
}
return right;                       // smallest passing value
```

Invariant: `passes(left) == false`, `passes(right) == true`, always.

This is exactly your *Kth Smallest in a Sorted Matrix* solution, with `passes(v) = (count(v) >= k)`.

## Template B — largest value that passes → `return left`

Predicate flips `T … T | F … F`. You want the **last T**. The passing side is on the left, so the answer is `left`.

```java
int left  = /* a value known to PASS  (bottom of the range)  */;
int right = /* a value known to FAIL  (just above the range) */;

while (right - left > 1) {
    int mid = left + (right - left) / 2;
    if (passes(mid)) left  = mid;   // mid passes → answer is mid or bigger
    else             right = mid;   // mid fails  → answer is smaller
}
return left;                        // largest passing value
```

Invariant: `passes(left) == true`, `passes(right) == false`, always.

#### The mnemonic

> Return the pointer that sits on the **passing** side. Smallest-passing → passing is on the right → `return right`. Largest-passing → passing is on the left → `return left`.

The two templates are mirror images: swap the `if`/`else` bodies and swap `left`/`right` in the return.

## Worked examples

Same predicate family (`v²` vs 50), one in each direction.

#### Template A — smallest `v` with `v² ≥ 50` (answer 8)

`passes(v) = (v*v >= 50)`. Start `left = 0` (0² = 0, fails ✓), `right = 50` (passes).

| left | right | mid | mid² ≥ 50? | move |
|-----:|------:|----:|:----------:|------|
| 0 | 50 | 25 | 625 ✓ | right = 25 |
| 0 | 25 | 12 | 144 ✓ | right = 12 |
| 0 | 12 | 6  | 36 ✗  | left = 6 |
| 6 | 12 | 9  | 81 ✓  | right = 9 |
| 6 | 9  | 7  | 49 ✗  | left = 7 |
| 7 | 9  | 8  | 64 ✓  | right = 8 |
| 7 | 8  | — | stop | **return right = 8** |

#### Template B — largest `v` with `v² ≤ 50` (answer 7)

`passes(v) = (v*v <= 50)`. Start `left = 0` (0² = 0, passes ✓), `right = 50` (2500, fails).

| left | right | mid | mid² ≤ 50? | move |
|-----:|------:|----:|:----------:|------|
| 0 | 50 | 25 | 625 ✗ | right = 25 |
| 0 | 25 | 12 | 144 ✗ | right = 12 |
| 0 | 12 | 6  | 36 ✓  | left = 6 |
| 6 | 12 | 9  | 81 ✗  | right = 9 |
| 6 | 9  | 7  | 49 ✓  | left = 7 |
| 7 | 9  | 8  | 64 ✗  | right = 8 |
| 7 | 8  | — | stop | **return left = 7** |

Notice both runs trace the *same* mids — only the pointer that moves, and the one returned, differ.

## Picking the initial `left` and `right` (sentinels)

The invariant has to hold **before** the loop starts. Because `mid` is always strictly between the two pointers, the **initial endpoints are never tested** — so you must place them where they're guaranteed correct:

- The "failing" endpoint should be just past the range, so it can never accidentally be the answer.
- For Template A, that's `left = min - 1` (nothing is ≤ `min - 1`, so it always fails a "count ≥ k" style predicate).
- For Template B, that's `right = max + 1`.

Two traps with sentinels:

- **Overflow / underflow.** `min - 1` underflows if `min == Integer.MIN_VALUE`; `max + 1` overflows if `max == Integer.MAX_VALUE`. If the range reaches the type's edge, use `long`, or initialize at the real endpoint and re-check it at the end instead.
- **Always compute `mid = left + (right - left) / 2`**, never `(left + right) / 2` — the latter overflows when both are large.

## Why this template

The gap-of-2 form (`while (right - left > 1)`) sidesteps the classic off-by-one and infinite-loop bugs of the `left <= right` / `left < right` forms:

- It can't loop forever — every iteration strictly shrinks `right - left` (since `mid` is strictly inside).
- There's no "is the answer `mid`, `mid-1`, or `mid+1`?" guesswork at the end — the loop exits with the boundary pinned between two adjacent values, and the template tells you which one to return.

One subtlety worth remembering: the boundary always lands on a value where the predicate *actually flips*. For predicates like "count of elements ≤ v ≥ k", the flip only happens at values that genuinely exist in the data — so the returned value is real, never a phantom gap value. (See [Staircase Search](Staircase_Search.md) for the matrix-count version of this argument.)

## Problems for further practice

- [LeetCode 378 — Kth Smallest Element in a Sorted Matrix](https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/) — Template A with `passes(v) = count(v) ≥ k` (count via [Staircase Search](Staircase_Search.md)).
- [LeetCode 875 — Koko Eating Bananas](https://leetcode.com/problems/koko-eating-bananas/) — Template A: smallest eating speed that finishes in time.
- [LeetCode 1011 — Capacity To Ship Packages Within D Days](https://leetcode.com/problems/capacity-to-ship-packages-within-d-days/) — Template A: smallest capacity that fits the deadline.
- [LeetCode 410 — Split Array Largest Sum](https://leetcode.com/problems/split-array-largest-sum/) — Template A: smallest "largest subarray sum" achievable.
- [LeetCode 668 — Kth Smallest Number in Multiplication Table](https://leetcode.com/problems/kth-smallest-number-in-multiplication-table/) — Template A over an implicit grid.
- [LeetCode 69 — Sqrt(x)](https://leetcode.com/problems/sqrtx/) — Template B: largest `v` with `v² ≤ x` (the worked example above).
