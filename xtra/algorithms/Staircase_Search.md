# Staircase Search (Saddleback Search)

Searching a grid that is sorted along **both** rows and columns by walking a corner-to-corner staircase. One comparison eliminates a whole row or whole column, so an O(rows × cols) scan becomes O(rows + cols).

## Contents

- [The trigger](#the-trigger)
- [The idea](#the-idea)
- [Cost](#cost)
- [Why start at a corner of the *informative* diagonal](#why-start-at-a-corner-of-the-informative-diagonal)
- [Pattern 1 — does a target exist?](#pattern-1--does-a-target-exist)
- [Pattern 2 — count elements ≤ v](#pattern-2--count-elements--v)
- [Problems for further practice](#problems-for-further-practice)

## The trigger

> **Grid sorted by both row and column** → think staircase search from a corner.

That recognition is the whole skill. Once you spot it, the code is ~10 lines.

This is the same *family* as binary search: exploit sortedness so one comparison rules out many candidates. Binary search throws away half a 1D array per step; staircase search throws away a whole row or column per step.

## The idea

Stand on **one** cell, start at the **bottom-left** corner `(n-1, 0)`, and take one step at a time until you fall off the top or off the right side.

The bottom-left cell is special: it's the **max of its column** but the **min of its row**. That asymmetry is what makes each comparison eliminate an entire line:

- `matrix[r][c] ≤ v` → it's the biggest in its column, so **everything above is also ≤ v**. Column finished → step **RIGHT**.
- `matrix[r][c] > v`  → it's the smallest in its row, so **everything to the right is also > v**. Row finished → step **UP**.

Every step retires a row or a column, so you never backtrack.

## Cost

| | Time | Space |
|---|------|-------|
| One staircase walk (n×m grid) | **O(n + m)** | O(1) |

Why O(n + m): each step moves the walker either right (`c++`) or up (`r--`), never back. `c` rises from 0 to at most `m`, `r` falls from `n-1` to at most `-1` — so at most `n + m` steps total, independent of the values. For a square grid that's **O(n)**.

When wrapped in **binary search on the answer** (Pattern 2), you call the O(n) walk once per binary-search step over the value range `[min, max]`, giving **O(n · log(max − min))** total — the value range, not n², drives the log factor.

## Why start at a corner of the *informative* diagonal

The two diagonals of the grid are not equal:

- **top-left ↔ bottom-right** — the *boring* diagonal. Both ends are extremes of the whole grid (global min and global max). A comparison there only tells you about that one cell.
- **top-right ↔ bottom-left** — the *informative* diagonal. Each end is an extreme in one dimension and the opposite extreme in the other. That's exactly what lets one comparison settle a whole row or column.

So start at **bottom-left** or **top-right** (they're mirror images — pick one). Starting top-left or bottom-right forces the walker to change direction, which breaks the one-direction guarantee.

## Pattern 1 — does a target exist?

Walk the staircase; if the current cell equals the target, found it.

```java
static boolean search(int[][] m, int target) {
    int n = m.length, cols = m[0].length;
    int r = n - 1, c = 0;          // bottom-left
    while (r >= 0 && c < cols) {
        if (m[r][c] == target) return true;
        if (m[r][c] > target) r--; // row done, go up
        else                  c++; // column done, go right
    }
    return false;
}
```

## Pattern 2 — count elements ≤ v

Same walk, but instead of stopping we **tally** whole columns. When `matrix[r][c] ≤ v`, the cells in this column from row 0 down to row `r` all count — that's `r + 1` cells.

```java
static int countLessEqual(int[][] m, int v) {
    int n = m.length, cols = m[0].length;
    int r = n - 1, c = 0, count = 0;
    while (r >= 0 && c < cols) {
        if (m[r][c] <= v) {        // whole column above me is ≤ v
            count += r + 1;
            c++;                   // never look at this column again
        } else {
            r--;                   // row from here is all > v
        }
    }
    return count;
}
```

This `count(v)` is the workhorse inside **[binary search on the answer](Binary_Search_Boundary.md)** (e.g. *Kth Smallest Element in a Sorted Matrix*): binary search the value range and use `count(mid) ≥ k` as the predicate, since `count` is monotonic in `v`.

#### Concrete example — `count(8)`

```
 1  3  5  9
 2  4  6 10
 7  8 11 13
12 14 15 16
```

Start at `(3,0)` = 12:

| at | value | vs 8 | action | count |
|----|------:|-----:|--------|------:|
| (3,0) | 12 | > 8 | up | 0 |
| (2,0) | 7  | ≤ 8 | +3, right | 3 |
| (2,1) | 8  | ≤ 8 | +3, right | 6 |
| (2,2) | 11 | > 8 | up | 6 |
| (1,2) | 6  | ≤ 8 | +2, right | 8 |
| (1,3) | 10 | > 8 | up | 8 |
| (0,3) | 9  | > 8 | up (off top) | 8 |

Result **8** — the path zig-zags bottom-left → top-right, never going left or down. Cells ≤ 8 are `{1, 2, 3, 4, 5, 6, 7, 8}` = 8. ✓

## Problems for further practice

- [LeetCode 240 — Search a 2D Matrix II](https://leetcode.com/problems/search-a-2d-matrix-ii/) — the plain target-search walk (Pattern 1).
- [LeetCode 378 — Kth Smallest Element in a Sorted Matrix](https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/) — binary search on the answer with `count(v)` (Pattern 2).
- [LeetCode 668 — Kth Smallest Number in Multiplication Table](https://leetcode.com/problems/kth-smallest-number-in-multiplication-table/) — same binary-search-on-answer idea; the grid is implicit (`i*j`) so you count per row instead of walking.
- [LeetCode 74 — Search a 2D Matrix](https://leetcode.com/problems/search-a-2d-matrix/) — fully sorted variant (can also be done as one flat binary search).
