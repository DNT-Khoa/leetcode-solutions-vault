# Backtracking Cheatsheet

Personal reference for backtracking problems: templates, duplicate handling, and complexity analysis.

---

## 1. The template

```java
void explore(int[] nums, ..., List<Integer> selected, int startIdx) {
    // 1. base case: record a valid state
    if (isValid(...)) {
        result.add(new ArrayList<>(selected));   // COPY — O(k) where k = |selected|
        // return here if there's nothing deeper to explore
    }

    // 2. prune if we've overshot
    if (isPruned(...)) return;

    // 3. try each next choice
    for (int i = startIdx; i < nums.length; i++) {
        // (optional) skip duplicates at this level
        if (i > startIdx && nums[i] == nums[i - 1]) continue;

        selected.add(nums[i]);           // select
        explore(nums, ..., selected, nextStart(i));   // recurse
        selected.remove(selected.size() - 1);         // backtrack
    }
}
```

Two parameters that vary between problems:

| What you're building        | `nextStart(i)` | Notes                                |
|-----------------------------|----------------|--------------------------------------|
| Subsets / combinations      | `i + 1`        | each element used at most once       |
| Combination sum (reusable)  | `i`            | element can be picked again          |
| Permutations                | `0` + `visited[]` | order matters, need used-tracking |

---

## 2. Avoiding duplicates

**When:** the input `candidates` contains repeated values (e.g. `[1, 1, 2, 5]`), and you don't want the same combination twice in the output.

**Two-part fix:**

1. **Sort the input first** — puts equal values next to each other.
2. **Skip a value equal to the previous one at the same recursion level:**
   ```java
   if (i > startIdx && candidates[i] == candidates[i - 1]) continue;
   ```

### Why `i > startIdx`, not `i > 0`?

Skip only when the duplicate is a **sibling** in the current loop — not when it was picked one level up. Using `i > 0` would wrongly skip legitimate combinations like `[1, 1, 6]`.

**Walkthrough** with `candidates = [1, 1, 2, 5, 6]`, `target = 8`:

At the root (`start = 0`), loop over `i = 0..4`:

- `i = 0`: pick `candidates[0] = 1` → recurse with `start = 1`
  - At depth 1 (`start = 1`), `i = 1`: pick second `1`.
  - Check: `i > start`? `1 > 1` is **false** → don't skip.
  - ✓ Correctly builds `[1, 1, 6]`.
- `i = 1` at root: try picking second `1`.
  - Check: `i > start`? `1 > 0` is **true**, and `candidates[1] == candidates[0]` → **skip**.
  - ✓ Prevents duplicate `[1, 2, 5]` (which would otherwise be built once via each `1`).

**Rule of thumb:** duplicates are OK **vertically** (deeper in the tree), banned **horizontally** (as sibling choices).

---

## 3. Time complexity

### The recursion tree

Every backtracking algorithm's time is dominated by the **number of nodes in its recursion tree**, multiplied by the **cost per node** (usually O(1) except at leaves where you copy `selected`).

### Case A: unique elements, each picked at most once (Subsets, Combination Sum II)

Every element has a binary fate: **in or out**. So the tree has depth `N` and each level roughly doubles.

```
                        start
                       /     \
                    in a    out a          ← N=1: 2 leaves
                    / \      / \
                 in b out b in b out b     ← N=2: 4 leaves
                 ...
```

- **Total leaves:** `2^N`
- **Copy at each leaf:** up to `O(N)`
- **Time:** `O(N · 2^N)`

### Case B: reusable elements (Combination Sum)

Depth is bounded not by `N` but by `T / M`, where:
- `T` = target
- `M` = smallest candidate value

(Each pick adds at least `M` to the running sum, so you can pick at most `T/M` times before overshooting.)

At each level, up to `N` branches. Levels: 0 through `h = T/M`, so `h + 1` levels total.

**Geometric series:**
```
1 + N + N² + ... + N^h  =  (N^(h+1) − 1) / (N − 1)  ≈  N^(h+1)
```

- **Total nodes:** `O(N^(T/M + 1))`
- **Copy at each leaf:** up to `O(T/M)`
- **Time (usually written):** `O(N^(T/M + 1))` — the copy factor gets absorbed into the exponent.

### Case C: permutations (Permutations, Permutations II)

Every arrangement of `N` distinct items is distinct — order matters — so there are `N!` leaves.

Nodes at level `k` = ordered pick of `k` from `N` = `N! / (N-k)!`.

**Total nodes** across all levels:
```
Σ N!/(N-k)!  for k = 0..N
  = N! · (1/0! + 1/1! + 1/2! + ... + 1/N!)
  ≈ e · N!  (≈ 2.72 · N!)
```

The inner sum is the partial Taylor series for `e`. The bottom two levels dominate: both level `N-1` and level `N` have exactly `N!` nodes, and everything above them adds only a smaller constant.

- **Total nodes:** `e · N!` = `O(N!)`
- **Work per node:** `O(N)` (loop over N choices, or O(N) copy at leaf)
- **Time:** `O(N · N!)` — the constant `e` is absorbed by Big-O.

### Why the `+1` in the time exponent?

The tree of height `h` has `h+1` **levels** (level 0 is the root). Total nodes ≈ last level × N = `N^h · N = N^(h+1)`.

The `+1` sits inside an exponent, so it's a **factor of N**, not a small additive:
```
N^(h+1) = N · N^h
```
For `N = 10`, that's a 10× difference — you cannot drop it.

