# Tree Construction from Traversals

Reconstructing a binary tree from two of its traversals (typically preorder + inorder, or postorder + inorder). The recipe is always the same three steps — pick a root, split into left/right using inorder, recurse — and each variant just changes *which* traversal is the "root finder."

## Contents

- [The trigger](#the-trigger)
- [The core insight — root finder + splitter](#the-core-insight--root-finder--splitter)
- [What each traversal gives you](#what-each-traversal-gives-you)
- [Universal template](#universal-template)
- [Variant 1 — Preorder + Inorder (LC 105)](#variant-1--preorder--inorder-lc-105)
- [Variant 2 — Postorder + Inorder (LC 106)](#variant-2--postorder--inorder-lc-106)
- [Variant 3 — Preorder + Postorder (LC 889)](#variant-3--preorder--postorder-lc-889)
- [Worked trace](#worked-trace)
- [Empty subtrees (missing children)](#empty-subtrees-missing-children)
- [Problems for further practice](#problems-for-further-practice)

## The trigger

> You're given two arrays representing the same binary tree via different traversals, and asked to reconstruct the tree. Node values are unique.

Uniqueness matters — with duplicates, the "find the root in inorder" step is ambiguous.

## The core insight — root finder + splitter

Every solution boils down to three steps at each recursive call:

1. **Pick the root** of the current subtree.
2. **Split** the current subtree's nodes into left and right subtrees.
3. **Recurse** on each side.

One traversal identifies the root; the other splits it. **Inorder is always the splitter** — given a root, everything to its left in the current inorder slice is the left subtree, everything to its right is the right subtree. Preorder and postorder are the root-finders.

## What each traversal gives you

| Traversal | Order | Role |
|---|---|---|
| **Preorder** | root → left → right | Root-finder — first unvisited element is the root |
| **Postorder** | left → right → root | Root-finder — last unvisited element is the root |
| **Inorder** | left → root → right | Splitter — position of root divides slice into left/right subtrees |

**Efficiency:** the "find root in inorder" step is O(n) if you linearly scan each call. Build a `HashMap<value, index>` once up front → each lookup is O(1) → total O(n) instead of O(n²).

## Universal template

The recursion carries **inorder bounds** `(inLeft, inRight)` to know which slice it owns. A separate cursor (`preIdx` or `postIdx`) marches through the root-finder array to always point at the next root.

```java
class Solution {
    private Map<Integer, Integer> inorderIndex;
    // Add cursor + root-finder array as fields, e.g.:
    // private int[] preorder;
    // private int preIdx;

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        inorderIndex = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            inorderIndex.put(inorder[i], i);
        }
        // stash root-finder array + initialize cursor here
        return build(0, inorder.length - 1);
    }

    private TreeNode build(int inLeft, int inRight) {
        if (inLeft > inRight) return null;                       // empty subtree

        int rootVal = /* pull from cursor — variant-specific */;
        TreeNode root = new TreeNode(rootVal);

        int mid = inorderIndex.get(rootVal);

        // Recurse — ORDER MATTERS, see variants
        root.left  = build(inLeft, mid - 1);
        root.right = build(mid + 1, inRight);

        return root;
    }
}
```

**Why the inorder bounds are essential:** the cursor alone tells you *what* the next root's value is, but not *where* it belongs in the tree structure. The bounds are what let the two arrays "talk" — the root-finder array gives values, inorder gives structure and knows when a subtree is empty.

## Variant 1 — Preorder + Inorder (LC 105)

- Cursor: `int preIdx = 0;` (advances forward)
- Root: `int rootVal = preorder[preIdx++];`
- Recurse order: **left first, then right** (preorder is root → left → right, so the next value after the root belongs to the left subtree)

```java
root.left  = build(inLeft, mid - 1);
root.right = build(mid + 1, inRight);
```

## Variant 2 — Postorder + Inorder (LC 106)

- Cursor: `int postIdx = postorder.length - 1;` (advances backward)
- Root: `int rootVal = postorder[postIdx--];`
- Recurse order: **right first, then left** (postorder consumed from the back gives root, then right subtree, then left subtree)

```java
root.right = build(mid + 1, inRight);
root.left  = build(inLeft, mid - 1);
```

## Variant 3 — Preorder + Postorder (LC 889)

Non-unique reconstruction — when a node has only one child, you can't tell if it's a left child or a right child. LC 889 accepts any valid answer.

- Root: `preorder[0]` (also `postorder[last]`).
- `preorder[1]` is the root of the left subtree (by convention). Find it in postorder → its position tells you where the left subtree ends and the right subtree begins.

Recurse on preorder+postorder index ranges rather than inorder bounds.

## Worked trace

Take LC 105 Example:

```
preorder = [10, 5, 3, 7, 15, 12, 20]
inorder  = [3, 5, 7, 10, 12, 15, 20]

        10
       /  \
      5    15
     / \   / \
    3   7 12  20
```

Recursion (indentation = stack depth, `pi` = `preIdx`):

```
build(0, 6)                  root=10, mid=3      pi: 0→1
├── build(0, 2)              root=5,  mid=1      pi: 1→2
│   ├── build(0, 0)          root=3,  mid=0      pi: 2→3
│   │   ├── build(0, -1) → null
│   │   └── build(1, 0)  → null
│   └── build(2, 2)          root=7,  mid=2      pi: 3→4
│       ├── build(2, 1) → null
│       └── build(3, 2) → null
└── build(4, 6)              root=15, mid=5      pi: 4→5
    ├── build(4, 4)          root=12, mid=4      pi: 5→6
    │   ├── build(4, 3) → null
    │   └── build(5, 4) → null
    └── build(6, 6)          root=20, mid=6      pi: 6→7
        ├── build(6, 5) → null
        └── build(7, 6) → null
```

`preIdx` walks preorder in order: 10, 5, 3, 7, 15, 12, 20 — exactly the preorder sequence. The left subtree of any node is fully built (draining `preIdx`) *before* the right subtree begins — that's why the recurse-order rule matters.

## Empty subtrees (missing children)

A subtree is empty ⟺ its inorder range is empty (`inLeft > inRight`). This happens exactly when the parent sits at the edge of its own inorder slice:

- Parent at the **left edge** of its slice (`mid == inLeft`) → left child missing.
- Parent at the **right edge** of its slice (`mid == inRight`) → right child missing.
- Parent strictly inside → both children exist.

Concrete example — zigzag tree:

```
preorder = [1, 2, 3, 4, 5]
inorder  = [2, 4, 3, 5, 1]

        1
       /
      2         ← 1 has no right child
       \
        3       ← 2 has no left child
       / \
      4   5
```

- Node `1` sits at the right edge of the full inorder → its right child is missing → the recursion `build(mid+1, inRight)` becomes `build(5, 4)`, which returns null immediately without consuming any more preorder values.
- Node `2` sits at the left edge of its inorder slice `[0..3]` → its left child is missing → `build(0, -1)` returns null.

## Problems for further practice

- [LeetCode 105 — Construct Binary Tree from Preorder and Inorder](https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/) — Variant 1.
- [LeetCode 106 — Construct Binary Tree from Inorder and Postorder](https://leetcode.com/problems/construct-binary-tree-from-inorder-and-postorder-traversal/) — Variant 2.
- [LeetCode 889 — Construct Binary Tree from Preorder and Postorder](https://leetcode.com/problems/construct-binary-tree-from-preorder-and-postorder-traversal/) — Variant 3 (non-unique).
- [LeetCode 1008 — Construct Binary Search Tree from Preorder Traversal](https://leetcode.com/problems/construct-binary-search-tree-from-preorder-traversal/) — BST case: preorder *alone* suffices because the BST ordering property gives you a natural split.
