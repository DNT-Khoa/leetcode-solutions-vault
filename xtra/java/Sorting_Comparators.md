# Sorting and Comparators in Java

How to sort collections and arrays in Java, with custom orderings via `Comparator`. Focused on the forms that actually come up in CP.

## Contents

- [Sort entry points](#sort-entry-points)
- [Natural ordering: `Comparable`](#natural-ordering-comparable)
- [Custom ordering: `Comparator`](#custom-ordering-comparator)
- [Comparator factories: `comparing` / `comparingInt`](#comparator-factories-comparing--comparingint)
- [Chaining: `reversed` and `thenComparing`](#chaining-reversed-and-thencomparing)
- [The `a - b` overflow trap](#the-a---b-overflow-trap)
- [`int[]` has no `Comparator` overload](#int-has-no-comparator-overload)
- [Problems for further practice](#problems-for-further-practice)

## Sort entry points

| API | Sorts | Notes |
|---|---|---|
| `Arrays.sort(arr)` | primitive arrays (`int[]`, `long[]`, …) and `Object[]` | natural order only for primitives; accepts a `Comparator` only for `Object[]` |
| `Collections.sort(list)` | any `List<T>` | older API; usually use `list.sort(...)` instead |
| `list.sort(cmp)` | any `List<T>` | preferred — instance method on `List`, accepts a `Comparator` |
| `stream.sorted(cmp)` | returns a sorted stream | use only when already in a stream pipeline |

All four are stable: equal elements keep their original relative order.

## Natural ordering: `Comparable`

If a type implements `Comparable<T>`, sorting it without a `Comparator` uses its built-in `compareTo`. Numeric wrappers (`Integer`, `Long`, …) and `String` already implement it.

```java
int[] arr = {3, 1, 2};
Arrays.sort(arr);                 // [1, 2, 3] — natural order on int

List<String> names = new ArrayList<>(List.of("banana", "apple"));
names.sort(null);                 // ["apple", "banana"] — natural order on String
```

`compareTo` returns an `int` following the **sign contract**: negative if `this < other`, zero if equal, positive if `this > other`. Every comparator and `compareTo` method must follow it.

## Custom ordering: `Comparator`

A `Comparator<T>` is a function `(T, T) -> int` following the same sign contract. The lambda form:

```java
cars.sort((a, b) -> Integer.compare(a.position(), b.position()));
```

Read it as: "return negative if `a` should come before `b`, zero if equal, positive if `b` should come before `a`."

## Comparator factories: `comparing` / `comparingInt`

Writing the full lambda gets repetitive. `Comparator.comparing(keyExtractor)` builds the comparator from a key-extractor function — usually a method reference like `Car::position`:

```java
cars.sort(Comparator.comparing(Car::position));
```

For primitive keys, prefer the typed variants — they avoid boxing the key into `Integer` / `Long` / `Double` on every comparison:

| Key type | Use |
|---|---|
| `int` | `Comparator.comparingInt(...)` |
| `long` | `Comparator.comparingLong(...)` |
| `double` | `Comparator.comparingDouble(...)` |
| any `Comparable` | `Comparator.comparing(...)` |

```java
cars.sort(Comparator.comparingInt(Car::position));    // no Integer boxing
```

For raw values (not extracted from an object), `Comparator.naturalOrder()` and `Comparator.reverseOrder()` cover the common cases:

```java
nums.sort(Comparator.reverseOrder());                 // descending
```

## Chaining: `reversed` and `thenComparing`

Compose comparators for multi-key sort.

- `.reversed()` flips the direction.
- `.thenComparing(...)` is the tiebreaker — only runs when the previous comparator returned zero.

```java
// Sort by score descending, then by name ascending (tiebreaker)
players.sort(
    Comparator.comparingInt(Player::score).reversed()
              .thenComparing(Player::name)
);
```

Order matters — the first comparator wins, chained ones only break ties.

## The `a - b` overflow trap

A tempting one-liner:

```java
list.sort((a, b) -> a - b);     // WRONG — silently breaks on large values
```

If `a = 2_000_000_000` and `b = -2_000_000_000`, then `a - b = 4_000_000_000`, which overflows `int` and wraps to a *negative* number — the sort thinks `a < b`.

Always use the static `compare`:

```java
list.sort((a, b) -> Integer.compare(a, b));    // safe
list.sort(Comparator.naturalOrder());          // even shorter
```

Same applies to `Long.compare` and `Double.compare`. The factories (`comparingInt` etc.) use the safe form internally, so they're not affected.

## `int[]` has no `Comparator` overload

`Arrays.sort(int[])` sorts ascending only. There's no `Arrays.sort(int[], Comparator)` — primitive arrays can't satisfy a generic `Comparator<T>`. So to sort an `int[]` in a custom order:

**Option A — box to `Integer[]`:**
```java
Integer[] arr = {3, 1, 2};
Arrays.sort(arr, Comparator.reverseOrder());    // [3, 2, 1]
```

**Option B — sort indices into the array** (avoids boxing the values themselves; useful when you need to preserve the original positions):
```java
int[] values = {30, 10, 20};
Integer[] idx = new Integer[values.length];
for (int i = 0; i < idx.length; i++) idx[i] = i;
Arrays.sort(idx, (a, b) -> Integer.compare(values[a], values[b]));
// idx = [1, 2, 0]  → values in sorted order: values[1]=10, values[2]=20, values[0]=30
```

**Option C — sort then reverse manually** (only when "descending an `int[]`" is all you need):
```java
int[] arr = {3, 1, 2};
Arrays.sort(arr);
for (int i = 0, j = arr.length - 1; i < j; i++, j--) {
    int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
}
```

## Problems for further practice

- [car-fleet](../../leetcode/car-fleet/notes.md) — sort cars by position descending with `Comparator.comparingInt(Car::position).reversed()`, then walk the sorted list to count fleets.
