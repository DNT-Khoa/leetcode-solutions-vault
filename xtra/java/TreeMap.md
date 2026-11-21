# TreeMap and TreeSet in Java

A `TreeMap` is a `Map` that keeps its keys **sorted**. It supports everything a `HashMap` does (`put`, `get`, `merge`, `remove`, `containsKey`), and on top of that answers questions about **order**: what's the smallest key, the largest, the closest key at or below some value, the next key after it. `TreeSet` is the same thing without values — a sorted `Set`.

## Contents

- [`HashMap` vs `TreeMap`](#hashmap-vs-treemap)
- [The basics](#the-basics)
- [Navigation methods](#navigation-methods)
- [Entries, polling, and range views](#entries-polling-and-range-views)
- [Pattern — sorted multiset (value → count)](#pattern--sorted-multiset-value--count)
- [Pattern — nearest key lookup](#pattern--nearest-key-lookup)
- [Custom ordering](#custom-ordering)
- [`TreeSet`](#treeset)
- [Pitfalls](#pitfalls)
- [Cost summary](#cost-summary)
- [Problems for further practice](#problems-for-further-practice)

## `HashMap` vs `TreeMap`

| | `HashMap` | `TreeMap` |
|---|---|---|
| Key order | none (looks random) | **sorted** (ascending by default) |
| `get` / `put` / `remove` | O(1) average | O(log N) |
| "smallest key?" / "next key after x?" | scan everything, O(N) | O(log N) |
| Built on | hash table | balanced binary search tree (red-black tree) |
| `null` key | one allowed | throws `NullPointerException` — see [HashMap.md](HashMap.md#null-keys-hashmap-allows-one-others-dont) |
| Key requirement | `equals` + `hashCode` | `Comparable`, or a `Comparator` |

Rule of thumb: plain lookups → `HashMap`. Anything where the **order of the keys** matters → `TreeMap`.

## The basics

```java
TreeMap<Integer, Integer> map = new TreeMap<>();
map.put(5, 1);
map.put(1, 3);
map.put(8, 2);
map.merge(5, 1, Integer::sum);     // 5 → 2

System.out.println(map);           // {1=3, 5=2, 8=2}  — always sorted by key
for (int key : map.keySet()) { }   // iterates 1, 5, 8 in order
for (var e : map.entrySet()) { }   // entries in key order too
```

Declare the variable as `TreeMap<…>` (or `NavigableMap<…>`), **not** `Map<…>`. The navigation methods below aren't on the `Map` interface, so `Map<Integer, Integer> m = new TreeMap<>()` can't call `firstKey()` or `floorKey(x)`.

## Navigation methods

With `map = {1=3, 5=2, 8=2}`:

| Method | Returns | Example | If none exists |
|---|---|---|---|
| `firstKey()` | smallest key | `1` | throws `NoSuchElementException` |
| `lastKey()` | largest key | `8` | throws `NoSuchElementException` |
| `floorKey(x)` | largest key **≤ x** | `floorKey(6)` → `5` | `null` |
| `ceilingKey(x)` | smallest key **≥ x** | `ceilingKey(6)` → `8` | `null` |
| `lowerKey(x)` | largest key **< x** | `lowerKey(5)` → `1` | `null` |
| `higherKey(x)` | smallest key **> x** | `higherKey(5)` → `8` | `null` |

Memory aid: **floor / ceiling include x** (≤, ≥); **lower / higher exclude x** (<, >).

Each has an `…Entry` twin (`firstEntry()`, `floorEntry(x)`, …) that returns the key **and** value as a `Map.Entry`, or `null` — handy when you need both.

## Entries, polling, and range views

```java
Map.Entry<Integer, Integer> e = map.floorEntry(6);   // 5=2
e.getKey();    // 5
e.getValue();  // 2

map.pollFirstEntry();   // removes and returns the smallest entry (null if empty)
map.pollLastEntry();    // removes and returns the largest entry

map.headMap(5);          // keys <  5   (headMap(5, true) for <= 5)
map.tailMap(5);          // keys >= 5   (tailMap(5, false) for > 5)
map.subMap(2, 8);        // keys in [2, 8)
map.descendingMap();     // same map, iterated largest → smallest
```

The range views are **live views**, not copies: removing from `map.headMap(5)` removes from `map` itself. Creating a view is O(log N); iterating it costs its size.

## Pattern — sorted multiset (value → count)

Java has no built-in multiset (a set that allows duplicates), so a `TreeMap<value, count>` stands in for one. It gives you counts **and** "smallest / largest value still present".

From [846. Hand of Straights](../../hand-of-straights/notes.md):

```java
TreeMap<Integer, Integer> counts = new TreeMap<>();
for (int card : hand) counts.merge(card, 1, Integer::sum);

while (!counts.isEmpty()) {
    int first = counts.firstKey();            // smallest card left starts a group
    for (int c = first; c < first + groupSize; c++) {
        if (!counts.containsKey(c)) return false;
        int count = counts.get(c);
        if (count == 1) counts.remove(c);     // last copy: drop the key
        else counts.put(c, count - 1);
    }
}
```

The one rule that makes it work: **remove a key when its count reaches 0**. A key with count 0 is still a key — `firstKey()` would keep returning a value you no longer have, and `containsKey` would say it's there.

The same structure works for a **sliding window** that needs its min and max: add the entering value (`merge(x, 1, Integer::sum)`), decrement or remove the leaving one, and read `firstKey()` / `lastKey()`.

## Pattern — nearest key lookup

When you store things by a sortable key (a timestamp, a position, a start time) and need "the closest one to x", the navigation methods do the binary search for you:

- "Latest value at or before time `t`" → `floorEntry(t)`.
- "Does a new booking `[s, e)` clash?" → check the booking that starts just before (`floorKey(s)`) and just after (`ceilingKey(s)`).
- "Closest value to `x`" → compare `floorKey(x)` and `ceilingKey(x)`.

Always handle the `null` these return when nothing qualifies.

## Custom ordering

Pass a `Comparator` to change the order — same rules as in [Sorting & Comparators](Sorting_Comparators.md):

```java
TreeMap<Integer, Integer> desc = new TreeMap<>(Comparator.reverseOrder());   // largest key first
TreeMap<String, Integer> byLength = new TreeMap<>(Comparator.comparingInt(String::length)
                                                            .thenComparing(Comparator.naturalOrder()));
```

**The comparator defines key equality.** If it says two keys compare as 0, the map treats them as the **same key** — `equals` is never consulted. A comparator on length alone would make `"ab"` and `"cd"` overwrite each other. That's why the second example adds a tie-breaker.

## `TreeSet`

A sorted `Set` with the same navigation methods, minus the values:

```java
TreeSet<Integer> set = new TreeSet<>(List.of(5, 1, 8));
set.first();        // 1
set.last();         // 8
set.floor(6);       // 5
set.ceiling(6);     // 8
set.lower(5);       // 1
set.higher(5);      // 8
set.pollFirst();    // removes and returns 1
set.headSet(5);  set.tailSet(5);  set.subSet(2, 8);  set.descendingSet();
```

Note the names drop the `Key` suffix: `first()` instead of `firstKey()`, `floor(x)` instead of `floorKey(x)`.

Because it's a set, **duplicates collapse**. If you need repeated values, either use the [multiset pattern](#pattern--sorted-multiset-value--count) or make elements unique (e.g. store `int[]{value, index}` with a comparator that breaks ties on index).

## Pitfalls

- **`firstKey()` / `lastKey()` on an empty map throw** `NoSuchElementException`. Check `isEmpty()` first, or use `firstEntry()` (returns `null`).
- **`floorKey` and friends return `null`.** Assigning the result to an `int` auto-unboxes and throws `NullPointerException`. Store it in an `Integer` and null-check:
  ```java
  Integer prev = map.floorKey(x);
  if (prev != null) { … }
  ```
- **Count 0 is still present.** In a multiset, remove the key instead of storing 0.
- **Declared as `Map`** → navigation methods aren't available. Declare as `TreeMap` or `NavigableMap`.
- **No `null` keys**, because keys are compared with `compareTo`.
- **Don't mutate a key** after inserting it in a way that changes its ordering — the tree won't re-sort, and lookups start failing.
- **Comparing `Integer` values with `==`** only works for small values (−128 to 127, the cached range). `map.get(a) == map.get(b)` can be `false` for equal large counts; use `.equals` or unbox to `int` first.

## Cost summary

`N` = number of keys.

| Operation | `TreeMap` / `TreeSet` | `HashMap` / `HashSet` |
|---|---|---|
| `put` / `get` / `remove` / `containsKey` | O(log N) | O(1) average |
| `firstKey` / `lastKey` / `floorKey` / `ceilingKey` / … | O(log N) | — (O(N) scan) |
| `pollFirstEntry` / `pollLastEntry` | O(log N) | — |
| iterate in sorted order | O(N) | sort first: O(N log N) |
| space | O(N) | O(N) |

## Problems for further practice

Solved in this repo:

- [846. Hand of Straights](../../hand-of-straights/notes.md) — sorted multiset, `firstKey()` picks the smallest card.

Next:

| Problem | Difficulty | What TreeMap / TreeSet does there |
|---|---|---|
| [1296. Divide Array in Sets of K Consecutive Numbers](https://leetcode.com/problems/divide-array-in-sets-of-k-consecutive-numbers/) | Medium | same problem as 846 — good for a quick review |
| [981. Time Based Key-Value Store](https://leetcode.com/problems/time-based-key-value-store/) | Medium | `floorEntry(timestamp)` per key |
| [729. My Calendar I](https://leetcode.com/problems/my-calendar-i/) | Medium | check neighbours with `floorKey` / `ceilingKey` for overlap |
| [2034. Stock Price Fluctuation](https://leetcode.com/problems/stock-price-fluctuation/) | Medium | multiset of prices; `firstKey` / `lastKey` for min / max |
| [1438. Longest Continuous Subarray With Absolute Diff Less Than or Equal to Limit](https://leetcode.com/problems/longest-continuous-subarray-with-absolute-diff-less-than-or-equal-to-limit/) | Medium | sliding-window multiset; `lastKey() - firstKey()` |
| [855. Exam Room](https://leetcode.com/problems/exam-room/) | Medium | `TreeSet` of occupied seats; `lower` / `higher` for the neighbours |
| [2353. Design a Food Rating System](https://leetcode.com/problems/design-a-food-rating-system/) | Medium | `TreeSet` with a custom comparator (rating desc, name asc) |
| [220. Contains Duplicate III](https://leetcode.com/problems/contains-duplicate-iii/) | Hard | sliding-window `TreeSet`; `ceiling(x - t)` |
| [352. Data Stream as Disjoint Intervals](https://leetcode.com/problems/data-stream-as-disjoint-intervals/) | Hard | intervals keyed by start; merge with `floorKey` / `higherKey` |
| [975. Odd Even Jump](https://leetcode.com/problems/odd-even-jump/) | Hard | `ceilingKey` / `floorKey` while scanning right to left |

Related docs: [HashMap and Map](HashMap.md), [Sorting & Comparators](Sorting_Comparators.md), [Intervals](../dsa/Intervals.md) (My Calendar, Range Module use `TreeMap`).
