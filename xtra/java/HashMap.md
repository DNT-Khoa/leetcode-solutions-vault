# HashMap and Map in Java

Behaviors of Java's `Map` implementations that catch you off-guard — mostly around null keys, missing keys, and how key equality actually works. `HashMap` is the default; other implementations differ in ways worth knowing.

## Contents

- [Null keys: `HashMap` allows one, others don't](#null-keys-hashmap-allows-one-others-dont)
- [`get(k)` returns null for missing keys — the ambiguity](#getk-returns-null-for-missing-keys--the-ambiguity)
- [Key equality: `hashCode` for bucket, `equals` for match](#key-equality-hashcode-for-bucket-equals-for-match)
- [When identity-based keys are what you want](#when-identity-based-keys-are-what-you-want)
- [Seeding `null → null` as a wiring trick](#seeding-null--null-as-a-wiring-trick)
- [Problems for further practice](#problems-for-further-practice)

## Null keys: `HashMap` allows one, others don't

Not every `Map` accepts a null key. This bites when you swap implementations or use a factory method:

| Map type | `put(null, v)` | `get(null)` |
|---|---|---|
| `HashMap` | allowed (one null key) | returns its value, or `null` if absent |
| `LinkedHashMap` | allowed | same as `HashMap` |
| `TreeMap` | throws `NullPointerException` (uses `compareTo`) | throws `NullPointerException` |
| `ConcurrentHashMap` | throws `NullPointerException` | throws `NullPointerException` |
| `Map.of(...)`, `Map.copyOf(...)` | throws `NullPointerException` | throws `NullPointerException` |

Null *values* follow the same split: `HashMap` accepts them, `ConcurrentHashMap` / `Map.of` reject them.

## `get(k)` returns null for missing keys — the ambiguity

`map.get(k)` returns `null` in two different situations:

1. `k` is not in the map.
2. `k` is in the map, but its value is `null`.

If the map might hold `null` values (or `null` keys, since `HashMap.get(null)` is legal), the pattern `if (map.get(k) == null) …` can't distinguish them:

```java
Map<String, Integer> map = new HashMap<>();
map.put("x", null);

map.get("x");           // null
map.get("y");           // null   ← same result, different meaning
map.containsKey("x");   // true
map.containsKey("y");   // false
```

Use `containsKey` when the distinction matters. For counter/accumulator patterns where "absent" should mean 0, `map.getOrDefault(k, 0)` sidesteps the ambiguity in the direction that matters.

## Key equality: `hashCode` for bucket, `equals` for match

`HashMap` locates a key in two steps:

1. Compute `key.hashCode()` → picks a bucket.
2. Walk that bucket comparing entries with `key.equals(...)`.

Both methods come from `Object`, and the **defaults are identity-based** (compare by reference). So if a class doesn't override them, two logically-equal instances are treated as **different keys**:

```java
class Point { int x, y; /* no equals/hashCode override */ }

Map<Point, String> map = new HashMap<>();
map.put(new Point(1, 2), "a");
map.get(new Point(1, 2));   // null — different instance, different key
```

The full identity-vs-value story (which built-in types override, what to do for custom classes, why `int[]` doesn't work as a key): see [Value-based vs identity-based `equals`](JAVA_NOTES.md#value-based-vs-identity-based-equals) in `JAVA_NOTES.md`.

## When identity-based keys are what you want

For "handle" or "node" types — graph vertices, `ListNode`, LeetCode's `Node` — you specifically want each *instance* to be its own key, even when two of them happen to share the same field values. The default identity behavior gives you exactly this, for free:

```java
Map<Node, Node> originalToClone = new HashMap<>();
originalToClone.put(orig, new Node(orig.val));
```

Two original nodes with `val = 5` still get their own separate clones — which is the whole point when you're copying a graph.

Rule of thumb:
- Object represents an **identity / handle** (a node in a structure) → default identity-based keys are correct.
- Object represents a **value / compound key** (a coordinate, a tuple) → override `equals`/`hashCode`, or use a `record`.

## Seeding `null → null` as a wiring trick

When copying a graph where a pointer field can be null (e.g. `Node.random`), seeding the map with `map.put(null, null)` lets you drop the null-check on lookups:

```java
Map<Node, Node> map = new HashMap<>();
map.put(null, null);
// ...
copy.random = map.get(orig.random);   // works whether orig.random is null or not
```

Without the seed, `map.get(null)` on a `HashMap` still returns `null` (absent-key returns `null`), so the code is *correct* either way. But the explicit seed documents intent — "null maps to null is a design choice, not a coincidence" — and survives refactors that swap in a different `Map` or change what "absent" means for the algorithm.

## Problems for further practice

- [copy-list-with-random-pointer](../../leetcode/copy-list-with-random-pointer/notes.md) — `Map<Node, Node>` where `Node.random` can be null. Combines identity-based node keys (each original instance → its own clone) with the `null → null` seeding trick so `map.get(orig.random)` handles both cases uniformly.
