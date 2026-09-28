# Selenium Condition Annotations

Declarative `ExpectedCondition` contracts attached as field-level annotations to the `By`
statements of a Classic Page Object, with compile-time validation.

This is a corrected implementation of an earlier `@Condition(expected="...") + @Attributes(...)`
design. Every change below traces to a specific defect in that design.

---

## What changed, and why

| # | Original | Corrected | Reason |
|---|----------|-----------|--------|
| 1 | `String expected()` | `ExpectedConditionType value()` | `ExpectedConditions` is heavily overloaded; a bare method name cannot select a target. Also restores compile-time checking, autocomplete and rename safety. |
| 2 | `selected() default true`, `number() default 0`, `regex() default "[A-Za-z0-9]"` | `Tri.UNSET`, `Integer.MIN_VALUE`, `""` sentinel | Each original default was a *legal argument value*, so "not supplied" was indistinguishable from "supplied as default". The regex default silently matched almost anything — a false green. |
| 3 | `property` doubled as `frameLocator`; `number` doubled as `frameLocator` | `frameNameOrId()`, `frameIndex()` as their own members | One member serving two unrelated Selenium parameters is unvalidatable. |
| 4 | Every member optional on every condition | `ExpectedConditionType.requiredMembers()` + annotation processor | Arity and applicability are now compile errors, not runtime surprises. |
| 5 | `value()` meant "expected attribute value" | Renamed `expectedValue()`; `value()` left undeclared | `value()` is the shorthand slot; claiming it for a domain meaning invites misreading. |
| 6 | `@Condition` and `@Attributes` independent | `@Attributes` is `@Target({})`, reachable only via `Condition.with()` | An orphan `@Attributes` no longer compiles. |
| 7 | Not repeatable | `@Repeatable(Conditions.class)` + `Combine` | Expresses `and` / `or`. Depth 1 only — JLS 9.6.1 forbids self-containing annotation types. |
| 8 | No wait configuration | `timeoutMillis`, `pollingMillis`, `ignoring`, `message` | The declaration now carries its own envelope. |
| 9 | `by()` a bare `String` | `childUsing()` (`Using` enum) + `childValue()` | A raw string cannot express a locator strategy. |
| 10 | `@Target(TYPE)` undefined | TYPE = page load contract, FIELD = locator contract, METHOD = post-condition | Each target now has enforced semantics. |
| 11 | `package annonations` | `io.seleniumstudio.conditions` | Typo, on public API surface. |
| 12 | Unused `ExpectedCondition` import | Removed | Dead import. |

## Layout

```
io.seleniumstudio.conditions            Selenium-free metadata + annotations
  Sentinels, Tri, Using, Combine, LocatorUse, Yields, Member
  ExpectedConditionType                 37-constant catalogue with required-member metadata
  Attributes, Condition, Conditions
  spi.ConditionFactory                  open extension point
io.seleniumstudio.conditions.build      the only Selenium-dependent construction code
  Attr, ConditionBuilders
io.seleniumstudio.conditions.runtime
  ConditionResolver, WaitExecutor, PageScanner, PageContract, AnnotatedPage
io.seleniumstudio.conditions.processor
  ConditionProcessor                    JSR 269 compile-time validation
```

The metadata enum carries **no Selenium dependency**, which is what lets the processor run without
Selenium on the processor path — and makes a later split into `-api` / `-runtime` modules trivial.

## Coverage

37 catalogue constants covering every `ExpectedConditions` entry point that is expressible from a
locator, plus four modifiers (`not`, `refreshed`, `and`, `or`) expressed as
`negate()`, `refreshed()` and `Conditions.combine()`.

**Deliberately not expressible.** Overloads anchored to a `WebElement` that cannot be derived from a
locator, and any condition parameterised by *runtime* data. Annotation members must be compile-time
constants (JLS 9.6.1), so this is a language limit, not an implementation gap:

```java
// Impossible: the expected value comes from a data provider.
@Condition(value = TEXT_TO_BE_PRESENT_IN_ELEMENT, with = @Attributes(text = tradeIdFromDataProvider))
```

Do **not** solve this with property placeholders resolved from a thread-local. That reintroduces
runtime failure, defeats the processor, and couples the page object to execution state. Use
`AnnotatedPage.awaitDynamic(...)`, which shares the same `WaitExecutor`, so the declarative and
imperative paths cannot drift.

| Parameterisation | Mechanism |
|---|---|
| Static (visibility, clickability, fixed attribute, fixed count) | annotation |
| Runtime-valued (expected text, computed counts, dynamic URLs) | `awaitDynamic(...)` |
| Structurally complex boolean logic | `CUSTOM` + `ConditionFactory` |

## Checks performed at compile time

1. Every required `@Attributes` member is set to a non-sentinel value.
2. No member outside the condition's required set is set.
3. `LocatorUse.REQUIRED` only on `By`-typed fields; `FORBIDDEN` only on types and methods.
4. `regex()` compiles as a `Pattern`.
5. `value() == CUSTOM` **iff** `factory()` is set.
6. `timeoutMillis` / `pollingMillis` are `-1` or strictly positive.
7. `childUsing()` and `childValue()` are set together.

Adding a catalogue constant extends coverage of checks 1–2 with no processor change.

## Build and run (Windows 11)

Requires JDK 17+, Maven 3.9+, and Chrome. Selenium Manager resolves the driver binary.

```powershell
mvn -q clean install -DskipTests      # pass 1: proc=none, installs the processor jar
mvn test                              # pass 2: proc=full, then runs the suite
```

The two-pass shape is required because the processor cannot be run against its own sources.

To watch the processor reject a bad declaration, add this to any page object and recompile:

```java
// error: TITLE_IS is document-scoped (LocatorUse.FORBIDDEN) and must not be declared
//        on the locator field BAD.
@Condition(value = ExpectedConditionType.TITLE_IS, with = @Attributes(title = "x"))
public static final By BAD = By.id("heading");

// error: ATTRIBUTE_TO_BE requires @Attributes members [expectedValue()] to be set.
@Condition(value = ExpectedConditionType.ATTRIBUTE_TO_BE, with = @Attributes(attribute = "id"))
public static final By ALSO_BAD = By.id("heading");
```

## Known limitation worth confirming on first build

`@Repeatable` combined with a nested annotation member (`Condition.with()`) is legal under
JLS 9.6.3 / 9.7.5, but IDE annotation-processing plugins vary in how they surface the container
form. Confirm your IDE renders `@Conditions` diagnostics correctly before standardising on the
repeatable style.

## Note on self-healing locators

Annotations are compile-time-fixed metadata, and `static final By` fields cannot be reassigned
reflectively on modern JDKs (`Field.modifiers` filtering since JDK 12, strong encapsulation since
JDK 17). A registry-backed healing layer therefore belongs behind the field reference, not in it —
keep the annotation as declared intent and let a `ManagedLocator` wrapper hold the mutable resolved
`By`. `PageScanner` reads the field value once, so swapping `By` for such a wrapper is a change to
`PageScanner` and `AnnotatedPage` alone.
