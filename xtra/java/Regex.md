# Regular Expressions in Java

What you need to know about regex to solve LeetCode string problems in Java: the API, the syntax that actually comes up, how `split` and `replaceAll` use regex behind your back, how to reason about complexity, and the mistakes that are easy to make. Every output shown below was checked by running it.

## Contents

- [When to use regex on LeetCode](#when-to-use-regex-on-leetcode)
- [The Java API](#the-java-api)
- [Escaping: two layers](#escaping-two-layers)
- [Character classes](#character-classes)
- [Quantifiers](#quantifiers)
- [Groups and alternation](#groups-and-alternation)
- [Anchors and boundaries](#anchors-and-boundaries)
- [Flags](#flags)
- [Lookahead](#lookahead)
- [split gotchas](#split-gotchas)
- [Replacing text](#replacing-text)
- [Building patterns from pieces](#building-patterns-from-pieces)
- [Complexity](#complexity)
- [Bug checklist](#bug-checklist)
- [Problems for further practice](#problems-for-further-practice)

## When to use regex on LeetCode

Regex is a good fit when the problem is about the **shape** of a string:

- **Validation:** is this an IP address, a number, an email? Use one pattern with `matches()`.
- **Tokenizing:** split a sentence into words while ignoring punctuation and repeated spaces, using `split` with a character class.
- **Extracting:** pull out every number in a string, using `find()` in a loop.
- **Rewriting:** reformat, mask or normalize text, using `replaceAll` with groups.

Regex is a poor fit when:

- The answer needs **counting or nesting**, like balanced parentheses or nested tags. Regular expressions can't count, so use a stack.
- You must **implement** matching yourself. 10. Regular Expression Matching and 44. Wildcard Matching ask you to build the engine, usually with DP, so you can't call `Pattern`.
- An interviewer wants to see the **manual parsing logic**. Know both ways. Regex is great for showing you understand the format, and a character-by-character loop shows you can handle every case yourself.

## The Java API

```java
import java.util.regex.Matcher;
import java.util.regex.Pattern;

Pattern p = Pattern.compile("\\d+");   // compile once and reuse
Matcher m = p.matcher("a12b345c6");
```

| Call | Question it answers |
|---|---|
| `m.matches()` | Does the **whole** string match? |
| `m.lookingAt()` | Does a **prefix** match (starting at index 0)? |
| `m.find()` | Is there a match **anywhere**? Each call moves on to the next match. |
| `m.group()` / `m.group(1)` | Text of the last match, or of group 1 inside it |
| `m.start()` / `m.end()` | Where the last match begins and ends (end is exclusive) |

```java
Pattern d = Pattern.compile("\\d+");
d.matcher("12ab").lookingAt();   // true   (the prefix "12" matches)
d.matcher("12ab").matches();     // false  (the whole string isn't digits)
```

**Collecting every match:**

```java
Matcher m = Pattern.compile("\\d+").matcher("a12b345c6");
List<String> nums = new ArrayList<>();
while (m.find()) nums.add(m.group());          // [12, 345, 6]

// Java 9+: the same thing as a stream
Pattern.compile("\\d+").matcher("a12b345").results()
       .map(MatchResult::group).toList();       // [12, 345]
```

**Shortcuts on `String`.** Each of these compiles the regex again **on every call**:

```java
s.matches(regex)              // same as Pattern.matches(regex, s), whole-string match
s.split(regex)
s.replaceAll(regex, repl)
s.replaceFirst(regex, repl)
```

They're fine for one-off calls. Inside a loop, or in a method LeetCode calls thousands of times, store a `private static final Pattern` and reuse it.

`String.replace(a, b)` is **not** regex. It replaces literal text, which is often what you actually want.

## Escaping: two layers

A regex in Java goes through **two** parsers:

1. **Java's string literal** turns `\\` into a single `\`.
2. **The regex engine** then reads `\d`, `\.`, and so on.

| You want the regex to see | Write in Java |
|---|---|
| `\d` (a digit) | `"\\d"` |
| `\.` (a literal dot) | `"\\."` |
| `\\` (a literal backslash) | `"\\\\"` |

**Characters with special meaning** outside `[...]`, which need a `\` to match literally:

```
.  *  +  ?  ^  $  |  (  )  [  ]  {  }  \
```

These are **not** special and need no escaping: `:` `-` (outside a class) `,` `@` `#` `/` `=` space.

To match a string literally without thinking about escaping, use `Pattern.quote(s)`. It wraps `s` as `\Q...\E`, so `"a.b".split(Pattern.quote("."))` splits on the dot.

## Character classes

A `[...]` matches **exactly one character**.

| Syntax | Matches one... |
|---|---|
| `[abc]` | `a`, `b` or `c` |
| `[a-z]` | lowercase letter |
| `[0-9a-fA-F]` | hex digit |
| `[^0-9]` | character that is **not** a digit (`^` first means "not") |
| `.` | any character except a line break |

**Shorthands:**

| Shorthand | Same as | Opposite |
|---|---|---|
| `\d` | `[0-9]` | `\D` |
| `\w` | `[a-zA-Z0-9_]`, including the underscore | `\W` |
| `\s` | whitespace: space, tab, newline, ... | `\S` |

`\w` is ASCII-only by default: `"é_a1".replaceAll("\\w", "#")` gives `"é###"`.

**Inside `[...]`, most special characters lose their meaning.** `[.]` is a literal dot, and `[+*?]` is a literal plus, star or question mark. The characters that still matter inside a class:

- `]` ends the class
- `\` escapes
- `^`, but only as the first character
- `-` between two characters makes a range. Put it first or last to mean a literal dash: `[-x]`.

**Each bracket is one character.** `[0][5]` is two separate one-character classes, so it matches only the text `"05"`. It does **not** mean "0 to 5". For that, write `[0-5]`.

## Quantifiers

A quantifier repeats **the single item right before it**: one character, one class, or one group.

| Syntax | Meaning |
|---|---|
| `x?` | 0 or 1 |
| `x*` | 0 or more |
| `x+` | 1 or more |
| `x{3}` | exactly 3 |
| `x{1,4}` | 1 to 4 |
| `x{2,}` | 2 or more |

```
ab{3}    →  "abbb"     ({3} applies only to b)
(ab){3}  →  "ababab"   ({3} applies to the group)
\.{3}    →  "..."      (three dots in a row, not three dot-separated parts)
```

**No spaces inside braces.** `{1, 4}` is a syntax error in Java: *"Unclosed counted closure"*.

**Greedy vs. lazy.** By default, quantifiers take as much as they can and give characters back only if the rest of the pattern needs them. Adding `?` makes them take as little as possible:

```java
"<a><b>"  with  <.+>    →  "<a><b>"   (greedy: runs to the last >)
"<a><b>"  with  <.+?>   →  "<a>"      (lazy: stops at the first >)
```

## Groups and alternation

**`|` has the lowest precedence of everything.** It splits the whole pattern, or the whole group it sits in, into options:

```
cat|dogs     →  "cat"  or  "dogs"
(cat|dog)s   →  "cats" or  "dogs"
```

This matters most when you **paste a sub-pattern containing `|` into a bigger one**. Without parentheses, the `|` breaks the outer pattern apart:

```
part = 0|1[0-9]
part + "\\." + part     →   0|1[0-9]\.0|1[0-9]       (three unrelated options)
"(" + part + ")\\." + "(" + part + ")"   →   (0|1[0-9])\.(0|1[0-9])   (what you meant)
```

A safe habit: **always put the parentheses inside the sub-pattern string itself**, so it's safe wherever you use it.

**Kinds of groups:**

| Syntax | Groups? | Captures? |
|---|---|---|
| `(...)` | yes | yes, numbered from 1 by its opening `(` |
| `(?:...)` | yes | no. Use this when you only need grouping. |
| `(?<name>...)` | yes | yes, read it with `m.group("name")` |

```java
Matcher m = Pattern.compile("(?<user>\\w+)@(?<host>\\w+)").matcher("bob@mail");
m.matches();
m.group("user");   // "bob"
m.group("host");   // "mail"
```

**Backreference:** `\1` inside the pattern means "the same text that group 1 matched". `(\w)\1` finds a doubled character, as in `"xaab"`.

## Anchors and boundaries

Anchors match a **position**, not a character.

| Anchor | Position |
|---|---|
| `^` | start of the input |
| `$` | end of the input, **or just before a final line break** |
| `\z` | true end of the input |
| `\b` | word boundary, between `\w` and `\W` |

**`matches()` vs `find()` with anchors:**

- `matches()` already requires the whole string to match, so you don't need `^` or `$`.
- `find()` accepts a match anywhere, so `"abc1.2.3.4xyz"` contains a match for an IP pattern. Adding `^...$` limits it to the whole string, **almost**:

```java
Pattern.compile("\\d+").matcher("1\n").matches();      // false
Pattern.compile("^\\d+$").matcher("1\n").find();       // true!  $ matches before the final \n
```

**For validation, use `matches()`.** If you have to use `find()`, end the pattern with `\z`, not `$`.

**Word boundaries** let you replace whole words only:

```java
"cat concat".replaceAll("\\bcat\\b", "dog");   // "dog concat"
```

## Flags

| Inline | Constant | Effect |
|---|---|---|
| `(?i)` | `Pattern.CASE_INSENSITIVE` | ignore case |
| `(?m)` | `Pattern.MULTILINE` | `^` and `$` also match at each line |
| `(?s)` | `Pattern.DOTALL` | `.` also matches line breaks |

```java
"HeLLo".matches("(?i)hello");                          // true
Pattern.compile("[a-f0-9]{1,4}", Pattern.CASE_INSENSITIVE);
```

## Lookahead

`(?=X)` checks that `X` matches **from the current position**, without using up any characters. `(?!X)` checks that it doesn't. They let you require several things at once:

```
(?=.*\d)(?=.*[a-z]).{8,}     →   at least 8 characters, containing a digit and a lowercase letter
```

They come up in password-style rules. For most LeetCode problems you won't need them, but you should be able to read them.

## split gotchas

`split` takes a **regex**, not plain text. That gives you some surprises:

| Call | Result | Why |
|---|---|---|
| `"a.b.c".split(".")` | `[]` (empty array) | `.` matches every character, so every piece is empty and all of them are dropped |
| `"a\|b".split("\|")` | `[a, \|, b]` | an empty pattern matches between every pair of characters |
| `"a\|b".split("\\\|")` | `[a, b]` | escaped, so it's a literal pipe |

**Empty strings at the start and end:**

```java
",a,,b,,".split(",")       // ["", "a", "", "b"]          leading empty strings kept, trailing ones dropped
",a,,b,,".split(",", -1)   // ["", "a", "", "b", "", ""]  limit -1 keeps everything
"a,b,c".split(",", 2)      // ["a", "b,c"]                limit n gives at most n parts
"".split(",")              // [""]   length 1, not 0!
```

**Whitespace:**

```java
"  hello world  ".split(" ")           // ["", "", "hello", "world"]
"  hello world  ".split("\\s+")        // ["", "hello", "world"]   one leading "" remains
"  hello world  ".trim().split("\\s+") // ["hello", "world"]
```

**For validation, use limit `-1`.** Without it, `"1.2.3.4."` loses its trailing empty part and looks like 4 valid parts.

## Replacing text

```java
"2026-11-07".replaceAll("(\\d+)-(\\d+)-(\\d+)", "$3/$2/$1");   // "07/11/2026"
```

- In the **replacement** string, `$1` means group 1 and `\` escapes. A literal `$` must be written `\\$`, or use `Matcher.quoteReplacement("$5")`.
- `replaceFirst` replaces only the first match.
- `String.replace("a.b", "x")` treats both arguments as plain text, with no escaping needed.

## Building patterns from pieces

Write the smallest piece first, test it, then put it together:

```java
private static final String PART = "(?:...)";                        // one segment, grouped
private static final Pattern WHOLE = Pattern.compile("(?:" + PART + SEP + "){k}" + PART);
```

The **"k separated parts"** pattern: repeat `(part separator)` k−1 times, then add the last part on its own. This gives exactly k parts with no separator at the start or end.

**Matching a number range** like 0–255: a regex can't compare numbers, so split the range by its digits. Ask:

1. How many digits can it have? Write one option per length.
2. For the longest length, how does the first digit limit the next ones? Write one option per case, so that **ranges never overlap and leave no gaps**.
3. Are leading zeros allowed? If not, a multi-digit option must not start with `0`.

**Test the piece completely.** For a number-range piece, loop over every number from 0 to 9999 (with and without leading zeros) and compare the result with `Integer.parseInt` and a range check. It runs in a fraction of a second and finds every gap.

## Complexity

**Compiling** costs time proportional to the pattern length. That's a constant for a fixed pattern, but `String.matches` and `split` compile again on every call.

**Matching:**

| Situation | Cost |
|---|---|
| `split`, `replaceAll` or a `find()` loop over the whole input | O(N): every character is read |
| `matches()` with a **bounded** pattern (only `{n}` and `{n,m}`, no `*`, `+` or `{n,}`) | O(1): the pattern can use at most a fixed number of characters, so matching stops after that. Example: an IPv6 match is at most 39 characters. |
| `matches()` with an unbounded pattern like `\d+` | O(N) |
| `find()` with no match | O(N) start positions × the cost of each attempt |

**Space:** `split` creates new strings, so O(N). `matches()` and `find()` keep only a small, fixed amount of state, so O(1) for these simple patterns.

**Catastrophic backtracking.** Nested quantifiers on the same characters, like `(a+)+b` or `(\w*)*x`, can take **exponential** time on input that almost matches, such as a long run of `a` with no `b`. The engine tries every way of splitting the run between the inner and outer quantifier. Avoid repeating something that is itself repeated, unless a separator between the copies forces only one way to split the input. For example, `(\d+\.){3}` is safe because each `\.` marks where a copy ends.

## Bug checklist

Mistakes from real attempts, roughly in the order they happen:

- [ ] **Space in a quantifier:** `{1, 4}` → `{1,4}`
- [ ] **One class per digit used as a range:** `[0][5]` → `[0-5]`
- [ ] **Each digit limited independently:** `[1-2][0-5][0-5]` rejects 106, 199, 249. Split the cases by the leading digit.
- [ ] **Overlapping options:** `2[0-5][0-9]` lets 256–259 through. Check that every option covers only its own range.
- [ ] **`|` without a group** when pasting a sub-pattern. Wrap it in parentheses.
- [ ] **Quantifier on the wrong item:** `\.{3}` is three dots in a row. Group the part with its separator: `(part\.){3}`.
- [ ] **Wrong repeat count:** `{0,3}` allows 1 to 4 parts. For exactly 4 parts, use `{3}` plus the final part.
- [ ] **Separator copied from another pattern:** an IPv6 pattern that still has IPv4's `\.` instead of `:`.
- [ ] **`find()` where you meant `matches()`**, or `$` letting a trailing `\n` through.
- [ ] **`split(".")` or `split("|")`** without escaping.
- [ ] **`split` dropping trailing empty parts** when you needed to see them. Use limit `-1`.
- [ ] **Compiling inside a hot path.** Use a `static final Pattern`.

## Problems for further practice

Already solved here:

- [validate-ip-address](../../validate-ip-address/notes.md): whole-string validation with `matches()`, a 0–255 range piece, and "k separated parts" with `{k-1}`. Compare it with the earlier `split(..., -1)` version to see the O(N) → O(1) change.
- [most-common-word](../../most-common-word/notes.md): tokenizing with a negated character class and `+`.
- [reverse-words-in-a-string](../../reverse-words-in-a-string/notes.md): `trim()` plus `\s+` to get rid of uneven whitespace.
- [reorder-data-in-log-files](../../reorder-data-in-log-files/notes.md): `split` with a limit, to separate the first token from everything after it.

**Easy: split, replace and character classes**

- 58. Length of Last Word: what `split` does with trailing spaces.
- 434. Number of Segments in a String: the `"".split(...)` and leading-space edge cases.
- 1108. Defanging an IP Address: literal replacement and escaping `.`. Try it both with and without regex.
- 1678. Goal Parser Interpretation: replacing in order, with special characters in the pattern.
- 1805. Number of Different Integers in a String: tokenizing by what is *not* allowed, plus leading zeros.
- 1816. Truncate Sentence: `split` with a limit, or `indexOf`.
- 2042. Check if Numbers Are Ascending in a Sentence: telling numbers apart from words.
- 2129. Capitalize the Title: splitting words and rebuilding them.

**Medium: validation and extraction**

- 8. String to Integer (atoi): a prefix match with `lookingAt()`. Mind the optional sign and the leading whitespace.
- 290. Word Pattern: `split` plus mapping each word to a pattern letter.
- 609. Find Duplicate File in System: parsing `name(content)` tokens with capture groups.
- 831. Masking Personal Information: telling two formats apart, then rewriting with groups.
- 929. Unique Email Addresses: cutting the string at the first `@`, `+` and `.` (all three need care in regex).
- 1023. Camelcase Matching: **building** a pattern from input. Consider what can go between the required letters.
- 1410. HTML Entity Parser: replacing several tokens. Watch the replacement order.
- 1694. Reformat Phone Number: removing every non-digit, then grouping the rest.
- 2047. Number of Valid Words in a Sentence: a rule-heavy token pattern with optional parts.
- 2299. Strong Password Checker II: several "contains at least one" rules, a good place to practise lookahead.

**Hard: full grammars and engines**

- 65. Valid Number: the classic regex validation problem, with optional sign, decimal point and exponent. Build it piece by piece and test every piece.
- 591. Tag Validator: regex for the tokens, a stack for the nesting. A good example of where regex stops being enough.
- 10. Regular Expression Matching: implement `.` and `*` yourself with DP.
- 44. Wildcard Matching: the same idea with `?` and `*`.

**Regex in other languages on LeetCode**

- 193. Valid Phone Numbers (Bash): `grep -E` with anchors and alternation.
- 192. Word Frequency (Bash): tokenizing in a shell pipeline.
- 1517. Find Users With Valid E-Mails (SQL): `REGEXP`. Watch the escaping and case sensitivity.
- 1527. Patients With a Condition (SQL): matching at the start of a word, not just anywhere.
