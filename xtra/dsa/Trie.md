# Trie (Prefix Tree)

A **trie** stores a set of strings as a tree of characters. Every path from the root spells a prefix, and strings that share a prefix share the same nodes. That makes "does any word start with ___?" an O(length of prefix) question, independent of how many words are stored.

## Contents

- [What a trie looks like](#what-a-trie-looks-like)
- [When to reach for it](#when-to-reach-for-it)
- [Node representation](#node-representation)
- [Core template: insert / search / startsWith](#core-template-insert--search--startswith)
- [Pattern 1 — Wildcard search (DFS over children)](#pattern-1--wildcard-search-dfs-over-children)
- [Pattern 2 — Trie-guided grid DFS](#pattern-2--trie-guided-grid-dfs)
- [Pattern 3 — Shortest / longest prefix match](#pattern-3--shortest--longest-prefix-match)
- [Pattern 4 — Counts stored on nodes](#pattern-4--counts-stored-on-nodes)
- [Pattern 5 — Autocomplete / suggestions](#pattern-5--autocomplete--suggestions)
- [Pattern 6 — Reversed trie (suffixes, streams)](#pattern-6--reversed-trie-suffixes-streams)
- [Pattern 7 — Binary trie for XOR](#pattern-7--binary-trie-for-xor)
- [Cost summary](#cost-summary)
- [Pitfalls checklist](#pitfalls-checklist)
- [Problems for further practice](#problems-for-further-practice)

## What a trie looks like

Words: `app`, `apple`, `apt`, `bat`

```
          (root)
          /    \
         a      b
         |      |
         p      a
        / \     |
       p*  t*   t*
       |
       l
       |
       e*
```

`*` marks a node where a word ends. Note that `app` ends at an **internal** node — a word can be a prefix of another word, so "word ends here" must be a flag (or stored string), never inferred from "node has no children".

## When to reach for it

Signals in the problem statement:

1. **Many strings, many prefix queries** — "starts with", "autocomplete", "suggestions", "shortest root".
2. **Searching for many words at once** in some structure (a grid, a text stream). Put the *words* in a trie and walk the structure once, instead of searching once per word.
3. **Wildcards** like `.` that match any single character.
4. **Maximize XOR** between numbers — a trie over the bits.

If you only need exact-match lookups, a `HashSet<String>` is simpler. The trie earns its keep when **prefixes** matter.

## Node representation

Two common choices:

```java
// Map: any character set, only allocates children that exist
static class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    String word;          // non-null where a word ends (or: boolean isLeaf)
}

// Array: lowercase a–z only, faster (no hashing, no boxing)
static class TrieNode {
    TrieNode[] children = new TrieNode[26];   // index = c - 'a'
    String word;
}
```

| | `Map<Character, TrieNode>` | `TrieNode[26]` |
|---|---|---|
| Alphabet | anything | fixed and small |
| Lookup | hash + autobox `char` | direct index |
| Memory per node | proportional to real children | always 26 references |
| Iterating children | only existing ones | must skip `null`s; visits in **alphabetical order** for free |

**`boolean isLeaf` vs `String word`.** Storing the whole word at the end node saves you from rebuilding the string with a `StringBuilder` during DFS — handy whenever you need to *report* the word (Word Search II, autocomplete). `word != null` then doubles as the end-of-word flag.

**Why not a `record`?** Record fields are final and `equals`/`hashCode` are generated from all fields. A trie node gets its end-of-word marker set *after* creation, and a generated `hashCode` would walk the entire subtree. Use a plain (static nested) class.

## Core template: insert / search / startsWith

From [Implement Trie (Prefix Tree)](../../implement-trie-prefix-tree/notes.md):

```java
void insert(String word) {
    TrieNode curr = root;
    for (char c : word.toCharArray()) {
        curr = curr.children.computeIfAbsent(c, k -> new TrieNode());
    }
    curr.isLeaf = true;
}

// Shared walk: returns the node at the end of `s`, or null if the path breaks
TrieNode walk(String s) {
    TrieNode curr = root;
    for (char c : s.toCharArray()) {
        curr = curr.children.get(c);
        if (curr == null) return null;
    }
    return curr;
}

boolean search(String word)       { TrieNode n = walk(word);   return n != null && n.isLeaf; }
boolean startsWith(String prefix) { return walk(prefix) != null; }
```

The only difference between `search` and `startsWith` is whether the final node must be a word end.

`computeIfAbsent(c, k -> new TrieNode())` replaces the `containsKey` / `put` / `get` triple.

## Pattern 1 — Wildcard search (DFS over children)

From [Design Add and Search Words Data Structure](../../design-add-and-search-words-data-structure/notes.md).

A normal character follows exactly one child. A `.` must try **every** child, so search becomes a DFS that carries the index into the query:

```java
boolean dfs(String word, int i, TrieNode node) {
    if (i == word.length()) return node.isLeaf;
    char c = word.charAt(i);
    if (c == '.') {
        for (TrieNode child : node.children.values()) {
            if (dfs(word, i + 1, child)) return true;   // short-circuit on first match
        }
        return false;
    }
    TrieNode next = node.children.get(c);
    return next != null && dfs(word, i + 1, next);
}
```

Cost: O(m) with no wildcards; worst case O(26^d · m) where `d` is the number of `.` in the query.

## Pattern 2 — Trie-guided grid DFS

From [Word Search II](../../word-search-ii/notes.md). Find every word from a list that can be traced on a letter grid.

**Build the trie from the words, not from the board.** The board has exponentially many paths (`3^(R·C)` from each cell), while the words have a small, fixed total size `W`. Put the small side in the trie and use it to steer the walk over the big side.

Walk the grid and the trie **in lockstep**: step into a neighbour only if its letter is a child of the current trie node. The trie's depth (longest word, `L`) caps the path length, and any letter that doesn't continue a word cuts the branch immediately.

```java
void dfs(TrieNode node, int r, int c, boolean[][] visited) {
    TrieNode next = node.children.get(board[r][c]);
    if (next == null) return;                     // no word continues this way

    if (next.word != null) {
        result.add(next.word);
        next.word = null;                         // dedupe: report each word once
        // no `return` here — a longer word may continue (e.g. "app" → "apple")
    }

    for (int[] d : DIRECTIONS) {
        int nr = r + d[0], nc = c + d[1];
        if (nr < 0 || nr == ROWS || nc < 0 || nc == COLS || visited[nr][nc]) continue;
        visited[nr][nc] = true;
        dfs(next, nr, nc, visited);
        visited[nr][nc] = false;
    }

    // optional pruning: a node with no word and no children can never match again
    if (next.children.isEmpty() && next.word == null) node.children.remove(board[r][c]);
}
```

The three bugs this problem invites:

| Bug | Symptom | Fix |
|---|---|---|
| `return` right after recording a word | `["app", "apple"]` only finds `app` | keep exploring after a match |
| Same word reachable by two paths | duplicates in the result | null out `word` (or flag it) once reported |
| Start cell not marked visited | `"aba"` found on board `["ab"]` | mark the start cell before the first DFS call |

Cost: Time O(R·C · 3^L + W), Space O(R·C + W). With `L ≤ 10` that is at most `3^10 ≈ 59k` paths per start cell, versus `3^144` for a board-built trie on a 12×12 grid.

Pruning (removing dead leaves) doesn't change the worst-case bound, but it stops later start cells from re-walking branches whose words were already found — it matters on LeetCode's large tests.

## Pattern 3 — Shortest / longest prefix match

Walk the query through the trie and stop at the **first** (or remember the **last**) node where a word ends.

- Shortest dictionary root that prefixes a word → return at the first word-end node.
- Longest stored prefix of a query → keep updating "last word end seen" until the path breaks.

One walk per query, O(m), no matter how many dictionary words there are.

## Pattern 4 — Counts stored on nodes

Add an `int count` to each node and increment it on every node `insert` passes through. Then `count` at a node = number of inserted words having that prefix.

Uses:
- "How many words start with ___?" in O(m).
- Sum over all prefixes of a word of how many words share that prefix — insert everything, then walk each word summing `count`.
- Supporting `erase` — decrement counts along the path instead of deleting nodes.

Variant: store a value (`sum`) instead of a count when each word carries a weight.

## Pattern 5 — Autocomplete / suggestions

Walk to the node for the typed prefix, then collect words in its subtree.

- **DFS from that node in alphabetical order** (an array-based trie gives this for free), stopping after `k` results.
- Or **cache the top-k words on every node** during insert — query becomes O(prefix length), at the cost of extra memory.
- When the user types one more character, you don't restart from the root: just step from the previous node to its child.

## Pattern 6 — Reversed trie (suffixes, streams)

A trie answers prefix questions. For **suffix** questions, insert every word **reversed**.

- A stream of characters arrives and you must report whether the last few characters form a word → keep the recent characters, walk them backwards through a reversed-word trie.
- Matching both a prefix and a suffix at once → a common trick is inserting combined keys such as `suffix + '#' + word` so one prefix walk handles both.

## Pattern 7 — Binary trie for XOR

Insert numbers bit by bit from the **most significant** bit down (a trie with only children `0` and `1`, depth 31 for non-negative `int`s).

To maximize `x XOR y` against stored numbers, walk greedily: at each bit, prefer the child with the **opposite** bit of `x` (that makes this result bit 1, and a high bit beats all lower bits combined); fall back to the same bit only if the opposite child is missing.

Cost: O(32) per insert and per query, so O(32 · N) overall instead of O(N²) for all pairs.

Variant with an extra constraint on `y` (e.g. `y ≤ limit`): sort the queries and numbers, and insert numbers into the trie only once they satisfy the current query's limit (offline processing).

## Cost summary

`m` = length of the word or prefix, `W` = total characters inserted, `Σ` = alphabet size.

| Operation | Time | Notes |
|---|---|---|
| `insert` | O(m) | creates at most `m` nodes |
| `search` / `startsWith` | O(m) | independent of how many words are stored |
| wildcard search | O(Σ^d · m) worst | `d` = number of wildcards |
| build from all words | O(W) | |
| space | O(W) nodes | array version: O(W · Σ) references |

## Pitfalls checklist

- [ ] Mark the word end on the **last** node (`isLeaf = true` / `word = word`), not on the root or the node before.
- [ ] A word can end at an internal node — never use "no children" to mean "end of word".
- [ ] After matching a word during DFS, **keep going** unless the problem says otherwise.
- [ ] Results that must be unique → dedupe at the node (null out `word`) instead of using a `Set` afterwards.
- [ ] Array-based trie: index with `c - 'a'`, and check the constraints really are lowercase only.
- [ ] Building the trie from the side that is exponential (board paths, all substrings) is usually the wrong choice.
- [ ] Recursion depth = trie depth (longest word), so it's safe for typical constraints.

## Problems for further practice

Solved in this repo:

- [208. Implement Trie (Prefix Tree)](../../implement-trie-prefix-tree/notes.md) — core template.
- [211. Design Add and Search Words Data Structure](../../design-add-and-search-words-data-structure/notes.md) — Pattern 1.
- [212. Word Search II](../../word-search-ii/notes.md) — Pattern 2.

Next, roughly in order of difficulty:

| Problem | Difficulty | Pattern |
|---|---|---|
| [14. Longest Common Prefix](https://leetcode.com/problems/longest-common-prefix/) | Easy | walk while the node has exactly one child and isn't a word end |
| [648. Replace Words](https://leetcode.com/problems/replace-words/) | Medium | Pattern 3 — shortest root |
| [720. Longest Word in Dictionary](https://leetcode.com/problems/longest-word-in-dictionary/) | Medium | DFS only through nodes that are word ends |
| [677. Map Sum Pairs](https://leetcode.com/problems/map-sum-pairs/) | Medium | Pattern 4 — sums on nodes |
| [1268. Search Suggestions System](https://leetcode.com/problems/search-suggestions-system/) | Medium | Pattern 5 — autocomplete |
| [421. Maximum XOR of Two Numbers in an Array](https://leetcode.com/problems/maximum-xor-of-two-numbers-in-an-array/) | Medium | Pattern 7 — binary trie |
| [139. Word Break](https://leetcode.com/problems/word-break/) | Medium | DP; a trie speeds up the inner "which words start here" loop |
| [2416. Sum of Prefix Scores of Strings](https://leetcode.com/problems/sum-of-prefix-scores-of-strings/) | Hard | Pattern 4 — counts on nodes |
| [1032. Stream of Characters](https://leetcode.com/problems/stream-of-characters/) | Hard | Pattern 6 — reversed trie |
| [745. Prefix and Suffix Search](https://leetcode.com/problems/prefix-and-suffix-search/) | Hard | Pattern 6 — combined keys |
| [472. Concatenated Words](https://leetcode.com/problems/concatenated-words/) | Hard | trie + DFS/DP over split points |
| [1707. Maximum XOR With an Element From Array](https://leetcode.com/problems/maximum-xor-with-an-element-from-array/) | Hard | Pattern 7 + offline sorted queries |
| [336. Palindrome Pairs](https://leetcode.com/problems/palindrome-pairs/) | Hard | reversed trie + palindrome checks |