---

## 4. Space complexity

Two things to consider:

| Kind                   | What                                                   | Size                    |
|------------------------|--------------------------------------------------------|-------------------------|
| **Auxiliary space**    | recursion stack + `selected` list                      | `O(max depth)`          |
| **Output space**       | all combinations stored in `result`                    | up to time-complexity   |

By convention, when people say "space O(N)" for subsets, they mean **auxiliary**. If your interviewer asks about the full total including output, it's the same as the time bound (`O(N · 2^N)` etc.).

### Why space drops the `+1` but time doesn't

- **Space** is `O(h + 1)` frames. The `+1` is *added*, so `O(h + 1) = O(h)` — additive constants get dropped in Big-O.
- **Time** is `O(N^(h+1))`. The `+1` is in the *exponent*, so it's a multiplicative factor of `N` — must be kept.

| Quantity            | With +1        | Without +1     | Ratio        |
|---------------------|----------------|----------------|--------------|
| Space (h=30)        | 31             | 30             | 1.03× (drop) |
| Time (N=10, h=30)   | `10^31`        | `10^30`        | 10× (keep)   |

---

## 5. The O(N) copy cost

Every backtracking leaf usually does:

```java
result.add(new ArrayList<>(selected));   // copies up to N elements
```

This copy is unavoidable if the output must be a list of independent lists. You can dodge it by:

1. **Yielding lazily** (iterator/generator) — caller pays only for what they use.
2. **Returning bitmasks** — each subset represented as an int; O(1) per subset, decoded on demand.
3. **Persistent linked structure** — subsets share prefixes; O(1) per subset, O(N) to read.

Total output size is `Θ(N · 2^N)`, so you cannot beat that when returning materialized lists.

---

## 6. Concrete tree: Combination Sum II

`candidates = [1, 2, 4]`, `target = 7`, `N = 3` → expect `2^3 = 8` nodes.

```
                        [] sum=0
              ┌────────────┼────────────┐
             i=0          i=1          i=2
              │            │            │
           [1] s=1      [2] s=2      [4] s=4
           ┌────┴────┐     │
          i=1      i=2    i=2
           │        │      │
       [1,2] s=3 [1,4] s=5 [2,4] s=6
           │
          i=2
           │
       [1,2,4] s=7 ✓
```

Nodes: `[], [1], [1,2], [1,2,4], [1,4], [2], [2,4], [4]` = **8 = 2^N**.

The `start = i + 1` in the recursive call ensures each subset is visited in **exactly one** canonical order (ascending index), so no duplicates from permutation.

---

## 7. Concrete tree: Combination Sum (reusable)

`candidates = [2, 3]`, `target = 6`, so `N = 2`, `T/M = 3`.

```
                    []  sum=0
                   /        \
             pick 2           pick 3
              /                  \
          [2] sum=2             [3] sum=3
          /      \                  |
      pick 2    pick 3           pick 3
        /          \                |
    [2,2] sum=4   [2,3] sum=5    [3,3] sum=6  ✓
      /   \           |
   pick 2 pick 3   pick 3
     /      \         |
 [2,2,2]  [2,2,3]  [2,3,3]
  sum=6   sum=7    sum=8
   ✓       ✗        ✗
```

Total calls: **9**. Upper bound: `N^(T/M + 1) = 2^4 = 16`. ✓ (The `startIdx` pruning tightens it.)

**Sanity check on the `T/M` intuition:**

- `candidates = [3, 5]`, `target = 6`: `M = 3`, so `T/M = 2` → shallow tree.
- `candidates = [1, 2, 3]`, `target = 30`: `M = 1` → `T/M = 30` → `3^31` nodes. Catastrophic.

**Takeaway:** a candidate of `1` blows up the recursion. That's why LC problem constraints often exclude `M = 1` or cap the target.

---

## 8. Concrete tree: Permutations

`nums = [1, 2, 3]`, `N = 3` → expect `3! = 6` leaves and `≈ e · 3! ≈ 16` total nodes.

```
                          []
              ┌───────────┼───────────┐
             1            2            3
          ┌──┴──┐      ┌──┴──┐      ┌──┴──┐
         2     3      1     3      1     2
         │     │      │     │      │     │
         3     2      3     1      2     1
       [1,2,3][1,3,2][2,1,3][2,3,1][3,1,2][3,2,1]
```

Level counts: 1 + 3 + 6 + 6 = **16 nodes** = `2.67 · 3!` ≈ `e · N!` ✓ (converges to `e ≈ 2.72` as N grows).

- Loop uses `for (int i = 0; i < N; i++)` — starts at `0` every time (no `startIdx`).
- Skips already-used values with `if (used[i]) continue;`.
- The bottom two levels (each `N!` nodes) dominate — every level above them contributes only a small constant of extra nodes.

---

## 9. Quick checklist when solving a new backtracking problem

- [ ] Is the goal a subset, combination, permutation, or partition?
- [ ] Can elements be reused? (`i` vs `i + 1` in the recursive call)
- [ ] Are there duplicates in the input? If so:
  - [ ] Sort the input.
  - [ ] Add `if (i > startIdx && nums[i] == nums[i-1]) continue;`
- [ ] What's the pruning condition? (`sum > target`, length limit, etc.)
- [ ] Copy `selected` when you add to the result — never store the reference.
- [ ] Time: reason about the tree — depth × branching, plus O(k) copy at leaves.
- [ ] Space: recursion depth + `selected` (auxiliary), plus output if you're counting it.
