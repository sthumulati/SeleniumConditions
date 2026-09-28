package io.seleniumstudio.conditions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Closed catalogue of every {@code ExpectedConditions} entry point that is expressible
 * declaratively, together with the metadata needed to validate a declaration.
 *
 * <p>This type deliberately carries <em>no</em> Selenium dependency. Construction lives in
 * {@code io.seleniumstudio.conditions.build.ConditionBuilders}. The separation is what allows the
 * annotation processor to consume this metadata without Selenium on the processor path, and it
 * gives reporting and locator-registry tooling a single machine-readable description of every
 * wait declared in a suite.</p>
 *
 * <p><strong>Not expressible, by design:</strong> every {@code WebElement}-anchored overload whose
 * element cannot be derived from a locator at await time. Those are routed to the imperative API.
 * The {@code WebElement} overloads that <em>can</em> be derived from a locator
 * ({@code attributeToBeNotEmpty}, {@code domAttributeToBe}, {@code domPropertyToBe},
 * {@code stalenessOf}) are present here and resolve the element at await time.</p>
 *
 * <p>{@code not}, {@code refreshed}, {@code and} and {@code or} are modifiers rather than
 * conditions, and are expressed by {@code Condition.negate()}, {@code Condition.refreshed()} and
 * {@code Conditions.combine()} respectively.</p>
 */
public enum ExpectedConditionType {

    // ---------- Document scope (no locator) ----------
    TITLE_IS                        (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.TITLE),
    TITLE_CONTAINS                  (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.TITLE),
    URL_TO_BE                       (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.URL),
    URL_CONTAINS                    (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.FRACTION),
    URL_MATCHES                     (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.REGEX),
    ALERT_IS_PRESENT                (LocatorUse.FORBIDDEN, Yields.ALERT),
    NUMBER_OF_WINDOWS_TO_BE         (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.NUMBER),
    JAVASCRIPT_THROWS_NO_EXCEPTIONS (LocatorUse.FORBIDDEN, Yields.BOOLEAN, Member.JAVASCRIPT),
    JS_RETURNS_VALUE                (LocatorUse.FORBIDDEN, Yields.OBJECT,  Member.JAVASCRIPT),

    // ---------- Frame switching ----------
    FRAME_AVAILABLE_BY_NAME_OR_ID   (LocatorUse.FORBIDDEN, Yields.DRIVER,  Member.FRAME_NAME_OR_ID),
    FRAME_AVAILABLE_BY_INDEX        (LocatorUse.FORBIDDEN, Yields.DRIVER,  Member.FRAME_INDEX),
    FRAME_AVAILABLE_BY_LOCATOR      (LocatorUse.REQUIRED,  Yields.DRIVER),

    // ---------- Presence and visibility ----------
    PRESENCE_OF_ELEMENT_LOCATED     (LocatorUse.REQUIRED,  Yields.ELEMENT),
    PRESENCE_OF_ALL_ELEMENTS        (LocatorUse.REQUIRED,  Yields.ELEMENTS),
    VISIBILITY_OF_ELEMENT_LOCATED   (LocatorUse.REQUIRED,  Yields.ELEMENT),
    VISIBILITY_OF_ALL_ELEMENTS      (LocatorUse.REQUIRED,  Yields.ELEMENTS),
    INVISIBILITY_OF_ELEMENT_LOCATED (LocatorUse.REQUIRED,  Yields.BOOLEAN),
    INVISIBILITY_OF_ELEMENT_WITH_TEXT(LocatorUse.REQUIRED, Yields.BOOLEAN, Member.TEXT),

    // ---------- Interaction and selection state ----------
    ELEMENT_TO_BE_CLICKABLE         (LocatorUse.REQUIRED,  Yields.ELEMENT),
    ELEMENT_TO_BE_SELECTED          (LocatorUse.REQUIRED,  Yields.BOOLEAN),
    ELEMENT_SELECTION_STATE_TO_BE   (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.SELECTED),

    // ---------- Text ----------
    TEXT_TO_BE_PRESENT_IN_ELEMENT   (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.TEXT),
    TEXT_TO_BE_PRESENT_IN_VALUE     (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.TEXT),
    TEXT_MATCHES                    (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.REGEX),

    // ---------- Attributes and DOM properties ----------
    ATTRIBUTE_TO_BE                 (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.ATTRIBUTE, Member.EXPECTED_VALUE),
    ATTRIBUTE_CONTAINS              (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.ATTRIBUTE, Member.EXPECTED_VALUE),
    ATTRIBUTE_TO_BE_NOT_EMPTY       (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.ATTRIBUTE),
    DOM_ATTRIBUTE_TO_BE             (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.ATTRIBUTE, Member.EXPECTED_VALUE),
    DOM_PROPERTY_TO_BE              (LocatorUse.REQUIRED,  Yields.BOOLEAN, Member.PROPERTY,  Member.EXPECTED_VALUE),

    // ---------- Cardinality ----------
    NUMBER_OF_ELEMENTS_TO_BE        (LocatorUse.REQUIRED,  Yields.ELEMENTS, Member.NUMBER),
    NUMBER_OF_ELEMENTS_MORE_THAN    (LocatorUse.REQUIRED,  Yields.ELEMENTS, Member.NUMBER),
    NUMBER_OF_ELEMENTS_LESS_THAN    (LocatorUse.REQUIRED,  Yields.ELEMENTS, Member.NUMBER),

    // ---------- Nested elements ----------
    PRESENCE_OF_NESTED_ELEMENT      (LocatorUse.REQUIRED,  Yields.ELEMENT,  Member.CHILD_USING, Member.CHILD_VALUE),
    PRESENCE_OF_NESTED_ELEMENTS     (LocatorUse.REQUIRED,  Yields.ELEMENTS, Member.CHILD_USING, Member.CHILD_VALUE),
    VISIBILITY_OF_NESTED_ELEMENTS   (LocatorUse.REQUIRED,  Yields.ELEMENTS, Member.CHILD_USING, Member.CHILD_VALUE),

    // ---------- Lifecycle ----------
    STALENESS_OF                    (LocatorUse.REQUIRED,  Yields.BOOLEAN),

    // ---------- Open extension point ----------
    /** Resolved by a {@code ConditionFactory}; requires {@code Condition.factory()} to be set. */
    CUSTOM                          (LocatorUse.OPTIONAL,  Yields.OBJECT);

    private final LocatorUse locatorUse;
    private final Yields yields;
    private final Set<Member> required;

    ExpectedConditionType(LocatorUse locatorUse, Yields yields, Member... required) {
        this.locatorUse = locatorUse;
        this.yields = yields;
        this.required = required.length == 0
                ? Collections.<Member>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(java.util.Arrays.asList(required)));
    }

    public LocatorUse locatorUse() { return locatorUse; }

    public Yields yields() { return yields; }

    /** Members that MUST be set. Also the complete set of members that MAY be set. */
    public Set<Member> requiredMembers() { return required; }

    public int arity() { return required.size(); }
}
